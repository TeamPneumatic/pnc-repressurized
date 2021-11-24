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
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressurizedSpawner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerPressurizedSpawner extends ContainerPneumaticBase<TileEntityPressurizedSpawner> {
    public ContainerPressurizedSpawner(int windowId, PlayerInventory invPlayer, PacketBuffer buffer) {
        this(windowId, invPlayer, getTilePos(buffer));
    }

    public ContainerPressurizedSpawner(int windowId, PlayerInventory invPlayer, BlockPos pos) {
        super(ModContainers.PRESSURIZED_SPAWNER.get(), windowId, invPlayer, pos);

        addSlot(new ContainerVacuumTrap.SlotSpawnerCore(te.getPrimaryInventory(), 0, 62, 38));

        addUpgradeSlots(8, 29);

        addPlayerSlots(invPlayer, 84);
    }
}
