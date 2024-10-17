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
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * To be implemented on items with an upgrade GUI opened via the "Upgrade" button in the Charging Station GUI.
 */
public interface IChargeableContainerProvider {
    /**
     * Get a container provider for this item when it's being upgraded
     * @param te the charging station that the item is in
     * @return the container provider
     */
    MenuProvider getContainerProvider(ChargingStationBlockEntity te);

    /**
     * Returns a possible item tag, which contains a list of upgrade items which may not be inserted in the upgrade GUI
     * (even if they're otherwise applicable to the item being upgraded). Intended for pack developers to limit what
     * upgrades may be used.
     *
     * @return a possible item tag
     */
    default Optional<TagKey<Item>> getUpgradeBlacklistTag() {
        return Optional.empty();
    }

    /**
     * Convenience class to create a menu provider useful for charging station upgrade screens
     */
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
