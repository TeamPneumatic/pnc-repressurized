package me.desht.pneumaticcraft.api.recipe;

import me.desht.pneumaticcraft.common.recipes.HeatFrameCoolingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
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
     * @param inputAmount the quantity of input required & used
     * @param output the output item
     * @return a basic Heat Frame cooling recipe
     */
    static IHeatFrameCoolingRecipe basicRecipe(ResourceLocation id, Ingredient input, int inputAmount, ItemStack output) {
        return new HeatFrameCoolingRecipe(id, input, inputAmount, output);
    }

    /**
     * Used for client-side sync'ing of recipes: do not call directly!
     * @param buf a packet buffer
     * @return a deserialised recipe
     */
    static IHeatFrameCoolingRecipe read(PacketBuffer buf) {
        ResourceLocation id = buf.readResourceLocation();
        Ingredient input = Ingredient.read(buf);
        int amount = buf.readVarInt();
        ItemStack out = buf.readItemStack();
        return new HeatFrameCoolingRecipe(id, input, amount, out);
    }
}
