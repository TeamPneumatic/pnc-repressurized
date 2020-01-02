package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.common.recipes.machine.ExplosionCraftingRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface IExplosionCraftingRecipe extends IModRecipe {
    Ingredient getInput();

    int getAmount();

    List<ItemStack> getOutputs();

    int getLossRate();

    boolean matches(ItemStack stack);

    /**
     * Create a basic explosion crafting recipe.  This uses in-world explosions to convert nearby items on the ground
     * (in item entity form) to one or more other items.  See {@link me.desht.pneumaticcraft.api.crafting.StackedIngredient}
     * if you need a recipe taking multiples of an input item.
     *
     * @param id unique ID for the recipe
     * @param input the input ingredient
     * @param lossRate the average item loss rate, as a percentage
     * @param outputs the output items
     * @return a basic Explosion Crafting recipe
     */
    static IExplosionCraftingRecipe basicRecipe(ResourceLocation id, Ingredient input, int lossRate, ItemStack... outputs) {
        return new ExplosionCraftingRecipe(id, input, lossRate, outputs);
    }
}
