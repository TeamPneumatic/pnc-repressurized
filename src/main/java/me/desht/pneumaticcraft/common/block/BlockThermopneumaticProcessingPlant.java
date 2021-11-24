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
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BlockThermopneumaticProcessingPlant extends BlockPneumaticCraft {
    private static final VoxelShape TANK_IN1 = Block.box(0, 0, 9, 7, 12, 16);
    private static final VoxelShape TANK_IN2 = Block.box(9, 0, 9, 16, 12, 16);
    private static final VoxelShape TANK_OUT = Block.box(5, 0, 0, 11, 8, 6);
    private static final VoxelShape SHAPE_N = VoxelShapes.or(TANK_IN1, TANK_IN2, TANK_OUT);
    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_N, 180);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_N, 270);
    private static final VoxelShape[] SHAPES = { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public BlockThermopneumaticProcessingPlant() {
        super(ModBlocks.defaultProps().noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES[state.getValue(directionProperty()).get2DDataValue()];
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityThermopneumaticProcessingPlant.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }
}
