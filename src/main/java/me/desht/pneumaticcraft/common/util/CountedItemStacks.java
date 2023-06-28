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

import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Objects;

public class CountedItemStacks extends Object2IntOpenCustomHashMap<ItemStack> {
    private final boolean canCoalesce;

    private static class ItemStackHashingStrategy implements Strategy<ItemStack> {
        @Override
        public int hashCode(ItemStack object) {
            return Objects.hash(object.getItem(), object.getTag());
        }

        @Override
        public boolean equals(ItemStack o1, ItemStack o2) {
            return (o1 == o2) || !(o1 == null || o2 == null)
                    && ItemStack.isSameItemSameTags(o1, o2);
        }
    }

    public CountedItemStacks() {
        super(new ItemStackHashingStrategy());
        canCoalesce = false;
    }

    public CountedItemStacks(IItemHandler handler) {
        super(handler.getSlots(), new ItemStackHashingStrategy());

        boolean canCoalesce = false;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                int seenAlready = getInt(stack);
                if (seenAlready > 0 && seenAlready + stack.getCount() <= stack.getMaxStackSize()) {
                    canCoalesce = true;
                }
                put(stack, seenAlready + stack.getCount());
            }
        }
        this.canCoalesce = canCoalesce;
    }

    /**
     * Can the item handler that this collection of stacks came from be coalesced into fewer stacks?
     * @return true if it can be coalesced, false otherwise
     */
    public boolean canCoalesce() {
        return canCoalesce;
    }

    public NonNullList<ItemStack> coalesce() {
        NonNullList<ItemStack> coalesced = NonNullList.create();
        forEach((stack, amount) -> {
            while (amount > 0) {
                int toTake = Math.min(amount, stack.getMaxStackSize());
                amount -= toTake;
                coalesced.add(ItemHandlerHelper.copyStackWithSize(stack, toTake));
            }
        });
        return coalesced;
    }
}
