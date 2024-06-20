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

package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.block.entity.utility.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.inventory.handler.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.upgrades.ApplicableUpgradesDB;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ChargingStationUpgradeManagerMenu extends AbstractPneumaticCraftMenu<ChargingStationBlockEntity> {

    private ChargingStationUpgradeManagerMenu(MenuType type, int windowId, Inventory inv, FriendlyByteBuf data) {
        this(type, windowId, inv, getTilePos(data));
    }

    public ChargingStationUpgradeManagerMenu(MenuType type, int windowId, Inventory inventoryPlayer, BlockPos pos) {
        super(type, windowId, inventoryPlayer, pos);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addSlot(new UpgradeSlot(blockEntity, i * 3 + j, 44 + j * 18, 34 + i * 18));
            }
        }

        addPlayerSlots(inventoryPlayer, 100);
    }

    public static ChargingStationUpgradeManagerMenu createMinigunContainer(int windowId, Inventory inv, FriendlyByteBuf data) {
        return new ChargingStationUpgradeManagerMenu(ModMenuTypes.CHARGING_MINIGUN.get(), windowId, inv, data);
    }

    public static ChargingStationUpgradeManagerMenu createDroneContainer(int windowId, Inventory inv, FriendlyByteBuf data) {
        return new ChargingStationUpgradeManagerMenu(ModMenuTypes.CHARGING_DRONE.get(), windowId, inv, data);
    }

    public static ChargingStationUpgradeManagerMenu createArmorContainer(int windowId, Inventory inv, FriendlyByteBuf data) {
        return new ChargingStationUpgradeManagerMenu(ModMenuTypes.CHARGING_ARMOR.get(), windowId, inv, data);
    }

    public static ChargingStationUpgradeManagerMenu createJackhammerContainer(int windowId, Inventory inv, FriendlyByteBuf data) {
        return new ChargingStationUpgradeManagerMenu(ModMenuTypes.CHARGING_JACKHAMMER.get(), windowId, inv, data);
    }

    public static ChargingStationUpgradeManagerMenu createAmadronContainer(int windowId, Inventory inv, FriendlyByteBuf data) {
        return new ChargingStationUpgradeManagerMenu(ModMenuTypes.CHARGING_AMADRON.get(), windowId, inv, data);
    }

    private class UpgradeSlot extends SlotItemHandler {
        UpgradeSlot(ChargingStationBlockEntity te, int slotIndex, int posX, int posY) {
            super(te.getChargeableInventory(), slotIndex, posX, posY);
        }

        @Override
        public int getMaxStackSize(@Nonnull ItemStack stack) {
            return ApplicableUpgradesDB.getInstance().getMaxUpgrades(blockEntity.getChargingStack().getItem(), PNCUpgrade.from(stack));
        }

        @Override
        public void setChanged() {
            super.setChanged();
            ((ChargeableItemHandler) getItemHandler()).writeToChargingStack();
        }
    }
}
