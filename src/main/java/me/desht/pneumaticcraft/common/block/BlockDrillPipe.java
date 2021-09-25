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

import me.desht.pneumaticcraft.common.core.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import java.util.stream.Stream;

public class BlockDrillPipe extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE = Stream.of(
            Block.box(6, 0, 6, 7, 16, 10),
            Block.box(7, 0, 6, 9, 16, 7),
            Block.box(7, 0, 9, 9, 16, 10),
            Block.box(10, 14, 6, 10.25, 18, 10),
            Block.box(5.75, 14, 10, 10.25, 18, 10.25),
            Block.box(5.75, 2, 6, 6, 3, 10),
            Block.box(5.75, 2, 5.75, 10.25, 3, 6),
            Block.box(9, 0, 6, 10, 16, 10),
            Block.box(5.75, 14, 5.75, 10.25, 18, 6),
            Block.box(5.75, 14, 6, 6, 18, 10),
            Block.box(10, 2, 6, 10.25, 3, 10),
            Block.box(5.75, 2, 10, 10.25, 3, 10.25)
    ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();

    public BlockDrillPipe() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }
}
