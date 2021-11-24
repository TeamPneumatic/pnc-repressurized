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
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerLiquidCompressor extends ContainerPneumaticBase<TileEntityLiquidCompressor> {

    public ContainerLiquidCompressor(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(ModContainers.LIQUID_COMPRESSOR.get(), i, playerInventory, getTilePos(buffer));
    }

    public ContainerLiquidCompressor(int i, PlayerInventory playerInventory, BlockPos tePos) {
        this(ModContainers.LIQUID_COMPRESSOR.get(), i, playerInventory, tePos);
    }

    ContainerLiquidCompressor(ContainerType type, int i, PlayerInventory playerInventory, BlockPos pos) {
        super(type, i, playerInventory, pos);

        addUpgradeSlots(11, 29);

        addSlot(new SlotFluidContainer(te.getPrimaryInventory(), 0, getFluidContainerOffset(), 22));
        addSlot(new SlotOutput(te.getPrimaryInventory(), 1, getFluidContainerOffset(), 55));

        addPlayerSlots(playerInventory, 84);
    }

    protected int getFluidContainerOffset() {
        return 62;
    }

}
