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

import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.block.entity.utility.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.upgrades.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.common.upgrades.SavedUpgrades;
import me.desht.pneumaticcraft.common.upgrades.UpgradableItemUtils;
import net.minecraft.world.item.ItemStack;

public class ChargeableItemHandler extends BaseItemStackHandler {
    public ChargeableItemHandler(ChargingStationBlockEntity te) {
        super(te, UpgradableItemUtils.UPGRADE_INV_SIZE);

        if (!getChargingStack().has(ModDataComponents.ITEM_UPGRADES)) {
            writeToChargingStack();
        }
        readFromChargingStack();
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        writeToChargingStack();
    }

    private ItemStack getChargingStack() {
        return ((ChargingStationBlockEntity) te).getChargingStack();
    }

    public void writeToChargingStack() {
        UpgradableItemUtils.setUpgrades(getChargingStack(), this);
    }

    private void readFromChargingStack() {
        getChargingStack().getOrDefault(ModDataComponents.ITEM_UPGRADES, SavedUpgrades.EMPTY)
                        .fillItemHandler(this);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack itemStack) {
        return itemStack.isEmpty() || isApplicable(itemStack) && isUnique(slot, itemStack);
    }

    private boolean isUnique(int slot, ItemStack stack) {
        for (int i = 0; i < getSlots(); i++) {
            if (i != slot && PNCUpgrade.from(stack) == PNCUpgrade.from(getStackInSlot(i))) return false;
        }
        return true;
    }

    private boolean isApplicable(ItemStack stack) {
        return ApplicableUpgradesDB.getInstance().getMaxUpgrades(getChargingStack().getItem(), PNCUpgrade.from(stack)) > 0;
    }
}
