package me.desht.pneumaticcraft.common.util;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.List;

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
    public enum ExtractCount {
        /**
         * Extract exactly the items specified, including amount.  If the exact number isn't available, extract nothing.
         */
        EXACT,
        /**
         * Extract the first matching ItemStack in the inventory, but not more than the given amount.
         */
        FIRST_MATCHING,
        /**
         * Extract up to the number of items specified but not more.
         */
        UP_TO
    }

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
     * Extract a specific number of the given item from the given item handler
     *
     * @param handler the item handler
     * @param requestedStack the item to search for, including the number of items
     * @param countType how to interpret the item count of requestedStack
     * @param simulate true if extraction should only be simulated
     * @param fuzzyMeta true if item meta should be ignored when searching
     *
     * @return the extracted item stack, or ItemStack.EMPTY if nothing was extracted
     */
    public static ItemStack extract(IItemHandler handler, ItemStack requestedStack, ExtractCount countType, boolean simulate, boolean fuzzyMeta) {
        if (requestedStack.isEmpty()) return requestedStack;

        if (handler != null) {
            int itemsFound = 0;
            List<Integer> slotsOfInterest = Lists.newArrayList();
            for (int slot = 0; slot < handler.getSlots() && itemsFound < requestedStack.getCount(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty() && matchStacks(stack, requestedStack, fuzzyMeta)) {
                    if (countType == ExtractCount.FIRST_MATCHING) {
                        return handler.extractItem(slot, Math.min(requestedStack.getCount(), stack.getCount()), simulate);
                    }
                    itemsFound += stack.getCount();
                    slotsOfInterest.add(slot);
                }
            }
            if (countType == ExtractCount.UP_TO || itemsFound >= requestedStack.getCount()) {
                ItemStack exportedStack = ItemStack.EMPTY;
                int itemsNeeded = requestedStack.getCount();
                int totalExtracted = 0;
                for (int slot : slotsOfInterest) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (matchStacks(stack, requestedStack, fuzzyMeta)) {
                        int itemsSubtracted = Math.min(itemsNeeded, stack.getCount());
                        if (itemsSubtracted > 0) {
                            exportedStack = stack;
                        }
                        itemsNeeded -= itemsSubtracted;
                        ItemStack extracted = handler.extractItem(slot, itemsSubtracted, simulate);
                        totalExtracted += extracted.getCount();
                    }
                }
                exportedStack = exportedStack.copy();
                exportedStack.setCount(totalExtracted);
                return exportedStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean matchStacks(ItemStack stack1, ItemStack stack2, boolean fuzzyMeta) {
        return fuzzyMeta ? ItemStack.areItemsEqualIgnoreDurability(stack1, stack2) : ItemStack.areItemsEqual(stack1, stack2);
    }

    @Nonnull
    public static LocatedItemStack extractOneItem(IItemHandler handler, int startSlot, boolean simulate) {
        if (handler != null) {
            for (int n = 0; n < handler.getSlots(); n++) {
                int slot = startSlot + n;
                if (slot >= handler.getSlots()) slot %= handler.getSlots();
                ItemStack ret = handler.extractItem(slot, 1, simulate);
                if (!ret.isEmpty()) {
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
    
    /**
     * Returns true if succeeded
     * @param input
     * @param output
     * @return
     */
    public static boolean transferOneItem(IItemHandler input, IItemHandler output){
        if(input == null || output == null) return false;
        
        for(int i = 0; i < input.getSlots(); i++){
            ItemStack extracted = input.extractItem(i, 1, true);
            if(!extracted.isEmpty()){
                if(ItemHandlerHelper.insertItemStacked(output, extracted, false).isEmpty()){
                    input.extractItem(i, 1, false);
                    return true;
                }
            }
        }
        
        return false;
    }
}
