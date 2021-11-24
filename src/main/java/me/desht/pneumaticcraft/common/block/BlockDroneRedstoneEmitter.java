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

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.tileentity.TileEntityDroneRedstoneEmitter;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEntityReader;

import javax.annotation.Nullable;
import java.util.List;

public class BlockDroneRedstoneEmitter extends AirBlock {
    public BlockDroneRedstoneEmitter() {
        super(Block.Properties.copy(Blocks.AIR));
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState state, IBlockReader par1IBlockAccess, BlockPos pos, Direction side) {
        return 0;
    }

    @Override
    public int getSignal(BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) {
        if (blockAccess instanceof IEntityReader) {
            IEntityReader world = (IEntityReader) blockAccess;
            List<EntityDrone> drones = world.getEntitiesOfClass(EntityDrone.class, new AxisAlignedBB(pos, pos.offset(1, 1, 1)));
            int signal = 0;
            for (EntityDrone drone : drones) {
                signal = Math.max(signal, drone.getEmittingRedstone(side.getOpposite()));
            }
            return signal;
        } else {
            return 0;
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntityDroneRedstoneEmitter();
    }
}