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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class BlockHeatPipe extends BlockPneumaticCraftCamo implements SimpleWaterloggedBlock, EntityBlockPneumaticCraft {
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(CONNECTION_PROPERTIES);
        builder.add(WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        if (state == null) return null;

        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return state.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return stateIn;
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
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
                    SHAPE_CACHE[idx] = Shapes.join(SHAPE_CACHE[idx], SIDES[i], BooleanOp.OR);
                }
            }
        }
        return SHAPE_CACHE[idx];
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TileEntityHeatPipe(pPos, pState);
    }
}
