package me.desht.pneumaticcraft.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

/*
 * This file is part of Blue Power.
 *
 *     Blue Power is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Blue Power is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Blue Power.  If not, see <http://www.gnu.org/licenses/>
 */

/**
 * @author MineMaarten
 * @author Dynious
 */
public class IOHelper {
    public static class LocatedItemStack {
        public final ItemStack stack;
        public final int slot;

        static final LocatedItemStack NONE = new LocatedItemStack(ItemStack.EMPTY, -1);

        LocatedItemStack(ItemStack stack, int slot) {
            this.slot = slot;
            this.stack = stack;
        }

        @Override
        public String toString() {
            return "[ " + stack.toString() + " @ slot " + slot + " ]";
        }
    }

    public static IItemHandler getInventoryForTE(TileEntity te, EnumFacing facing) {
        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
        } else {
            return null;
        }
    }
    public static IItemHandler getInventoryForTE(TileEntity te) {
        return getInventoryForTE(te, null);
    }

    public static TileEntity getNeighbor(TileEntity te, EnumFacing dir) {
        return te.getWorld().getTileEntity(te.getPos().offset(dir));
    }

    /**
     * Extracts an exact item (including size) from the given tile entity, trying all sides.
     *
     * @param tile the tile to extract from
     * @param itemStack the precise item stack to extract (size matters)
     * @param simulate true if extraction should only be simulated
     * @return the extracted item stack
     */
    @Nonnull
    public static ItemStack extract(TileEntity tile, ItemStack itemStack, boolean simulate) {
        for (EnumFacing d : EnumFacing.VALUES) {
            IItemHandler handler = getInventoryForTE(tile, d);
            if (handler != null) {
                ItemStack extracted = extract(handler, itemStack, true, simulate);
                if (!extracted.isEmpty()) {
                    return extracted;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    public static LocatedItemStack extract(IItemHandler handler, boolean simulate) {
        for (int slot = 0; slot < handler.getSlots(); ++slot) {
            ItemStack stack = extract(handler, slot, simulate);
            if (!stack.isEmpty()) return new LocatedItemStack(stack, slot);
        }
        return LocatedItemStack.NONE;
    }


    @Nonnull
    public static ItemStack extract(IItemHandler handler, int slot, boolean simulate) {
        ItemStack stack = handler.getStackInSlot(slot);
        return handler.extractItem(slot, stack.getCount(), simulate);
    }

    @Nonnull
    public static ItemStack extract(IItemHandler handler, ItemStack requestedStack, boolean useItemCount, boolean simulate) {
        return extract(handler, requestedStack, useItemCount, simulate, false);
    }

    /**
     * Retrieves a specfied item from the specified inventory.
     *
     * @param handler
     * @param requestedStack
     * @param useItemCount   if true, it'll only retrieve the stack of the exact item count given. it'll look in multiple slots of the inventory. if false, the
     *                       first matching stack, ignoring item count, will be returned.
     * @param simulate
     * @param fuzzyMeta   ,
     * @return
     */
    @Nonnull
    public static ItemStack extract(IItemHandler handler, ItemStack requestedStack, boolean useItemCount, boolean simulate, boolean fuzzyMeta) {
        if (requestedStack.isEmpty()) return requestedStack;

        if (handler != null) {
            int itemsFound = 0;
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty() && matchStacks(stack, requestedStack, fuzzyMeta)) {
                    if (!useItemCount) {
                        return handler.extractItem(slot, stack.getCount(), simulate);
                    }
                    itemsFound += stack.getCount();
                }
            }
            if (itemsFound >= requestedStack.getCount()) {
                ItemStack exportedStack = ItemStack.EMPTY;
                int itemsNeeded = requestedStack.getCount();
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (stack.isItemEqual(requestedStack)) {
                        int itemsSubtracted = Math.min(itemsNeeded, stack.getCount());
                        if (itemsSubtracted > 0) {
                            exportedStack = stack;
                        }
                        itemsNeeded -= itemsSubtracted;
                        if (itemsNeeded <= 0) break;
                        handler.extractItem(slot, itemsSubtracted, simulate);
//                        if (!simulate) tile.markDirty();
                    }
                }
                exportedStack = exportedStack.copy();
                exportedStack.setCount(requestedStack.getCount());
                return exportedStack;
            }
        }
        return ItemStack.EMPTY;

    }

    private static boolean matchStacks(ItemStack stack1, ItemStack stack2, boolean fuzzyMeta) {
        return fuzzyMeta ? ItemStack.areItemsEqualIgnoreDurability(stack1, stack2) : ItemStack.areItemsEqual(stack1, stack2);
    }

    @Nonnull
    public static LocatedItemStack extractOneItem(IItemHandler handler, boolean simulate) {
        if (handler != null) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (stack.getCount() > 0) {
                    ItemStack ret = handler.extractItem(slot, 1, simulate);
                    return new LocatedItemStack(ret, slot);
                }
            }
        }
        return LocatedItemStack.NONE;
    }

    @Nonnull
    public static ItemStack insert(TileEntity tile, ItemStack itemStack, boolean simulate) {
        ItemStack insertingStack = itemStack.copy();
        for (EnumFacing side : EnumFacing.VALUES) {
            IItemHandler inv = getInventoryForTE(tile, side);
            insertingStack = ItemHandlerHelper.insertItem(inv, insertingStack, simulate);
            if (insertingStack.isEmpty()) return ItemStack.EMPTY;
        }
        return insertingStack;
    }

    @Nonnull
    public static ItemStack insert(TileEntity tile, ItemStack itemStack, EnumFacing side, boolean simulate) {
        IItemHandler inv = getInventoryForTE(tile, side);
        if (inv != null) return ItemHandlerHelper.insertItem(inv, itemStack, simulate);
        return itemStack;
    }

    @Nonnull
    public static ItemStack insert(ICapabilityProvider provider, ItemStack itemStack, EnumFacing side, boolean simulate) {
        if (provider.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
            IItemHandler handler = provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            return ItemHandlerHelper.insertItem(handler, itemStack, simulate);
        }
        return ItemStack.EMPTY;
    }
}
