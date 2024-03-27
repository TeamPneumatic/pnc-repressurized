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

import me.desht.pneumaticcraft.client.gui.SecurityStationInventoryScreen;
import me.desht.pneumaticcraft.common.block.entity.SecurityStationBlockEntity;
import me.desht.pneumaticcraft.common.inventory.slot.ItemFilteredSlot;
import me.desht.pneumaticcraft.common.item.NetworkComponentItem;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.Predicate;

public class SecurityStationMainMenu extends AbstractPneumaticCraftMenu<SecurityStationBlockEntity> {

    public SecurityStationMainMenu(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public SecurityStationMainMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.SECURITY_STATION_MAIN.get(), windowId, playerInventory, pos);

        //add the network slots
        for (int i = 0; i < SecurityStationBlockEntity.INV_ROWS; i++) {
            for (int j = 0; j < SecurityStationBlockEntity.INV_COLS; j++) {
                addSlot(new SlotSecurityNode(blockEntity.getItemHandler(), SecurityStationMainMenu::isSecStationComponent, j + i * 5, 17 + j * 18, 22 + i * 18));
            }
        }

        addUpgradeSlots(128, 62);

        addPlayerSlots(playerInventory, 157);
    }

    private static boolean isSecStationComponent(ItemStack stack) {
        return NetworkComponentItem.getType(stack).map(NetworkComponentItem.NetworkComponentType::isSecStationComponent).orElse(false);
    }

    private class SlotSecurityNode extends ItemFilteredSlot {
        SlotSecurityNode(IItemHandler handler, Predicate<ItemStack> itemAllowed, int index, int x, int y) {
            super(handler, itemAllowed, index, x, y);
        }

        @Override
        public void setChanged() {
            super.setChanged();

            if (blockEntity != null && blockEntity.nonNullLevel().isClientSide) SecurityStationInventoryScreen.reinitConnectionRendering();
        }
    }
}
