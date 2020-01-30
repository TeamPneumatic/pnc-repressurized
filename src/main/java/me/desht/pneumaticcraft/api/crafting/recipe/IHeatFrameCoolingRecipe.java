package me.desht.pneumaticcraft.api.crafting.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public interface IHeatFrameCoolingRecipe extends IModRecipe {

    /**
     * Get the input ingredient
     * @return the input ingredient
     */
    Ingredient getInput();

    /** Get the number of input ingredients which will be used.
     *
     * @return the number of ingredients
     */
    int getInputAmount();

    /**
     * Get the output item
     * @return the output item
     */
    ItemStack getOutput();

    /**
     * Get the threshold temperature (Kelvin) below which cooling occurs.
     *
     * @return the threshold temperature
     */
    int getTemperature();

    /**
     * Check if the given itemstack is valid for this recipe.
     *
     * @param stack the itemstack
     * @return true if this itemstack is valid for this recipe
     */
    boolean matches(ItemStack stack);
}
