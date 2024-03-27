package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.recipe.*;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES 
            = DeferredRegister.create(Registries.RECIPE_TYPE, Names.MOD_ID);

    public static final Supplier<PneumaticCraftRecipeType<AmadronRecipe>> AMADRON
            = register(PneumaticCraftRecipeTypes.AMADRON_OFFERS, PneumaticCraftRecipeType::new);
    public static final Supplier<PneumaticCraftRecipeType<AssemblyRecipe>> ASSEMBLY_LASER
            = register(PneumaticCraftRecipeTypes.ASSEMBLY_LASER, PneumaticCraftRecipeType::new);
    public static final Supplier<PneumaticCraftRecipeType<AssemblyRecipe>> ASSEMBLY_DRILL
            = register(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL, PneumaticCraftRecipeType::new);
    public static final Supplier<PneumaticCraftRecipeType<AssemblyRecipe>> ASSEMBLY_DRILL_LASER
            = register(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL_LASER, PneumaticCraftRecipeType::new);
    public static final Supplier<PneumaticCraftRecipeType<HeatPropertiesRecipe>> BLOCK_HEAT_PROPERTIES
            = register(PneumaticCraftRecipeTypes.HEAT_PROPERTIES, PneumaticCraftRecipeType::new);
    public static final Supplier<PneumaticCraftRecipeType<ExplosionCraftingRecipe>> EXPLOSION_CRAFTING
            = register(PneumaticCraftRecipeTypes.EXPLOSION_CRAFTING, PneumaticCraftRecipeType::new);
    public static final Supplier<PneumaticCraftRecipeType<FluidMixerRecipe>> FLUID_MIXER
            = register(PneumaticCraftRecipeTypes.FLUID_MIXER, PneumaticCraftRecipeType::new);
    public static final Supplier<PneumaticCraftRecipeType<FuelQualityRecipe>> FUEL_QUALITY
            = register(PneumaticCraftRecipeTypes.FUEL_QUALITY, PneumaticCraftRecipeType::new);
    public static final Supplier<PneumaticCraftRecipeType<HeatFrameCoolingRecipe>> HEAT_FRAME_COOLING
            = register(PneumaticCraftRecipeTypes.HEAT_FRAME_COOLING, PneumaticCraftRecipeType::new);
    public static final Supplier<PneumaticCraftRecipeType<PressureChamberRecipe>> PRESSURE_CHAMBER
            = register(PneumaticCraftRecipeTypes.PRESSURE_CHAMBER, PneumaticCraftRecipeType::new);
    public static final Supplier<PneumaticCraftRecipeType<RefineryRecipe>> REFINERY
            = register(PneumaticCraftRecipeTypes.REFINERY, PneumaticCraftRecipeType::new);
    public static final Supplier<PneumaticCraftRecipeType<ThermoPlantRecipe>> THERMO_PLANT
            = register(PneumaticCraftRecipeTypes.THERMO_PLANT, PneumaticCraftRecipeType::new);

    private static <T extends PneumaticCraftRecipeType<?>> Supplier<T> register(String name, Function<String, T> factory) {
        return RECIPE_TYPES.register(name, () -> factory.apply(name));
    }

    public static <T extends PneumaticCraftRecipe> Collection<RecipeHolder<T>> getRecipes(Level level, Supplier<PneumaticCraftRecipeType<T>> sup) {
        return sup.get().allRecipeHolders(level);
    }
}
