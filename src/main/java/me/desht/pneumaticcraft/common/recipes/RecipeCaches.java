package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.RefineryRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;

public class RecipeCaches {
    public static final RecipeCache<FluidMixerRecipe> FLUID_MIXER = new RecipeCache<>();
    public static final RecipeCache<ThermoPlantRecipe> THERMO_PLANT = new RecipeCache<>();
    public static final RecipeCache<RefineryRecipe> REFINERY = new RecipeCache<>();

    public static void clearAll() {
        FLUID_MIXER.clear();
        THERMO_PLANT.clear();
        REFINERY.clear();
    }
}
