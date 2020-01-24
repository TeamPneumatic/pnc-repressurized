package me.desht.pneumaticcraft.common.recipes.machine;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

/**
 * TODO Not a true recipe type; here mainly as placeholder, and for JEI purposes.
 */
public class UVLightBoxRecipe {
    private final Ingredient in;
    private final ItemStack out;

    public UVLightBoxRecipe(Ingredient in, ItemStack out) {
        this.in = in;
        this.out = out;
    }

    public Ingredient getIn() {
        return in;
    }

    public ItemStack getOut() {
        return out;
    }
}
