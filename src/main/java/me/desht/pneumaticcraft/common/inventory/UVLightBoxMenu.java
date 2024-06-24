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

import me.desht.pneumaticcraft.common.block.entity.processing.UVLightBoxBlockEntity;
import me.desht.pneumaticcraft.common.inventory.slot.OutputOnlySlot;
import me.desht.pneumaticcraft.common.inventory.slot.UpgradeSlot;
import me.desht.pneumaticcraft.common.item.EmptyPCBItem;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class UVLightBoxMenu extends AbstractPneumaticCraftMenu<UVLightBoxBlockEntity> {

    public UVLightBoxMenu(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public UVLightBoxMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.UV_LIGHT_BOX.get(), windowId, playerInventory, pos);

        addSlot(new SlotPCB(blockEntity.getInputHandler(), 0, 11, 22));
        addSlot(new OutputOnlySlot(blockEntity.getOutputHandler(), 0, 49, 22));

        // add upgrade slots
        for (int i = 0; i < 4; i++) {
            addSlot(new UpgradeSlot(blockEntity, i, 98 + i * 18, 90));
        }

        addPlayerSlots(playerInventory, 114);
    }

    private static class SlotPCB extends SlotItemHandler {
        SlotPCB(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return stack.getItem() instanceof EmptyPCBItem && EmptyPCBItem.getEtchProgress(stack) == 0;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
