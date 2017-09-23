package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.item.ItemStack;

public interface IItemFiltering {
    boolean isItemValidForFilters(ItemStack item);
}
