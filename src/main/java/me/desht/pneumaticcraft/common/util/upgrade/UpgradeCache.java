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

package me.desht.pneumaticcraft.common.util.upgrade;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.core.Direction;
import net.minecraftforge.items.IItemHandler;

import java.util.Arrays;

public class UpgradeCache {
    private final byte[] upgradeCount = new byte[EnumUpgrade.values().length];
    private final IUpgradeHolder holder;
    private boolean isValid = false;
    private Direction ejectDirection;

    public UpgradeCache(IUpgradeHolder holder) {
        this.holder = holder;
    }

    /**
     * Mark the upgrade cache as invalid.  The next query for an upgrade will force a cache rebuild from the upgrade
     * inventory.
     */
    public void invalidate() {
        isValid = false;
    }

    public int getUpgrades(EnumUpgrade type) {
        validate();
        return upgradeCount[type.ordinal()];
    }

    public Direction getEjectDirection() {
        return ejectDirection;
    }

    public void validate() {
        if (isValid) return;

        IItemHandler handler = holder.getUpgradeHandler();

        Arrays.fill(upgradeCount, (byte)0);
        ejectDirection = null;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.getItem() instanceof ItemMachineUpgrade) {
                ItemMachineUpgrade upgrade = ItemMachineUpgrade.of(stack);
                EnumUpgrade type = upgrade.getUpgradeType();
                if (upgradeCount[type.ordinal()] != 0) {
                    Log.warning("found upgrade " + type + " in multiple slots! Ignoring.");
                    continue;
                }
                upgradeCount[type.ordinal()] = (byte)(stack.getCount() * upgrade.getTier());
                handleExtraData(stack, type);
            } else if (!stack.isEmpty()) {
                throw new IllegalStateException("found non-upgrade item in an upgrade handler! " + stack);
            }
        }
        isValid = true;
        holder.onUpgradesChanged();
    }

    private void handleExtraData(ItemStack stack, EnumUpgrade type) {
        if (type == EnumUpgrade.DISPENSER && stack.hasTag()) {
            ejectDirection = Direction.byName(NBTUtils.getString(stack, ItemMachineUpgrade.NBT_DIRECTION));
        }
    }

    public ByteArrayTag toNBT() {
        validate();
        return new ByteArrayTag(upgradeCount);
    }
}
