/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.PneumaticCraftRecipe;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.block.entity.processing.FluidMixerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.processing.PressureChamberInterfaceBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.processing.RefineryControllerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.processing.ThermoPlantBlockEntity;
import me.desht.pneumaticcraft.common.fluid.FuelRegistry;
import me.desht.pneumaticcraft.common.heat.BlockHeatProperties;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketClearRecipeCache;
import me.desht.pneumaticcraft.common.recipes.machine.AssemblyRecipeImpl;
import me.desht.pneumaticcraft.common.recipes.machine.HeatFrameCoolingRecipeImpl;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PneumaticCraftRecipeType<T extends PneumaticCraftRecipe> implements RecipeType<T> {
    private static CacheReloadListener cacheReloadListener;
    private static RecipeMap clientRecipeMap = RecipeMap.EMPTY;

    private final Map<Identifier, RecipeHolder<T>> cachedRecipes = new HashMap<>();
    private final String typeName;

    public PneumaticCraftRecipeType(String name) {
        this.typeName = "PneumaticCraftRecipeType[" + RL(name) + "]";
    }

    @Override
    public String toString() {
        return typeName;
    }

    public Map<Identifier, RecipeHolder<T>> getRecipeMap(Level level) {
        if (level == null) {
            // we should pretty much always have a world, but use the overworld as a fallback
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                level = server.getLevel(Level.OVERWORLD);
            }
            if (level == null) {
                // still no world?  oh well
                Log.error("detected someone trying to get recipes for {} with no world available - returning empty recipe list", this);
                return Collections.emptyMap();
            }
        }

        if (cachedRecipes.isEmpty()) {
            RecipeMap recipeMap = recipeMap(level);
            Collection<RecipeHolder<T>> recipes = recipeMap.byType(this);
            recipes.forEach(recipe -> cachedRecipes.put(recipe.id().identifier(), recipe));

            if (this == ModRecipeTypes.ASSEMBLY_DRILL_LASER.get()) {
                var drillRecipes = List.copyOf(recipeMap.byType(ModRecipeTypes.ASSEMBLY_DRILL.get()));
                var laserRecipes = List.copyOf(recipeMap.byType(ModRecipeTypes.ASSEMBLY_LASER.get()));
                //noinspection unchecked
                AssemblyRecipeImpl.calculateAssemblyChain(drillRecipes, laserRecipes)
                        .forEach((id, recipe) -> cachedRecipes.put(id, (RecipeHolder<T>) recipe));
            } else if (this == ModRecipeTypes.FLUID_MIXER.get()) {
                List<FluidMixerRecipe> l = recipes.stream()
                        .filter(r -> r.value() instanceof FluidMixerRecipe)
                        .map(r -> (FluidMixerRecipe)r.value())
                        .toList();
                FluidMixerBlockEntity.cacheRecipeFluids(l);
            }
        }

        return cachedRecipes;
    }

    /**
     * The client no longer gets all recipes from the server: PneumaticCraft recipe types are requested
     * in {@link #onDatapackSync(OnDatapackSyncEvent)} and arrive in {@code RecipesReceivedEvent}, which hands
     * them to {@link #setClientRecipeMap(RecipeMap)}.
     */
    private static RecipeMap recipeMap(Level level) {
        return level instanceof ServerLevel serverLevel ? serverLevel.recipeAccess().recipeMap() : clientRecipeMap;
    }

    public static void setClientRecipeMap(RecipeMap recipeMap) {
        clientRecipeMap = recipeMap;
        clearCachedRecipes();
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        for (var type : ModRecipeTypes.RECIPE_TYPES.getEntries()) {
            if (type.get() instanceof PneumaticCraftRecipeType<?> pncrType && pncrType != ModRecipeTypes.ASSEMBLY_DRILL_LASER.get()) {
                event.sendRecipes(pncrType);
            }
        }
    }

    public Collection<RecipeHolder<T>> allRecipeHolders(Level level) {
        return Collections.unmodifiableCollection(getRecipeMap(level).values());
    }

    public Collection<T> allRecipes(Level level) {
        return getRecipeMap(level).values().stream().map(RecipeHolder::value).toList();
    }

    public Stream<RecipeHolder<T>> stream(Level level) {
        return getRecipeMap(level).values().stream();
    }

    public Optional<RecipeHolder<T>> findFirst(Level level, Predicate<T> predicate) {
        return stream(level)
                .filter(holder -> predicate.test(holder.value()))
                .findFirst();
    }

    public Optional<RecipeHolder<T>> getRecipe(Level level, Identifier recipeId) {
        return Optional.ofNullable(getRecipeMap(level).get(recipeId));
    }

    public static CacheReloadListener getCacheReloadListener() {
        if (cacheReloadListener == null) {
            cacheReloadListener = new CacheReloadListener();
        }
        return cacheReloadListener;
    }

    public static void clearCachedRecipes() {
        for (var type : ModRecipeTypes.RECIPE_TYPES.getEntries()) {
            if (type.get() instanceof PneumaticCraftRecipeType<?> pncrType) {
                pncrType.cachedRecipes.clear();
            }
        }

        HeatFrameCoolingRecipeImpl.cacheMaxThresholdTemp(Collections.emptyList());  // clear the cached temp
        FluidMixerBlockEntity.clearCachedFluids();
        PressureChamberInterfaceBlockEntity.clearCachedItems();
        ThermoPlantBlockEntity.clearCachedItemsAndFluids();
        RefineryControllerBlockEntity.clearCachedFluids();
        AmadronOfferManager.getInstance().rebuildRequired();
        FuelRegistry.getInstance().clearCachedFuelFluids();
        BlockHeatProperties.getInstance().clear();
        VanillaRecipeCache.clearAll();
        RecipeCaches.clearAll();
    }

    public static class CacheReloadListener implements PreparableReloadListener {
        @Override
        public CompletableFuture<Void> reload(SharedState currentReload, Executor taskExecutor, PreparationBarrier preparationBarrier, Executor reloadExecutor) {
            return CompletableFuture.runAsync(() -> {
                clearCachedRecipes();
                if (ServerLifecycleHooks.getCurrentServer() != null) {
                    NetworkHandler.sendToAll(PacketClearRecipeCache.INSTANCE);
                }
            }, reloadExecutor).thenCompose(preparationBarrier::wait);
        }
    }
}
