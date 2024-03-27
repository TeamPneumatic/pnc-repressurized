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

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.ref.WeakReference;

public class ElevatorFrameBlockEntity extends AbstractPneumaticCraftBlockEntity {
    private WeakReference<ElevatorBaseBlockEntity> baseRef = null;

    public ElevatorFrameBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ELEVATOR_FRAME.get(), pos, state);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    public ElevatorBaseBlockEntity getElevatorBase() {
        if (baseRef == null || baseRef.get() == null) {
            ElevatorBaseBlockEntity base = findElevatorBase();
            baseRef = new WeakReference<>(base);
        }
        return baseRef.get();
    }

    private ElevatorBaseBlockEntity findElevatorBase() {
        BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos();
        pos1.set(worldPosition);
        Level level = nonNullLevel();
        while (true) {
            pos1.move(Direction.DOWN);
            if (level.getBlockState(pos1).getBlock() == ModBlocks.ELEVATOR_BASE.get()) {
                return (ElevatorBaseBlockEntity) level.getBlockEntity(pos1);
            } else if (level.getBlockState(pos1).getBlock() != ModBlocks.ELEVATOR_FRAME.get() || pos1.getY() <= level.getMinBuildHeight()) {
                return null;
            }
        }
    }
}
