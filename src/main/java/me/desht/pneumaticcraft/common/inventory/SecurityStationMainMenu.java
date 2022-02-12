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

import me.desht.pneumaticcraft.client.gui.GuiSecurityStationInventory;
import me.desht.pneumaticcraft.common.core.ModMenuTypes;
import me.desht.pneumaticcraft.common.inventory.slot.SlotItemSpecific;
import me.desht.pneumaticcraft.common.item.ItemNetworkComponent;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Predicate;

public class SecurityStationMainMenu extends AbstractPneumaticCraftMenu<TileEntitySecurityStation> {

    public SecurityStationMainMenu(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public SecurityStationMainMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.SECURITY_STATION_MAIN.get(), windowId, playerInventory, pos);

        //add the network slots
        for (int i = 0; i < TileEntitySecurityStation.INV_ROWS; i++) {
            for (int j = 0; j < TileEntitySecurityStation.INV_COLS; j++) {
                addSlot(new SlotSecurityNode(te.getPrimaryInventory(), stack -> stack.getItem() instanceof ItemNetworkComponent, j + i * 5, 17 + j * 18, 22 + i * 18));
            }
        }

        addUpgradeSlots(128, 62);

        addPlayerSlots(playerInventory, 157);
    }

    private class SlotSecurityNode extends SlotItemSpecific {
        SlotSecurityNode(IItemHandler handler, Predicate<ItemStack> itemAllowed, int index, int x, int y) {
            super(handler, itemAllowed, index, x, y);
        }

        @Override
        public void setChanged() {
            super.setChanged();

            if (te != null && te.nonNullLevel().isClientSide) GuiSecurityStationInventory.reinitConnectionRendering();
        }
    }
}
