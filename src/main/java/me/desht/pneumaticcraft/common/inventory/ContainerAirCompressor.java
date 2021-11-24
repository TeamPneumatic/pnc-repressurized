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
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerAirCompressor extends ContainerPneumaticBase<TileEntityAirCompressor> {

    public ContainerAirCompressor(int windowId, PlayerInventory invPlayer, PacketBuffer extra) {
        this(ModContainers.AIR_COMPRESSOR.get(), windowId, invPlayer, getTilePos(extra));
    }

    public ContainerAirCompressor(int windowId, PlayerInventory invPlayer, BlockPos tePos) {
        this(ModContainers.AIR_COMPRESSOR.get(), windowId, invPlayer, tePos);
    }

    ContainerAirCompressor(ContainerType type, int windowId, PlayerInventory invPlayer, BlockPos tePos) {
        super(type, windowId, invPlayer, tePos);

        // Add the burn slot
        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, getFuelSlotXOffset(), 54));

        addUpgradeSlots(23, 29);
        addPlayerSlots(invPlayer, 84);
    }

    protected int getFuelSlotXOffset() {
        return 80;
    }

}
