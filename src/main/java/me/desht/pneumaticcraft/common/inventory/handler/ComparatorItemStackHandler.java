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

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * An ItemStackHandler which also supports comparator signal level calculation.
 * Smart enough to only recalculate the signal when the contents have changed.
 */
public class ComparatorItemStackHandler extends BaseItemStackHandler {
    private int signalLevel = -1;  // -1 indicates recalc needed

    public ComparatorItemStackHandler(BlockEntity te, int invSize) {
        super(te, invSize);
    }

    @Override
    protected void onContentsChanged(int slot) {
        signalLevel = -1;
        super.onContentsChanged(slot);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        super.deserializeNBT(provider, nbt);
        signalLevel = -1;
    }

    /**
     * Get the comparator level for this inventory.  Follows the same rules as Container#calcRedstoneLevel()
     *
     * @return a redstone signal level based on the inventory fullness
     */
    public int getComparatorValue() {
        if (signalLevel < 0) {
            signalLevel = ItemHandlerHelper.calcRedstoneFromInventory(this);
        }
        return signalLevel;
    }

    /**
     * Force a recalculation of the comparator level.  Recalculation will be done the next time
     * getComparatorValue() is called.  May be necessary to call this if the inventory has been changed
     * indirectly, e.g. by modifying an ItemStack returned from getStackInSlot().
     */
    public void invalidateComparatorValue() {
        signalLevel = -1;
    }
}
