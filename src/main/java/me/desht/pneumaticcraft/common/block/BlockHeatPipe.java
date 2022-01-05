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
import me.desht.pneumaticcraft.common.tileentity.TileEntityHeatPipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

import static net.minecraft.state.properties.BlockStateProperties.WATERLOGGED;

public class BlockHeatPipe extends BlockPneumaticCraftCamo implements IWaterLoggable {
    private static final VoxelShape CORE = Block.box(4, 4, 4, 12, 12, 12);
    private static final VoxelShape[] SIDES = {
            Block.box(4, 0, 4, 12, 4, 12),
            Block.box(4, 12, 4, 12, 16, 12),
            Block.box(4, 4, 0, 12, 12, 4),
            Block.box(4, 4, 12, 12, 12, 16),
            Block.box(0, 4, 4, 4, 12, 12),
            Block.box(12, 4, 4, 16, 12, 12)
    };

    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[64];  // 2^6 shapes

    public BlockHeatPipe() {
        super(ModBlocks.defaultProps().noOcclusion());  // notSolid() because of camo requirements

        BlockState state = getStateDefinition().any();
        for (BooleanProperty prop : CONNECTION_PROPERTIES) {
            state = state.setValue(prop, false);
        }
        registerDefaultState(state.setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(CONNECTION_PROPERTIES);
        builder.add(WATERLOGGED);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityHeatPipe.class;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state == null) return null;

        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return state.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return stateIn;
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        byte idx = 0;
        for (int i = 0; i < 6; i++) {
            if (state.getValue(CONNECTION_PROPERTIES[i])) {
                idx |= 1 << i;
            }
        }

        if (SHAPE_CACHE[idx] == null) {
            SHAPE_CACHE[idx] = CORE;
            for (int i = 0; i < 6; i++) {
                if ((idx & (1 << i)) != 0) {
                    SHAPE_CACHE[idx] = VoxelShapes.join(SHAPE_CACHE[idx], SIDES[i], IBooleanFunction.OR);
                }
            }
        }
        return SHAPE_CACHE[idx];
    }
}
