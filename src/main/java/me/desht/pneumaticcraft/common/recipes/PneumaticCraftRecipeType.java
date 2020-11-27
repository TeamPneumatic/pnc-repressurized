package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.PneumaticCraftRecipe;
import me.desht.pneumaticcraft.common.fluid.FuelRegistry;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketClearRecipeCache;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.machine.*;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidMixer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
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

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class PneumaticCraftRecipeType<T extends PneumaticCraftRecipe> implements IRecipeType<T> {
    private static final List<PneumaticCraftRecipeType<? extends PneumaticCraftRecipe>> types = new ArrayList<>();

    public static final PneumaticCraftRecipeType<AmadronOffer> AMADRON_OFFERS
            = registerType(PneumaticCraftRecipeTypes.AMADRON_OFFERS);
    public static final PneumaticCraftRecipeType<AssemblyRecipe> ASSEMBLY_LASER
            = registerType(PneumaticCraftRecipeTypes.ASSEMBLY_LASER);
    public static final PneumaticCraftRecipeType<AssemblyRecipe> ASSEMBLY_DRILL
            = registerType(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL);
    public static final PneumaticCraftRecipeType<AssemblyRecipe> ASSEMBLY_DRILL_LASER
            = registerType(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL_LASER);
    public static final PneumaticCraftRecipeType<ExplosionCraftingRecipeImpl> EXPLOSION_CRAFTING
            = registerType(PneumaticCraftRecipeTypes.EXPLOSION_CRAFTING);
    public static final PneumaticCraftRecipeType<HeatFrameCoolingRecipeImpl> HEAT_FRAME_COOLING
            = registerType(PneumaticCraftRecipeTypes.HEAT_FRAME_COOLING);
    public static final PneumaticCraftRecipeType<PressureChamberRecipeImpl> PRESSURE_CHAMBER
            = registerType(PneumaticCraftRecipeTypes.PRESSURE_CHAMBER);
    public static final PneumaticCraftRecipeType<RefineryRecipeImpl> REFINERY
            = registerType(PneumaticCraftRecipeTypes.REFINERY);
    public static final PneumaticCraftRecipeType<ThermoPlantRecipeImpl> THERMO_PLANT
            = registerType(PneumaticCraftRecipeTypes.THERMO_PLANT);
    public static final PneumaticCraftRecipeType<FluidMixerRecipeImpl> FLUID_MIXER
            = registerType(PneumaticCraftRecipeTypes.FLUID_MIXER);

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
    }

    public Map<ResourceLocation, T> getRecipes(World world) {
        if (world == null) {
            // we should pretty much always have a world, but here's a fallback: the overworld
            world = ServerLifecycleHooks.getCurrentServer().getWorld(World.OVERWORLD);
            if (world == null) return Collections.emptyMap();
        }

        if (cachedRecipes.isEmpty()) {
            RecipeManager recipeManager = world.getRecipeManager();
            List<T> recipes = recipeManager.getRecipes(this, PneumaticCraftRecipe.DummyIInventory.getInstance(), world);
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
                FuelRegistry.getInstance().clearCachedFuelFluids();
                if (ServerLifecycleHooks.getCurrentServer() != null) {
                    NetworkHandler.sendToAll(new PacketClearRecipeCache());
                }
            }, gameExecutor).thenCompose(stage::markCompleteAwaitingOthers);
        }
    }
}
