package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.recipe.ItemIngredient;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HeatFrameCoolingRecipe {
	public static final List<HeatFrameCoolingRecipe> recipes = new ArrayList<>();
	
	public final ItemIngredient input;
	public final ItemStack output;

    public HeatFrameCoolingRecipe(ItemIngredient input, ItemStack output) {
        this.input = input;
        this.output = output;
    }
}
