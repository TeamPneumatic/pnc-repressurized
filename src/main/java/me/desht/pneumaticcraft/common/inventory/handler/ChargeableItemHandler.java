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
import me.desht.pneumaticcraft.common.item.IChargeableContainerProvider;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.upgrades.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.common.upgrades.SavedUpgrades;
import me.desht.pneumaticcraft.common.upgrades.UpgradableItemUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChargeableItemHandler extends BaseItemStackHandler {
    private boolean inInit = true;

    public ChargeableItemHandler(ChargingStationBlockEntity te) {
        super(te, UpgradableItemUtils.UPGRADE_INV_SIZE);

        readFromChargingStack();

        inInit = false;
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
        if (!inInit) {
            UpgradableItemUtils.setUpgrades(getChargingStack(), this);
        }
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
        Item chargeableItem = getChargingStack().getItem();
        return !isItemBlacklisted(chargeableItem, stack)
                && ApplicableUpgradesDB.getInstance().getMaxUpgrades(chargeableItem, PNCUpgrade.from(stack)) > 0;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isItemBlacklisted(Item item, ItemStack stack) {
        return item instanceof IChargeableContainerProvider p
                && p.getUpgradeBlacklistTag().map(stack::is).orElse(false);
    }
}
