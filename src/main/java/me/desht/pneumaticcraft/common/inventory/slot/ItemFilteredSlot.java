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

package me.desht.pneumaticcraft.common.inventory.slot;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public class ItemFilteredSlot extends SlotItemHandler {
    private final Predicate<ItemStack> itemAllowed;

    public ItemFilteredSlot(IItemHandler handler, Predicate<ItemStack> itemAllowed, int index, int x, int y) {
        super(handler, index, x, y);
        this.itemAllowed = itemAllowed;
    }

    public ItemFilteredSlot(IItemHandler handler, Item item, int index, int x, int y) {
        this(handler, stack -> stack.getItem() == item, index, x, y);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return stack.isEmpty() || itemAllowed.test(stack);
    }
}
