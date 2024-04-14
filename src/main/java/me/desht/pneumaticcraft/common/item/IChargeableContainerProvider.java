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

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.block.entity.utility.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.inventory.ChargingStationUpgradeManagerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nullable;

/**
 * Represents an item with an upgrade GUI openable via the "Upgrade" button in the Charging Station GUI.
 */
public interface IChargeableContainerProvider {
    /**
     * Get a container provider for this item
     * @param te the charging station that the item is in
     * @return the container provider
     */
    MenuProvider getContainerProvider(ChargingStationBlockEntity te);

    class Provider implements MenuProvider {
        private final ChargingStationBlockEntity te;
        private final MenuType<? extends ChargingStationUpgradeManagerMenu> type;

        public Provider(ChargingStationBlockEntity te, MenuType<? extends ChargingStationUpgradeManagerMenu> type) {
            this.te = te;
            this.type = type;
        }

        @Override
        public Component getDisplayName() {
            return te.getChargingStack().getHoverName();
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
            return new ChargingStationUpgradeManagerMenu(type, windowId, playerInventory, te.getBlockPos());
        }
    }
}
