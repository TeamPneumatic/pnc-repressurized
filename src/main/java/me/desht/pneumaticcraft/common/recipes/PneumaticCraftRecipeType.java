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
import me.desht.pneumaticcraft.common.block.entity.FluidMixerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.PressureChamberInterfaceBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.RefineryControllerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.ThermoPlantBlockEntity;
import me.desht.pneumaticcraft.common.fluid.FuelRegistry;
import me.desht.pneumaticcraft.common.heat.BlockHeatProperties;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketClearRecipeCache;
import me.desht.pneumaticcraft.common.recipes.machine.AssemblyRecipeImpl;
import me.desht.pneumaticcraft.common.recipes.machine.HeatFrameCoolingRecipeImpl;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PneumaticCraftRecipeType<T extends PneumaticCraftRecipe> implements RecipeType<T> {
    private static CacheReloadListener cacheReloadListener;

    private final Map<ResourceLocation, RecipeHolder<T>> cachedRecipes = new HashMap<>();
    private final String typeName;

    public PneumaticCraftRecipeType(String name) {
        this.typeName = "PneumaticCraftRecipeType[" + RL(name) + "]";
    }

    @Override
    public String toString() {
        return typeName;
    }

    public Map<ResourceLocation, RecipeHolder<T>> getRecipeMap(Level level) {
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
            RecipeManager recipeManager = level.getRecipeManager();
            List<RecipeHolder<T>> recipes = recipeManager.getRecipesFor(this, PneumaticCraftRecipe.DummyIInventory.getInstance(), level);
            recipes.forEach(recipe -> cachedRecipes.put(recipe.id(), recipe));

            if (this == ModRecipeTypes.ASSEMBLY_DRILL_LASER.get()) {

                var drillRecipes = level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.ASSEMBLY_DRILL.get());
                var laserRecipes = level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.ASSEMBLY_LASER.get());
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

    public Optional<RecipeHolder<T>> getRecipe(Level level, ResourceLocation recipeId) {
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
        public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return CompletableFuture.runAsync(() -> {
                clearCachedRecipes();
                if (ServerLifecycleHooks.getCurrentServer() != null) {
                    NetworkHandler.sendToAll(PacketClearRecipeCache.INSTANCE);
                }
            }, gameExecutor).thenCompose(stage::wait);
        }
    }
}
