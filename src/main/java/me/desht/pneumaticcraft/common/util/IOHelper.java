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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.Optional;
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

    public static Optional<IItemHandler> getInventoryForBlock(BlockEntity te, Direction facing) {
        return getCap(te, Capabilities.ItemHandler.BLOCK, facing);
    }

    public static Optional<IItemHandler> getInventoryForBlock(BlockEntity te) {
        return getInventoryForBlock(te, null);
    }

    public static Optional<IItemHandler> getInventoryForEntity(Entity entity, Direction dir) {
        return Optional.ofNullable(entity.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, dir));
    }

    public static Optional<IFluidHandler> getFluidHandlerForBlock(BlockEntity te, Direction facing) {
        return getCap(te, Capabilities.FluidHandler.BLOCK, facing);
    }

    public static Optional<IFluidHandler> getFluidHandlerForBlock(BlockEntity te) {
        return getFluidHandlerForBlock(te, null);
    }

    public static Optional<IFluidHandlerItem> getFluidHandlerForItem(ItemStack stack) {
        return Optional.ofNullable(stack.getCapability(Capabilities.FluidHandler.ITEM));
    }

    public static Optional<IFluidHandler> getFluidHandlerForEntity(Entity entity, Direction dir) {
        return Optional.ofNullable(entity.getCapability(Capabilities.FluidHandler.ENTITY, dir));
    }

    public static Optional<IEnergyStorage> getEnergyStorageForBlock(BlockEntity te, Direction side) {
        return getCap(te, Capabilities.EnergyStorage.BLOCK, side);
    }

    public static Optional<IEnergyStorage> getEnergyStorageForBlock(BlockEntity te) {
        return getCap(te, Capabilities.EnergyStorage.BLOCK, null);
    }

    public static Optional<IEnergyStorage> getEnergyStorageForItem(ItemStack item) {
        return Optional.ofNullable(item.getCapability(Capabilities.EnergyStorage.ITEM));
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
        return matchNBT ? ItemStack.isSameItemSameComponents(stack1, stack2) : ItemStack.isSameItem(stack1, stack2);
    }

    @Nonnull
    public static ItemStack insert(BlockEntity tile, ItemStack itemStack, boolean simulate) {
        for (Direction side : Direction.values()) {
            ItemStack inserted = getInventoryForBlock(tile, side)
                    .map(handler -> ItemHandlerHelper.insertItem(handler, itemStack.copy(), simulate))
                    .orElse(ItemStack.EMPTY);
            if (inserted.getCount() < itemStack.getCount()) return inserted;
        }
        return itemStack;
    }

    @Nonnull
    public static ItemStack insert(BlockEntity tile, ItemStack itemStack, Direction side, boolean simulate) {
        return getInventoryForBlock(tile, side).map(handler -> ItemHandlerHelper.insertItem(handler, itemStack, simulate)).orElse(itemStack);
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
    public static int countItems(IItemHandler cap, Predicate<ItemStack> pred) {
        return IntStream.range(0, cap.getSlots())
                .filter(i -> pred.test(cap.getStackInSlot(i)))
                .map(i -> cap.getStackInSlot(i).getCount())
                .sum();
    }



    public static <T> Optional<T> getCap(BlockEntity te, BlockCapability<T,Direction> cap, Direction face) {
        return te == null || te.getLevel() == null ?
                Optional.empty() :
                Optional.ofNullable(te.getLevel().getCapability(cap, te.getBlockPos(), te.getBlockState(), te, face));
    }

    public static <T> Optional<T> getCap(ItemStack stack, ItemCapability<T, Void> cap) {
        return Optional.ofNullable(stack.getCapability(cap));
    }

    public static <T> Optional<T> getCapV(Entity entity, EntityCapability<T, Void> cap) {
        return Optional.ofNullable(entity.getCapability(cap, null));
    }

    public static <T> Optional<T> getCap(Entity entity, EntityCapability<T, Direction> cap) {
        return Optional.ofNullable(entity.getCapability(cap, null));
    }
}
