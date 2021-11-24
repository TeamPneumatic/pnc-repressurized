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

import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationUpgradeManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;

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
    INamedContainerProvider getContainerProvider(TileEntityChargingStation te);

    class Provider implements INamedContainerProvider {
        private final TileEntityChargingStation te;
        private final ContainerType<? extends ContainerChargingStationUpgradeManager> type;

        public Provider(TileEntityChargingStation te, ContainerType<? extends ContainerChargingStationUpgradeManager> type) {
            this.te = te;
            this.type = type;
        }

        @Override
        public ITextComponent getDisplayName() {
            return te.getChargingStack().getHoverName();
        }

        @Nullable
        @Override
        public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
            return new ContainerChargingStationUpgradeManager(type, windowId, playerInventory, te.getBlockPos());
        }
    }
}
