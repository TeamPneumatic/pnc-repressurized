package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.recipe.*;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.function.Supplier;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, Names.MOD_ID);

    public static final RegistryObject<PneumaticCraftRecipeType<AmadronRecipe>> AMADRON
            = register(PneumaticCraftRecipeTypes.AMADRON_OFFERS, PneumaticCraftRecipeType::new);
    public static final RegistryObject<PneumaticCraftRecipeType<AssemblyRecipe>> ASSEMBLY_LASER
            = register(PneumaticCraftRecipeTypes.ASSEMBLY_LASER, PneumaticCraftRecipeType::new);
    public static final RegistryObject<PneumaticCraftRecipeType<AssemblyRecipe>> ASSEMBLY_DRILL
            = register(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL, PneumaticCraftRecipeType::new);
    public static final RegistryObject<PneumaticCraftRecipeType<AssemblyRecipe>> ASSEMBLY_DRILL_LASER
            = register(PneumaticCraftRecipeTypes.ASSEMBLY_DRILL_LASER, PneumaticCraftRecipeType::new);
    public static final RegistryObject<PneumaticCraftRecipeType<HeatPropertiesRecipe>> BLOCK_HEAT_PROPERTIES
            = register(PneumaticCraftRecipeTypes.HEAT_PROPERTIES, PneumaticCraftRecipeType::new);
    public static final RegistryObject<PneumaticCraftRecipeType<ExplosionCraftingRecipe>> EXPLOSION_CRAFTING
            = register(PneumaticCraftRecipeTypes.EXPLOSION_CRAFTING, PneumaticCraftRecipeType::new);
    public static final RegistryObject<PneumaticCraftRecipeType<FluidMixerRecipe>> FLUID_MIXER
            = register(PneumaticCraftRecipeTypes.FLUID_MIXER, PneumaticCraftRecipeType::new);
    public static final RegistryObject<PneumaticCraftRecipeType<FuelQualityRecipe>> FUEL_QUALITY
            = register(PneumaticCraftRecipeTypes.FUEL_QUALITY, PneumaticCraftRecipeType::new);
    public static final RegistryObject<PneumaticCraftRecipeType<HeatFrameCoolingRecipe>> HEAT_FRAME_COOLING
            = register(PneumaticCraftRecipeTypes.HEAT_FRAME_COOLING, PneumaticCraftRecipeType::new);
    public static final RegistryObject<PneumaticCraftRecipeType<PressureChamberRecipe>> PRESSURE_CHAMBER
            = register(PneumaticCraftRecipeTypes.PRESSURE_CHAMBER, PneumaticCraftRecipeType::new);
    public static final RegistryObject<PneumaticCraftRecipeType<RefineryRecipe>> REFINERY
            = register(PneumaticCraftRecipeTypes.REFINERY, PneumaticCraftRecipeType::new);
    public static final RegistryObject<PneumaticCraftRecipeType<ThermoPlantRecipe>> THERMO_PLANT
            = register(PneumaticCraftRecipeTypes.THERMO_PLANT, PneumaticCraftRecipeType::new);

    private static <T extends PneumaticCraftRecipeType<?>> RegistryObject<T> register(String name, Supplier<T> sup) {
        return RECIPE_TYPES.register(name, sup);
    }

    public static <T extends PneumaticCraftRecipe> Collection<T> getRecipes(Level world, Supplier<PneumaticCraftRecipeType<T>> sup) {
        return sup.get().getRecipes(world).values();
    }
}
