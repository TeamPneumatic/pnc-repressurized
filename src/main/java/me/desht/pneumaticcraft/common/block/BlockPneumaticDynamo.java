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
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDynamo;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class BlockPneumaticDynamo extends BlockPneumaticCraft {

    private static final VoxelShape SHAPE_UP = Stream.of(
            Block.box(0, 0, 0, 16, 10, 16),
            Block.box(4, 10, 4, 12, 16, 12),
            Block.box(0, 10, 0, 1, 11, 1),
            Block.box(15, 10, 0, 16, 11, 1),
            Block.box(15, 10, 15, 16, 11, 16),
            Block.box(0, 10, 15, 1, 11, 16),
            Block.box(0, 11, 0, 1, 12, 16),
            Block.box(15, 11, 0, 16, 12, 16),
            Block.box(1, 11, 0, 15, 12, 1),
            Block.box(1, 11, 15, 15, 12, 16)
    ).reduce((v1, v2) -> VoxelShapes.join(v1, v2, IBooleanFunction.OR)).get();
    private static final VoxelShape SHAPE_NORTH = VoxelShapeUtils.rotateX(SHAPE_UP, 270);
    private static final VoxelShape SHAPE_DOWN = VoxelShapeUtils.rotateX(SHAPE_NORTH, 270);
    private static final VoxelShape SHAPE_SOUTH = VoxelShapeUtils.rotateY(SHAPE_NORTH, 180);
    private static final VoxelShape SHAPE_EAST = VoxelShapeUtils.rotateY(SHAPE_NORTH, 90);
    private static final VoxelShape SHAPE_WEST = VoxelShapeUtils.rotateY(SHAPE_NORTH, 270);
    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            SHAPE_DOWN, SHAPE_UP, SHAPE_NORTH, SHAPE_SOUTH, SHAPE_WEST, SHAPE_EAST  // DUNSWE order
    };

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public BlockPneumaticDynamo() {
        super(ModBlocks.defaultProps());
        registerDefaultState(getStateDefinition().any().setValue(ACTIVE, false));
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPneumaticDynamo.class;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom(){
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        // coil faces the block it's placed against
        BlockState state = super.getStateForPlacement(ctx);
        return state == null ? null : state.setValue(directionProperty(), ctx.getClickedFace());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES[getRotation(state).get3DDataValue()];
    }
}
