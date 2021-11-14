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
import me.desht.pneumaticcraft.common.tileentity.TileEntityEtchingTank;
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

public class BlockEtchingTank extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE = Stream.of(
            Block.box(0, 1, 0, 16, 9, 2),
            Block.box(2, 1, 2, 14, 14, 14),
            Block.box(0, 1, 14, 16, 9, 16),
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 9, 0, 2, 14, 2),
            Block.box(14, 9, 0, 16, 14, 2),
            Block.box(14, 9, 14, 16, 14, 16),
            Block.box(0, 9, 14, 2, 14, 16),
            Block.box(0, 14, 14, 16, 16, 16),
            Block.box(0, 14, 0, 16, 16, 2),
            Block.box(14, 14, 2, 16, 16, 14),
            Block.box(0, 14, 2, 2, 16, 14),
            Block.box(5, 13, 5, 11, 16, 11),
            Block.box(14, 1, 2, 16, 9, 14),
            Block.box(0, 1, 2, 2, 9, 14)
    ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();

    public BlockEtchingTank() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityEtchingTank.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return BlockPneumaticCraft.ALMOST_FULL_SHAPE;
    }
}
