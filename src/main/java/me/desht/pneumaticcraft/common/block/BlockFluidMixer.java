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
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidMixer;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BlockFluidMixer extends BlockPneumaticCraft {
    private static final VoxelShape INPUT1 = box(10, 0, 1, 16,8, 7);
    private static final VoxelShape INPUT2 = box(0, 0, 1, 6,8, 7);
    private static final VoxelShape MAIN = box(2, 0, 7, 14,8, 15);
    private static final VoxelShape OUTPUT = box(5, 8, 8, 11,16, 14);

    private static final VoxelShape[] SHAPES = new VoxelShape[4];
    static {
        // S, W, N, E
        SHAPES[2] = VoxelShapes.or(INPUT1, INPUT2, MAIN, OUTPUT);
        SHAPES[1] = VoxelShapeUtils.rotateY(SHAPES[2], 270);
        SHAPES[0] = VoxelShapeUtils.rotateY(SHAPES[2], 180);
        SHAPES[3] = VoxelShapeUtils.rotateY(SHAPES[2], 90);
    }

    public BlockFluidMixer() {
        super(ModBlocks.defaultProps());
        registerDefaultState(getStateDefinition().any()
                .setValue(BlockPneumaticCraft.NORTH, false)
                .setValue(BlockPneumaticCraft.SOUTH, false)
                .setValue(BlockPneumaticCraft.WEST, false)
                .setValue(BlockPneumaticCraft.EAST, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockPneumaticCraft.NORTH, BlockPneumaticCraft.SOUTH, BlockPneumaticCraft.WEST, BlockPneumaticCraft.EAST);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityFluidMixer.class;
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

    @Override
    protected boolean reversePlacementRotation() {
        return true;
    }
}
