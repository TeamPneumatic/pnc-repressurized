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

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.recipe.*;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.block.entity.FluidMixerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.PressureChamberInterfaceBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.ThermopneumaticProcessingPlantBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.VacuumTrapBlockEntity;
import me.desht.pneumaticcraft.common.fluid.FuelRegistry;
import me.desht.pneumaticcraft.common.heat.BlockHeatProperties;
import me.desht.pneumaticcraft.common.item.ItemSeismicSensor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketClearRecipeCache;
import me.desht.pneumaticcraft.common.recipes.machine.AssemblyRecipeImpl;
import me.desht.pneumaticcraft.common.recipes.machine.HeatFrameCoolingRecipeImpl;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PneumaticCraftRecipeType<T extends PneumaticCraftRecipe> implements RecipeType<T> {
    private static final List<PneumaticCraftRecipeType<? extends PneumaticCraftRecipe>> types = new ArrayList<>();

    public static final PneumaticCraftRecipeType<AmadronRecipe> AMADRON_OFFERS
            = registerType(PneumaticCraftRecipeTypes.AMADRON_OFFERS);
    public static final PneumaticCraftRecipeType<AssemblyRecipe> ASSEMBLY_LASER
            = registerType(PneumaticCraftRecipeTypes.ASSEMBLY_LASER);
    public static final PneumaticCraftRecipeType<AssemblyRecipe> ASSEMBLY_DRILL
            = registerType(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL);
    public static final PneumaticCraftRecipeType<AssemblyRecipe> ASSEMBLY_DRILL_LASER
            = registerType(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL_LASER);
    public static final PneumaticCraftRecipeType<ExplosionCraftingRecipe> EXPLOSION_CRAFTING
            = registerType(PneumaticCraftRecipeTypes.EXPLOSION_CRAFTING);
    public static final PneumaticCraftRecipeType<HeatFrameCoolingRecipe> HEAT_FRAME_COOLING
            = registerType(PneumaticCraftRecipeTypes.HEAT_FRAME_COOLING);
    public static final PneumaticCraftRecipeType<PressureChamberRecipe> PRESSURE_CHAMBER
            = registerType(PneumaticCraftRecipeTypes.PRESSURE_CHAMBER);
    public static final PneumaticCraftRecipeType<RefineryRecipe> REFINERY
            = registerType(PneumaticCraftRecipeTypes.REFINERY);
    public static final PneumaticCraftRecipeType<ThermoPlantRecipe> THERMO_PLANT
            = registerType(PneumaticCraftRecipeTypes.THERMO_PLANT);
    public static final PneumaticCraftRecipeType<FluidMixerRecipe> FLUID_MIXER
            = registerType(PneumaticCraftRecipeTypes.FLUID_MIXER);
    public static final PneumaticCraftRecipeType<FuelQualityRecipe> FUEL_QUALITY
            = registerType(PneumaticCraftRecipeTypes.FUEL_QUALITY);
    public static final PneumaticCraftRecipeType<HeatPropertiesRecipe> HEAT_PROPERTIES
            = registerType(PneumaticCraftRecipeTypes.HEAT_PROPERTIES);

    private final Map<ResourceLocation, T> cachedRecipes = new HashMap<>();
    private final ResourceLocation registryName;
    private static CacheReloadListener cacheReloadListener;

    private static <T extends PneumaticCraftRecipe> PneumaticCraftRecipeType<T> registerType(String name) {
        PneumaticCraftRecipeType<T> type = new PneumaticCraftRecipeType<>(name);
        types.add(type);
        return type;
    }

    // TODO: use a Forge registry if/when there is one for recipe types
    static void registerRecipeTypes(IForgeRegistry<RecipeSerializer<?>> registry) {
        types.forEach(type -> Registry.register(Registry.RECIPE_TYPE, type.registryName, type));
    }

    private PneumaticCraftRecipeType(String name) {
        this.registryName = RL(name);
    }

    public static CacheReloadListener getCacheReloadListener() {
        if (cacheReloadListener == null) {
            cacheReloadListener = new CacheReloadListener();
        }
        return cacheReloadListener;
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    public static void clearCachedRecipes() {
        types.forEach(type -> type.cachedRecipes.clear());

        HeatFrameCoolingRecipeImpl.cacheMaxThresholdTemp(Collections.emptyList());  // clear the cached temp
        FluidMixerBlockEntity.clearCachedFluids();
        PressureChamberInterfaceBlockEntity.clearCachedItems();
        ThermopneumaticProcessingPlantBlockEntity.clearCachedItemsAndFluids();
        AmadronOfferManager.getInstance().rebuildRequired();
        FuelRegistry.getInstance().clearCachedFuelFluids();
        ItemSeismicSensor.clearCachedFluids();
        BlockHeatProperties.getInstance().clear();
        CraftingRecipeCache.INSTANCE.clear();
        VacuumTrapBlockEntity.clearBlacklistCache();
    }

    public Map<ResourceLocation, T> getRecipes(Level world) {
        if (world == null) {
            // we should pretty much always have a world, but use the overworld as a fallback
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                world = server.getLevel(Level.OVERWORLD);
            }
            if (world == null) {
                // still no world?  oh well
                Log.error("detected someone trying to get recipes for %s with no world available - returning empty recipe list", registryName.toString());
                return Collections.emptyMap();
            }
        }

        if (cachedRecipes.isEmpty()) {
            RecipeManager recipeManager = world.getRecipeManager();
            List<T> recipes = recipeManager.getRecipesFor(this, PneumaticCraftRecipe.DummyIInventory.getInstance(), world);
            recipes.forEach(recipe -> cachedRecipes.put(recipe.getId(), recipe));

            if (this == ASSEMBLY_DRILL_LASER) {
                Collection<AssemblyRecipe> drillRecipes = PneumaticCraftRecipeType.ASSEMBLY_DRILL.getRecipes(world).values();
                Collection<AssemblyRecipe> laserRecipes = PneumaticCraftRecipeType.ASSEMBLY_LASER.getRecipes(world).values();
                AssemblyRecipeImpl.calculateAssemblyChain(drillRecipes, laserRecipes).forEach((id, recipe) -> cachedRecipes.put(id, (T) recipe));
            } else if (this == FLUID_MIXER) {
                List<FluidMixerRecipe> l = recipes.stream().filter(r -> r instanceof FluidMixerRecipe).map(r -> (FluidMixerRecipe)r).toList();
                FluidMixerBlockEntity.cacheRecipeFluids(l);
            }
        }

        return cachedRecipes;
    }

    public Stream<T> stream(Level world) {
        return getRecipes(world).values().stream();
    }

    public T findFirst(Level world, Predicate<T> predicate) {
        return stream(world).filter(predicate).findFirst().orElse(null);
    }

    public T getRecipe(Level world, ResourceLocation recipeId) {
        return getRecipes(world).get(recipeId);
    }

    public static class CacheReloadListener implements PreparableReloadListener {
        @Override
        public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return CompletableFuture.runAsync(() -> {
                clearCachedRecipes();
                if (ServerLifecycleHooks.getCurrentServer() != null) {
                    NetworkHandler.sendToAll(new PacketClearRecipeCache());
                }
            }, gameExecutor).thenCompose(stage::wait);
        }
    }
}
