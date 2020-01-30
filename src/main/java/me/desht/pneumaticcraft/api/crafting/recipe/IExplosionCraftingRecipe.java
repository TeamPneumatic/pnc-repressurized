package me.desht.pneumaticcraft.api.crafting.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.List;

public interface IExplosionCraftingRecipe extends IModRecipe {
    Ingredient getInput();

    int getAmount();

    List<ItemStack> getOutputs();

    int getLossRate();

    boolean matches(ItemStack stack);
}
