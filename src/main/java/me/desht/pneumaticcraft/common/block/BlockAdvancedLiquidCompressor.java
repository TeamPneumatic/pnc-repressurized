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
import me.desht.pneumaticcraft.common.tileentity.TileEntityAdvancedLiquidCompressor;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import java.util.stream.Stream;

public class BlockAdvancedLiquidCompressor extends BlockPneumaticCraft {

    private static final VoxelShape SHAPE_N = Stream.of(
            Block.box(15, 0, 0, 16, 10, 1),
            Block.box(0, 10, 4, 16, 16, 12),
            Block.box(15, 0, 15, 16, 10, 16),
            Block.box(15, 0, 1, 16, 1, 15),
            Block.box(0, 0, 0, 1, 10, 1),
            Block.box(0, 0, 15, 1, 10, 16),
            Block.box(0, 0, 1, 1, 1, 15),
            Block.box(3, 1, 1, 13, 10, 15),
            Block.box(1, 0, 10, 15, 1, 14),
            Block.box(1, 0, 2, 15, 1, 6),
            Block.box(5, 5, 15, 11, 10, 16),
            Block.box(5, 5, 0, 11, 10, 1),
            Block.box(13, 6, 2, 16, 10, 10),
            Block.box(1, 6, 4, 2, 10, 5),
            Block.box(2, 6, 4, 3, 7, 5),
            Block.box(1, 4, 7, 2, 10, 8),
            Block.box(2, 4, 7, 3, 5, 8),
            Block.box(14, 7, 10, 15, 8, 13),
            Block.box(13.5, 6.5, 9.25, 15.5, 8.5, 10.25),
            Block.box(14, 8, 12, 15, 10, 13),
            Block.box(13.5, 9.75, 11.5, 15.5, 10.75, 13.5),
            Block.box(0, 10, 0, 16, 11, 4),
            Block.box(0, 10, 12, 16, 11, 16),
            Block.box(0, 11, 0, 16, 16, 4),
            Block.box(0, 11, 12, 16, 16, 16),
            Block.box(0.5, 9.75, 6.5, 2.5, 10.75, 8.5),
            Block.box(0.5, 9.75, 3.5, 2.5, 10.75, 5.5),
            Block.box(2.75, 3.5, 6.5, 3.75, 5.5, 8.5),
            Block.box(2.75, 5.5, 3.5, 3.75, 7.5, 5.5)
    ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();

    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public BlockAdvancedLiquidCompressor() {
        super(ModBlocks.defaultProps().noOcclusion());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAdvancedLiquidCompressor.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }
}
