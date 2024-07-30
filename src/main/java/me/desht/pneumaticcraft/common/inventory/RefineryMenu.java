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

import me.desht.pneumaticcraft.common.block.entity.processing.RefineryControllerBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class RefineryMenu extends AbstractPneumaticCraftMenu<RefineryControllerBlockEntity> {

    public RefineryMenu(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public RefineryMenu(int i, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.REFINERY.get(), i, playerInventory, pos);

        if (playerInventory.player instanceof ServerPlayer) {
            blockEntity.incPlayersUsing();
        }

        addPlayerSlots(playerInventory, 108);
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);

        if (pPlayer instanceof ServerPlayer) {
            blockEntity.decPlayersUsing();
        }
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slotIndex) {
        // Refinery itself has no item slots, but this allows shift-clicking items between player's inventory & hotbar
        ItemStack stack = ItemStack.EMPTY;
        Slot srcSlot = slots.get(slotIndex);

        if (srcSlot != null && srcSlot.hasItem()) {
            ItemStack stackInSlot = srcSlot.getItem();
            stack = stackInSlot.copy();

            if (slotIndex < 27) {
                if (!moveItemStackTo(stackInSlot, 27, 36, false)) return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(stackInSlot, 0, 27, false)) return ItemStack.EMPTY;
            }
            srcSlot.onQuickCraft(stackInSlot, stack);

            if (stackInSlot.isEmpty()) {
                srcSlot.set(ItemStack.EMPTY);
            } else {
                srcSlot.setChanged();
            }

            if (stackInSlot.getCount() == stack.getCount()) return ItemStack.EMPTY;

            srcSlot.onTake(par1EntityPlayer, stackInSlot);
        }

        return stack;
    }
}
