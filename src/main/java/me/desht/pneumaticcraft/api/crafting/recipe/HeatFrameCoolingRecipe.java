package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.Random;

public abstract class HeatFrameCoolingRecipe extends PneumaticCraftRecipe {
    protected HeatFrameCoolingRecipe(ResourceLocation id) {
        super(id);
    }

    /**
     * Get the input ingredient. Fluid ingredients ({@link FluidIngredient}) are
     * acceptable and will be matching by an item containing that fluid, and which provides an {@link IFluidHandlerItem}
     * capability.
     *
     * @return the input ingredient
     */
    public abstract Ingredient getInput();

    /**
     * Get the output item. This does not take into account any bonus multiplier.
     * @return the output item
     */
    public abstract ItemStack getOutput();

    /**
     * Get the threshold temperature (Kelvin) below which cooling occurs.
     *
     * @return the threshold temperature
     */
    public abstract int getThresholdTemperature();

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
    public abstract float getBonusMultiplier();

    /**
     * Get the bonus limit; a hard ceiling on the bonus output chance.  E.g. with a limit of 0.8, there will never be
     * a better than 80% chance of a bonus output.
     *
     * @return a bonus limit
     */
    public abstract float getBonusLimit();

    /**
     * Check if the given itemstack is valid for this recipe.
     *
     * @param stack the itemstack
     * @return true if this itemstack is valid for this recipe
     */
    public abstract boolean matches(ItemStack stack);

    /**
     * Calculate an output quantity based on the recipe's bonus settings and the current temperature of the heat frame.
     *
     * @param temperature heat frame's current temperature
     * @return the number of output items
     */
    public final int calculateOutputQuantity(double temperature) {
        if (getBonusMultiplier() <= 0) return 1;
        float delta = getThresholdTemperature() - (float)temperature;
        if (delta < 0) return 1;
        float mul = 1 + Math.min(getBonusLimit(), getBonusMultiplier() * delta);
        int result = (int) mul;
        if (new Random().nextFloat() < mul - result) result++;
        return result;
    }
}
