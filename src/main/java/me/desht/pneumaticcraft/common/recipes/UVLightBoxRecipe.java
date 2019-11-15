package me.desht.pneumaticcraft.common.recipes;

import net.minecraft.item.ItemStack;

/**
 * Not a true recipe, but mainly for JEI purposes.
 * Maybe one day...
 */
public class UVLightBoxRecipe {
    private final ItemStack in;
    private final ItemStack out;

    public UVLightBoxRecipe(ItemStack in, ItemStack out) {
        this.in = in;
        this.out = out;
    }

    public ItemStack getIn() {
        return in;
    }

    public ItemStack getOut() {
        return out;
    }
}
