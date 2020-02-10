package me.desht.pneumaticcraft.api.crafting.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.Random;

public interface IHeatFrameCoolingRecipe extends IModRecipe {
    /**
     * Get the input ingredient. Fluid ingredients are acceptable and will be matching by an item containing that fluid
     * (and which provides an IFluidHandlerItem capability).
     *
     * @return the input ingredient
     */
    Ingredient getInput();

    /**
     * Get the output item. This does not take into account any bonus multiplier.
     * @return the output item
     */
    ItemStack getOutput();

    /**
     * Get the threshold temperature (Kelvin) below which cooling occurs.
     *
     * @return the threshold temperature
     */
    int getThresholdTemperature();

    /**
     * Get the bonus output multiplier; for every degree below the threshold temperature, this raises the chance of
     * a bonus output by this amount.  E.g. with a multiplier of 0.01, there would be a 50% chance of a bonus output
     * when the current temperature is 50 below the threshold temperature.
     * <p>
     * Note that the calculated bonus chance could be greater than 1; in that case there is a guaranteed second output
     * plus a chance of a third, and so on.
     *
     * @return a bonus multiplier
     */
    float getBonusMultiplier();

    /**
     * Get the bonus limit; a hard ceiling on the bonus output chance.  E.g. with a limit of 1.8, there will never be
     * a better than 80% chance of a bonus output.
     *
     * @return a bonus limit
     */
    float getBonusLimit();

    /**
     * Check if the given itemstack is valid for this recipe.
     *
     * @param stack the itemstack
     * @return true if this itemstack is valid for this recipe
     */
    boolean matches(ItemStack stack);

    /**
     * Calculate an output quantity based on the recipe's bonus settings and the current temperature of the heat frame.
     *
     * @param temperature heat frame's current temperature
     * @return the number of output items
     */
    default int calculateOutputQuantity(double temperature) {
        if (getBonusMultiplier() <= 0) return 1;
        float delta = getThresholdTemperature() - (float)temperature;
        if (delta < 0) return 1;
        float mul = 1 + Math.min(getBonusLimit(), getBonusMultiplier() * delta);
        int result = (int) mul;
        if (new Random().nextFloat() < mul - result) result++;
        return result;
    }
}
