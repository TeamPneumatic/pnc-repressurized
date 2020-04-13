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
    private static final VoxelShape SHAPE1 = Block.makeCuboidShape(0, 0, 0, 16, 2, 16);
    private static final VoxelShape SHAPE2 = Block.makeCuboidShape(2, 2, 2, 14, 4, 14);
    private static final VoxelShape SHAPE3 = Block.makeCuboidShape(4, 4, 4, 12, 6, 12);
    private static final VoxelShape SHAPE4 = Block.makeCuboidShape(6, 6, 6, 10, 10, 10);
    private static final VoxelShape SHAPE = VoxelShapes.or(SHAPE1, VoxelShapes.or(SHAPE2, VoxelShapes.or(SHAPE3, SHAPE4)));

    public BlockGasLift() {
        super(ModBlocks.defaultProps());

        setDefaultState(getStateContainer().getBaseState()
                .with(UP, false)
                .with(NORTH, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(EAST, false)
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
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

        builder.add(UP, NORTH, SOUTH, WEST, EAST);
    }
}
