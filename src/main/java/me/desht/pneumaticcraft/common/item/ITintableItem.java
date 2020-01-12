package me.desht.pneumaticcraft.common.item;

import net.minecraft.item.ItemStack;

public interface ITintableItem {
    int getTintColor(ItemStack stack, int tintIndex);
}
