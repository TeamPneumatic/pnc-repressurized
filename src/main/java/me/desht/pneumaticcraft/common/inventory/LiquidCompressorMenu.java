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

import me.desht.pneumaticcraft.common.block.entity.compressor.LiquidCompressorBlockEntity;
import me.desht.pneumaticcraft.common.inventory.slot.FluidContainerSlot;
import me.desht.pneumaticcraft.common.inventory.slot.OutputOnlySlot;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public class LiquidCompressorMenu extends AbstractPneumaticCraftMenu<LiquidCompressorBlockEntity> {

    public LiquidCompressorMenu(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(ModMenuTypes.LIQUID_COMPRESSOR.get(), i, playerInventory, getTilePos(buffer));
    }

    public LiquidCompressorMenu(int i, Inventory playerInventory, BlockPos tePos) {
        this(ModMenuTypes.LIQUID_COMPRESSOR.get(), i, playerInventory, tePos);
    }

    LiquidCompressorMenu(MenuType type, int i, Inventory playerInventory, BlockPos pos) {
        super(type, i, playerInventory, pos);

        addUpgradeSlots(11, 29);

        addSlot(new FluidContainerSlot(blockEntity.getItemHandler(), 0, getFluidContainerOffset(), 22));
        addSlot(new OutputOnlySlot(blockEntity.getItemHandler(), 1, getFluidContainerOffset(), 55));

        addPlayerSlots(playerInventory, 84);
    }

    protected int getFluidContainerOffset() {
        return 62;
    }

}
