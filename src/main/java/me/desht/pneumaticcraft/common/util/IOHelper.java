/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.function.Predicate;
import java.util.stream.IntStream;

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
 * @author desht
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

    public static LazyOptional<IItemHandler> getInventoryForTE(BlockEntity te, Direction facing) {
        return te == null ? LazyOptional.empty() : te.getCapability(ForgeCapabilities.ITEM_HANDLER, facing);
    }

    public static LazyOptional<IItemHandler> getInventoryForTE(BlockEntity te) {
        return getInventoryForTE(te, null);
    }

    public static LazyOptional<IFluidHandler> getFluidHandlerForTE(BlockEntity te, Direction facing) {
        return te == null ? LazyOptional.empty() : te.getCapability(ForgeCapabilities.FLUID_HANDLER, facing);
    }

    public static LazyOptional<IFluidHandler> getFluidHandlerForTE(BlockEntity te) {
        return getFluidHandlerForTE(te, null);
    }

    /**
     * Extract a specific number of the given item from the given item handler
     *
     * @param handler the item handler
     * @param requestedStack the item to search for, including the number of items; this stack is not modified
     * @param countType how to interpret the item count of requestedStack
     * @param simulate true if extraction should only be simulated
     * @param matchNBT if true, require an exact match of item NBT
     *
     * @return the extracted item stack, or ItemStack.EMPTY if nothing was extracted
     */
    public static ItemStack extract(IItemHandler handler, ItemStack requestedStack, ExtractCount countType, boolean simulate, boolean matchNBT) {
        if (requestedStack.isEmpty()) return requestedStack;

        if (handler != null) {
            int itemsFound = 0;
            IntList slotsOfInterest = new IntArrayList();
            for (int slot = 0; slot < handler.getSlots() && itemsFound < requestedStack.getCount(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty() && matchStacks(stack, requestedStack, matchNBT)) {
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
                    if (matchStacks(stack, requestedStack, matchNBT)) {
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

    private static boolean matchStacks(ItemStack stack1, ItemStack stack2, boolean matchNBT) {
        return ItemStack.isSame(stack1, stack2) && (!matchNBT || ItemStack.tagMatches(stack1, stack2));
    }

    @Nonnull
    public static ItemStack insert(BlockEntity tile, ItemStack itemStack, boolean simulate) {
        for (Direction side : Direction.values()) {
            ItemStack inserted = getInventoryForTE(tile, side)
                    .map(handler -> ItemHandlerHelper.insertItem(handler, itemStack.copy(), simulate))
                    .orElse(ItemStack.EMPTY);
            if (inserted.getCount() < itemStack.getCount()) return inserted;
        }
        return itemStack;
    }

    @Nonnull
    public static ItemStack insert(BlockEntity tile, ItemStack itemStack, Direction side, boolean simulate) {
        return getInventoryForTE(tile, side).map(handler -> ItemHandlerHelper.insertItem(handler, itemStack, simulate)).orElse(itemStack);
    }

    @Nonnull
    public static ItemStack insert(ICapabilityProvider provider, ItemStack itemStack, Direction side, boolean simulate) {
        return provider.getCapability(ForgeCapabilities.ITEM_HANDLER, side)
                .map(handler -> ItemHandlerHelper.insertItem(handler, itemStack, simulate))
                .orElse(itemStack);
    }

    /**
     * Try to transfer a single item between two item handlers
     *
     * @param input the input handler
     * @param output the output handler
     * @return true if an item was transferred
     */
    public static boolean transferOneItem(IItemHandler input, IItemHandler output) {
        if (input == null || output == null) return false;

        for (int i = 0; i < input.getSlots(); i++) {
            ItemStack extracted = input.extractItem(i, 1, true);
            if (!extracted.isEmpty()) {
                if (ItemHandlerHelper.insertItemStacked(output, extracted, false).isEmpty()) {
                    input.extractItem(i, 1, false);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Insert an item into an item handler, dropping any excess in the world
     * @param world the world
     * @param stack the stack to insert
     * @param handler the item handler
     * @param dropPos position to drop excess items at
     * @param simulate true if only simulating (excess will not be dropped)
     */
    public static void insertOrDrop(Level world, ItemStack stack, IItemHandler handler, Vec3 dropPos, boolean simulate) {
        ItemStack remainder = ItemHandlerHelper.insertItem(handler, stack, simulate);
        if (!remainder.isEmpty() && !simulate) {
            ItemEntity item = new ItemEntity(world, dropPos.x(), dropPos.y(), dropPos.z(), remainder);
            world.addFreshEntity(item);
        }
    }

    /**
     * Count the number of items in the given handler which match the given predicate
     * @param cap the item handler capability
     * @param pred a matching predicate
     * @return the number of matching items
     */
    public static int countItems(LazyOptional<IItemHandler> cap, Predicate<ItemStack> pred) {
        return cap.map(handler -> IntStream.range(0, handler.getSlots())
                .mapToObj(handler::getStackInSlot)
                .filter(pred)
                .mapToInt(ItemStack::getCount)
                .sum())
                .orElse(0);
    }
}
