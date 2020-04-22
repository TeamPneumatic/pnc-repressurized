package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityHeatPipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
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
    private static final VoxelShape CORE = Block.makeCuboidShape(3, 3, 3, 13, 13, 13);
    private static final VoxelShape[] SIDES = {
            Block.makeCuboidShape(3, 0, 3, 13, 3, 13),
            Block.makeCuboidShape(3, 13, 3, 13, 16, 13),
            Block.makeCuboidShape(3, 3, 0, 13, 13, 3),
            Block.makeCuboidShape(3, 3, 13, 13, 13, 16),
            Block.makeCuboidShape(0, 3, 3, 3, 13, 13),
            Block.makeCuboidShape(13, 3, 3, 16, 13, 13)
    };

    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[64];  // 2^6 shapes

    public BlockHeatPipe() {
        super(ModBlocks.defaultProps().notSolid());  // notSolid() because of camo requirements

        BlockState state = getStateContainer().getBaseState();
        for (BooleanProperty prop : CONNECTION_PROPERTIES) {
            state = state.with(prop, false);
        }
        setDefaultState(state.with(WATERLOGGED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

        builder.add(CONNECTION_PROPERTIES);
        builder.add(WATERLOGGED);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityHeatPipe.class;
    }

    @Override
    public IFluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);

        IFluidState fluidState = ctx.getWorld().getFluidState(ctx.getPos());
        return state.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return stateIn;
    }

    @Override
    public VoxelShape getUncamouflagedShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        byte idx = 0;
        for (int i = 0; i < 6; i++) {
            if (state.get(CONNECTION_PROPERTIES[i])) {
                idx |= 1 << i;
            }
        }

        if (SHAPE_CACHE[idx] == null) {
            SHAPE_CACHE[idx] = CORE;
            for (int i = 0; i < 6; i++) {
                if ((idx & (1 << i)) != 0) {
                    SHAPE_CACHE[idx] = VoxelShapes.combineAndSimplify(SHAPE_CACHE[idx], SIDES[i], IBooleanFunction.OR);
                }
            }
        }
        return SHAPE_CACHE[idx];
    }
}
