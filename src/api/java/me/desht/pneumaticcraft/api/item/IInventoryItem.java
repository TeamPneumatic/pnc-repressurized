/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.item;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

/**
 * Implement this interface for your items that have an inventory. When you don't have access to the item, just create
 * a class that implements this interface, and register an instance of it with
 * {@link IItemRegistry#registerInventoryItem(IInventoryItem)}.
 * This will then will be used in the Pneumatic Helmet's item search and item tooltip generation.
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
    default Component getInventoryHeader() { return null; }

    /**
     * A String to prepend to outputted tooltip lines. Can be used to apply colouring, for example.
     *
     * @param stack the itemstack currently being added to the tooltip
     * @return a string prefix
     */
    default String getTooltipPrefix(ItemStack stack) { return ""; }

    /**
     * Convenience implementation for {@link IInventoryItem#getStacksInItem(ItemStack, List)} for items have been
     * dropped from a block entity block with serialized data.
     *
     * @param stack the stack
     * @param curStacks a list of stacks to fill
     */
    static void getStacks(ItemStack stack, List<ItemStack> curStacks) {
        CompoundTag sub = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
        if (sub != null && sub.contains(NBTKeys.NBT_ITEM_INV, Tag.TAG_COMPOUND)) {
            ItemStackHandler handler = new ItemStackHandler();
            handler.deserializeNBT(sub.getCompound(NBTKeys.NBT_ITEM_INV));
            for (int i = 0; i < handler.getSlots(); i++) {
                if (!handler.getStackInSlot(i).isEmpty()) curStacks.add(handler.getStackInSlot(i));
            }
        }
    }
}
