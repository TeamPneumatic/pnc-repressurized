package me.desht.pneumaticcraft.api.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface ITagFilteringItem {
    boolean matchTags(ItemStack filterStack, Item item);
}
