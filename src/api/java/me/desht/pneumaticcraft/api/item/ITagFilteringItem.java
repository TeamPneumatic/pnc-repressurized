package me.desht.pneumaticcraft.api.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Implement this on items which can be used to do a match by item tags.  E.g. the Tag Filter item.  Such items can
 * be passed as the first parameter to {@link IItemRegistry#doesItemMatchFilter(ItemStack, ItemStack, boolean, boolean, boolean)}
 */
public interface ITagFilteringItem {
    /**
     * Does the given item's tags have a non-empty intersection with the given filter stack's configured tag list?
     * @param filterStack the stack to filter against; should be of an item implementing this interface
     * @param item the item to check
     * @return true if there's an intersection, false otherwise
     */
    boolean matchTags(ItemStack filterStack, Item item);
}
