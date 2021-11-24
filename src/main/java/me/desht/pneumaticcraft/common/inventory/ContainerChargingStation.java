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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerChargingStation extends ContainerPneumaticBase<TileEntityChargingStation> {

    public ContainerChargingStation(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerChargingStation(int i, PlayerInventory inventoryPlayer, BlockPos pos) {
        super(ModContainers.CHARGING_STATION.get(), i, inventoryPlayer, pos);

        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, 91, 45) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        addUpgradeSlots(42, 35);
        addArmorSlots(inventoryPlayer, 8, 25);
        addOffhandSlot(inventoryPlayer,28, 79);
        addPlayerSlots(inventoryPlayer, 101);
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(PlayerEntity player, int slot) {
        Slot srcSlot = slots.get(slot);
        if (srcSlot == null || !srcSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack srcStack = srcSlot.getItem().copy();
        ItemStack copyOfSrcStack = srcStack.copy();

        if (slot == 0 && srcStack.getItem() instanceof ArmorItem) {
            // chargeable slot - move to armor if appropriate, player inv otherwise
            if (!moveItemStackTo(srcStack, 5, 9, false)
                    && !moveItemStackTo(srcStack, playerSlotsStart, playerSlotsStart + 36, false))
                return ItemStack.EMPTY;
        } else if (slot >= 5 && slot < 9 && srcStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).isPresent()) {
            // armor slots - try to move to the charging slot if possible
            if (!moveItemStackTo(srcStack, 0, 1, false)
                    && !moveItemStackTo(srcStack, playerSlotsStart, playerSlotsStart + 36, false))
                return ItemStack.EMPTY;
        } else if (slot < playerSlotsStart) {
            if (!moveItemStackTo(srcStack, playerSlotsStart, playerSlotsStart + 36, false))
                return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(srcStack, 0, playerSlotsStart, false))
                return ItemStack.EMPTY;
        }

        srcSlot.set(srcStack);
        srcSlot.onQuickCraft(srcStack, copyOfSrcStack);
        srcSlot.onTake(player, srcStack);

        return copyOfSrcStack;
    }
}
