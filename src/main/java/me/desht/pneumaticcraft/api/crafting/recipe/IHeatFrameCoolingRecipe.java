package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.common.recipes.machine.HeatFrameCoolingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

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

    /**
     * Create a standard Heat Frame cooling recipe.
     *
     * @param id unique ID for the recipe
     * @param input the input ingredient
     * @param temperature the temperature (Kelvin) below which the cooling process occurs
     * @param output the output item
     * @return a basic Heat Frame cooling recipe
     */
    static IHeatFrameCoolingRecipe basicRecipe(ResourceLocation id, Ingredient input, int temperature, ItemStack output) {
        return new HeatFrameCoolingRecipe(id, input, temperature, output);
    }

}
