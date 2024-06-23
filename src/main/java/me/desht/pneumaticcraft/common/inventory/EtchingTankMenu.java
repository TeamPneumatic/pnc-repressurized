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

import me.desht.pneumaticcraft.common.block.entity.EtchingTankBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.UVLightBoxBlockEntity;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModMenuTypes;
import me.desht.pneumaticcraft.common.inventory.slot.OutputOnlySlot;
import me.desht.pneumaticcraft.common.item.EmptyPCBItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class EtchingTankMenu extends AbstractPneumaticCraftMenu<EtchingTankBlockEntity> {
    public EtchingTankMenu(int windowId, Inventory playerInv, BlockPos pos) {
        super(ModMenuTypes.ETCHING_TANK.get(), windowId, playerInv, pos);

        for (int i = 0; i < EtchingTankBlockEntity.ETCHING_SLOTS; i++) {
            int x = 8 + 18 * (i % 5);
            int y = 18 + 18 * (i / 5);
            addSlot(new SlotPCB(te.getPrimaryInventory(), i, x, y));
        }

        addSlot(new OutputOnlySlot(te.getOutputHandler(), 0, 104, 18));
        addSlot(new OutputOnlySlot(te.getFailedHandler(), 0, 104, 90));

        addPlayerSlots(playerInv, 125);

    }

    public EtchingTankMenu(int windowId, Inventory playerInv, FriendlyByteBuf buffer) {
        this(windowId, playerInv, getTilePos(buffer));
    }

    private static class SlotPCB extends SlotItemHandler {
        SlotPCB(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return stack.getItem() instanceof EmptyPCBItem && UVLightBoxBlockEntity.getExposureProgress(stack) > 0;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
