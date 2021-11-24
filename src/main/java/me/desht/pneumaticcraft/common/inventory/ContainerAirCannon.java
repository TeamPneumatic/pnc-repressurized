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

import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCannon;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;

public class ContainerAirCannon extends ContainerPneumaticBase<TileEntityAirCannon> {

    public ContainerAirCannon(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerAirCannon(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.AIR_CANNON.get(), i, playerInventory, pos);

        addUpgradeSlots(8, 29);

        // add the gps slot
        addSlot(new SlotItemSpecific(te.getPrimaryInventory(), itemStack -> {
            if (!(itemStack.getItem() instanceof IPositionProvider)) return false;
            List<BlockPos> l = ((IPositionProvider) itemStack.getItem()).getStoredPositions(te.getLevel(), itemStack);
            return !l.isEmpty() && l.get(0) != null;
        }, 1, 51, 29));

        // add the cannoned slot.
        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, 79, 40));

        addPlayerSlots(playerInventory, 84);
    }
}
