package me.desht.pneumaticcraft.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IThermopneumaticProcessingPlantRecipe {
    /**
     * Should return true when this recipe is valid, providing the input items. Do not modify these input items yet.
     *
     * @param inputTank
     * @param inputItem
     * @return
     */
    boolean isValidRecipe(FluidStack inputTank, ItemStack inputItem);

    /**
     * Should return the output fluid stack for this recipe. Do not modify these input items yet.
     *
     * @param inputTank
     * @param inputItem
     * @return
     */
    FluidStack getRecipeOutput(FluidStack inputTank, ItemStack inputItem);

    /**
     * Decrease the input items used in the recipe here. When the stacksize is decreased to 0 it will automatically be set to null, so you don't have to worry about that.
     *
     * @param inputTank
     * @param inputItem
     */
    void useRecipeItems(FluidStack inputTank, ItemStack inputItem);

    /**
     * @param inputTank
     * @param inputItem
     * @return temperature in degrees Kelvin.
     */
    double getRequiredTemperature(FluidStack inputTank, ItemStack inputItem);

    double heatUsed(FluidStack inputTank, ItemStack inputItem);

    float getRequiredPressure(FluidStack inputTank, ItemStack inputItem);

    int airUsed(FluidStack inputTank, ItemStack inputItem);
}
