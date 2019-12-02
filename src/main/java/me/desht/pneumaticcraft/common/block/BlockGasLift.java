package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityGasLift;
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
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class BlockGasLift extends BlockPneumaticCraftModeled {
    private static final VoxelShape SHAPE1 = Block.makeCuboidShape(0, 0, 0, 16, 2, 16);
    private static final VoxelShape SHAPE2 = Block.makeCuboidShape(2, 2, 2, 14, 4, 14);
    private static final VoxelShape SHAPE3 = Block.makeCuboidShape(4, 4, 4, 12, 6, 12);
    private static final VoxelShape SHAPE4 = Block.makeCuboidShape(6, 6, 6, 10, 10, 10);
    private static final VoxelShape SHAPE = VoxelShapes.or(SHAPE1, VoxelShapes.or(SHAPE2, VoxelShapes.or(SHAPE3, SHAPE4)));

    public BlockGasLift() {
        super("gas_lift");
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
        builder.add(BlockPneumaticCraft.UP, BlockPneumaticCraft.NORTH, BlockPneumaticCraft.SOUTH, BlockPneumaticCraft.WEST, BlockPneumaticCraft.EAST);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean b) {
        super.neighborChanged(state, world, pos, block, fromPos, b);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }
}
