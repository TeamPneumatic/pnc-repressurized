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

import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class InventorySearcherMenu extends AbstractContainerMenu {
    public InventorySearcherMenu(int windowId, Inventory inv, FriendlyByteBuf data) {
        super(ModMenuTypes.INVENTORY_SEARCHER.get(), windowId);

        // Add the player's inventory slots to the container
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 48 + row * 18));
            }
        }

        // Add the player's action bar slots to the container
        for (int slot = 0; slot < 9; ++slot) {
            addSlot(new Slot(inv, slot, 8 + slot * 18, 106));
        }
    }

    public void init(IItemHandler inv) {
        addSlot(new SlotItemHandler(inv, 0, 80, 23));
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    @Nonnull
    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int par2) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int par1, int state, @Nonnull ItemStack par2ItemStack) {
        //override this to do nothing, as NEI tries to place items in this container which makes it crash.
    }

    @Override
    public boolean stillValid(@Nonnull Player entityplayer) {
        return true;
    }

}
