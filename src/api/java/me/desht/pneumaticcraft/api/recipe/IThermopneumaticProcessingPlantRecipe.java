package me.desht.pneumaticcraft.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public interface IThermopneumaticProcessingPlantRecipe {
    /**
     * Should return true when this recipe is valid, providing the input items. Do not modify these input items yet.
     *
     * @param inputFluid
     * @param inputItem
     * @return
     */
    boolean isValidRecipe(FluidStack inputFluid, ItemStack inputItem);
    
    /**
     * If the given fluid stack is valid in any situation for this recipe.
     * This is used to check if a fluid may enter the input tank of the machine.
     * THIS SHOULD BE OVERRIDEN, IT IS ONLY MARKED DEFAULT FOR BACKWARDS API COMPAT.
     * TODO 1.13 remove default implementation.
     * @param inputFluid
     * @return
     */
    default boolean isValidInput(@Nonnull FluidStack inputFluid){
        return true;
    }
    
    /**
     * If the given item stack is valid in any situation for this recipe.
     * This is used to check if an item may enter the input inventory of the machine.
     * THIS SHOULD BE OVERRIDEN, IT IS ONLY MARKED DEFAULT FOR BACKWARDS API COMPAT.
     * TODO 1.13 remove default implementation.
     * @param inputItem
     * @return
     */
    default boolean isValidInput(@Nonnull ItemStack inputItem){
        return true;
    }

    /**
     * Should return the output fluid stack for this recipe. Do not modify these input items yet.
     *
     * @param inputFluid
     * @param inputItem
     * @return
     */
    FluidStack getRecipeOutput(FluidStack inputFluid, ItemStack inputItem);

    /**
     * Decrease the input items used in the recipe here. When the stacksize is decreased to 0 it will automatically be set to null, so you don't have to worry about that.
     *
     * @param inputFluid
     * @param inputItem
     */
    void useRecipeItems(FluidStack inputFluid, ItemStack inputItem);

    /**
     * @param inputFluid
     * @param inputItem
     * @return temperature in degrees Kelvin.
     */
    double getRequiredTemperature(FluidStack inputFluid, @Nonnull ItemStack inputItem);

    double heatUsed(FluidStack inputFluid, @Nonnull ItemStack inputItem);

    float getRequiredPressure(FluidStack inputFluid, @Nonnull ItemStack inputItem);

    int airUsed(FluidStack inputFluid, @Nonnull ItemStack inputItem);
}
