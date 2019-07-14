package me.desht.pneumaticcraft.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

/**
 * Implement this interface for your items that have an inventory. When you don't have access to the item, just create any old class
 * that implements this interface and register an instance of it with {@link IItemRegistry#registerInventoryItem(IInventoryItem)}.
 * This will then will be used in the Pneumatic Helmet's item search.
 */
public interface IInventoryItem {

    /**
     * @param stack Item that potentially has an inventory.
     * @param curStacks List of all currently added stacks for this item. Add more stacks in here in your implementation when found the right item.
     */
    void getStacksInItem(ItemStack stack, List<ItemStack> curStacks);

    /**
     * Get a header for the inventory list, for tooltip purposes.  Default return of null will not add any header.
     * @return a header string (can be a translation string), or null for no header
     */
    default ITextComponent getInventoryHeader() { return null; }
}
