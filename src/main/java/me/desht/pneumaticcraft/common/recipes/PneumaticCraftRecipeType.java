package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.recipe.*;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.fluid.FuelRegistry;
import me.desht.pneumaticcraft.common.heat.BlockHeatProperties;
import me.desht.pneumaticcraft.common.item.ItemSeismicSensor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketClearRecipeCache;
import me.desht.pneumaticcraft.common.recipes.machine.AssemblyRecipeImpl;
import me.desht.pneumaticcraft.common.recipes.machine.FluidMixerRecipeImpl;
import me.desht.pneumaticcraft.common.recipes.machine.HeatFrameCoolingRecipeImpl;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidMixer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumTrap;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class PneumaticCraftRecipeType<T extends PneumaticCraftRecipe> implements IRecipeType<T> {
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
    static void registerRecipeTypes(IForgeRegistry<IRecipeSerializer<?>> registry) {
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
        TileEntityFluidMixer.clearCachedFluids();
        TileEntityPressureChamberInterface.clearCachedItems();
        TileEntityThermopneumaticProcessingPlant.clearCachedItemsAndFluids();
        AmadronOfferManager.getInstance().rebuildRequired();
        FuelRegistry.getInstance().clearCachedFuelFluids();
        ItemSeismicSensor.clearCachedFluids();
        BlockHeatProperties.getInstance().clear();
        CraftingRecipeCache.INSTANCE.clear();
        TileEntityVacuumTrap.clearBlacklistCache();
    }

    public Map<ResourceLocation, T> getRecipes(World world) {
        if (world == null) {
            // we should pretty much always have a world, but use the overworld as a fallback
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                world = server.getLevel(World.OVERWORLD);
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
                TileEntityFluidMixer.cacheRecipeFluids((List<FluidMixerRecipeImpl>) recipes);
            }
        }

        return cachedRecipes;
    }

    public Stream<T> stream(World world) {
        return getRecipes(world).values().stream();
    }

    public T findFirst(World world, Predicate<T> predicate) {
        return stream(world).filter(predicate).findFirst().orElse(null);
    }

    public T getRecipe(World world, ResourceLocation recipeId) {
        return getRecipes(world).get(recipeId);
    }

    public static class CacheReloadListener implements IFutureReloadListener {
        @Override
        public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return CompletableFuture.runAsync(() -> {
                clearCachedRecipes();
                if (ServerLifecycleHooks.getCurrentServer() != null) {
                    NetworkHandler.sendToAll(new PacketClearRecipeCache());
                }
            }, gameExecutor).thenCompose(stage::wait);
        }
    }
}
