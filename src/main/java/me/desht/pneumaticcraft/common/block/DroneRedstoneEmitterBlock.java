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

package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.DroneRedstoneEmitterBlockEntity;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.Comparator;

public class DroneRedstoneEmitterBlock extends AirBlock implements EntityBlock {
    public DroneRedstoneEmitterBlock() {
        super(Block.Properties.ofFullCopy(Blocks.AIR));
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter par1IBlockAccess, BlockPos pos, Direction side) {
        return 0;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (blockAccess instanceof EntityGetter entityGetter) {
            return entityGetter.getEntitiesOfClass(DroneEntity.class, new AABB(pos)).stream()
                    .map(drone -> drone.getEmittingRedstone(side.getOpposite()))
                    .max(Comparator.naturalOrder())
                    .orElse(0);
        } else {
            return 0;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DroneRedstoneEmitterBlockEntity(pPos, pState);
    }
}