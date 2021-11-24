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
import me.desht.pneumaticcraft.common.tileentity.TileEntityGasLift;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BlockGasLift extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE1 = Block.box(0, 0, 0, 16, 2, 16);
    private static final VoxelShape SHAPE2 = Block.box(2, 2, 2, 14, 4, 14);
    private static final VoxelShape SHAPE3 = Block.box(4, 4, 4, 12, 6, 12);
    private static final VoxelShape SHAPE4 = Block.box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape SHAPE = VoxelShapes.or(SHAPE1, VoxelShapes.or(SHAPE2, VoxelShapes.or(SHAPE3, SHAPE4)));

    public BlockGasLift() {
        super(ModBlocks.defaultProps());

        registerDefaultState(getStateDefinition().any()
                .setValue(UP, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(EAST, false)
        );
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext selectionContext) {
        return SHAPE;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityGasLift.class;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(UP, NORTH, SOUTH, WEST, EAST);
    }
}
