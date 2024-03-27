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
import me.desht.pneumaticcraft.common.block.entity.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ChargingStationMenu extends AbstractPneumaticCraftMenu<ChargingStationBlockEntity> {

    public ChargingStationMenu(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ChargingStationMenu(int i, Inventory inventoryPlayer, BlockPos pos) {
        super(ModMenuTypes.CHARGING_STATION.get(), i, inventoryPlayer, pos);

        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 91, 45) {
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
    public ItemStack quickMoveStack(Player player, int slot) {
        Slot srcSlot = slots.get(slot);
        if (srcSlot == null || !srcSlot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack srcStack = srcSlot.getItem().copy();
        ItemStack copyOfSrcStack = srcStack.copy();

        if (slot == 0 && srcStack.getItem() instanceof ArmorItem) {
            // chargeable slot - move to armor if appropriate, player inv otherwise
            if (!moveItemStackTo(srcStack, 5, 9, false)
                    && !moveItemStackToHotbarOrInventory(srcStack, playerSlotsStart))
                return ItemStack.EMPTY;
        } else if (slot >= 5 && slot < 9 && PNCCapabilities.getAirHandler(srcStack).isPresent()) {
            // armor slots - try to move to the charging slot if possible
            if (!moveItemStackTo(srcStack, 0, 1, false)
                    && !moveItemStackTo(srcStack, playerSlotsStart, playerSlotsStart + 36, false))
                return ItemStack.EMPTY;
        } else if (slot < playerSlotsStart) {
            if (!moveItemStackToHotbarOrInventory(srcStack, playerSlotsStart))
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
