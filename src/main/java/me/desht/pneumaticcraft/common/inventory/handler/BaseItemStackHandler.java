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

package me.desht.pneumaticcraft.common.inventory.handler;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

/**
 * Item stack handler which marks its owning BE as dirty when it changes.
 * Also filters item insertion using the isItemValid() method (which returns true by default).
 */
public class BaseItemStackHandler extends ItemStackHandler {
    protected final BlockEntity te;

    public BaseItemStackHandler(int size) {
        this(null, size);
    }

    public BaseItemStackHandler(BlockEntity te, int size) {
        super(size);
        this.te = te;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (te != null && te.getLevel() != null && !te.getLevel().isClientSide) te.setChanged();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
    }
}
