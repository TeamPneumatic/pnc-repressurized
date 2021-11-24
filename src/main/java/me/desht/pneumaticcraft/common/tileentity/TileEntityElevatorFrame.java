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

package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

import java.lang.ref.WeakReference;

public class TileEntityElevatorFrame extends TileEntityBase {
    private WeakReference<TileEntityElevatorBase> baseRef = null;

    //TODO redo elevator frames

    public TileEntityElevatorFrame() {
        super(ModTileEntities.ELEVATOR_FRAME.get());
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    public TileEntityElevatorBase getElevatorBase() {
        if (baseRef == null || baseRef.get() == null) {
            TileEntityElevatorBase base = findElevatorBase();
            baseRef = new WeakReference<>(base);
        }
        return baseRef.get();
    }

    private TileEntityElevatorBase findElevatorBase() {
        BlockPos.Mutable pos1 = new BlockPos.Mutable();
        pos1.set(worldPosition);
        while (true) {
            pos1.move(Direction.DOWN);
            if (level.getBlockState(pos1).getBlock() == ModBlocks.ELEVATOR_BASE.get()) {
                return (TileEntityElevatorBase) level.getBlockEntity(pos1);
            } else if (level.getBlockState(pos1).getBlock() != ModBlocks.ELEVATOR_FRAME.get() || pos1.getY() <= 0) {
                return null;
            }
        }
    }
}
