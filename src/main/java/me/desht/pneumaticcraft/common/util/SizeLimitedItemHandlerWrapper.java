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

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;

/**
 * An item handler wrapper which pretends to be smaller than its wrapped item handler; only as large as its last
 * non-empty slot.  Useful for tile entities with a large recipe list which might need to search the same mostly
 * empty inventory multiple times (once for each recipe).
 */
public class SizeLimitedItemHandlerWrapper implements IItemHandler {
    private final IItemHandler wrapped;
    private final int size;

    public SizeLimitedItemHandlerWrapper(IItemHandler wrapped) {
        this.wrapped = wrapped;
        this.size = findLastNonEmptySlot(wrapped) + 1;
    }

    private int findLastNonEmptySlot(IItemHandler h) {
        for (int i = h.getSlots() - 1; i >= 0; i--) {
            if (!h.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSlots() {
        return size;
    }

    private void validateSlot(int slot) {
        if (slot >= size) throw new IndexOutOfBoundsException("slot " + slot + " should be less than " + size);
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlot(slot);
        return wrapped.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        validateSlot(slot);
        return wrapped.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlot(slot);
        return wrapped.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlot(slot);
        return wrapped.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        validateSlot(slot);
        return wrapped.isItemValid(slot, stack);
    }
}
