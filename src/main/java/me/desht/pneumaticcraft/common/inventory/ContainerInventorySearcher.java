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

import me.desht.pneumaticcraft.common.core.ModContainers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerInventorySearcher extends Container {
    public ContainerInventorySearcher(int windowId, PlayerInventory inv, PacketBuffer data) {
        super(ModContainers.INVENTORY_SEARCHER.get(), windowId);

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
    public ItemStack quickMoveStack(PlayerEntity par1EntityPlayer, int par2) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int par1, @Nonnull ItemStack par2ItemStack) {
        //override this to do nothing, as NEI tries to place items in this container which makes it crash.
    }

    @Override
    public boolean stillValid(@Nonnull PlayerEntity entityplayer) {
        return true;
    }

}
