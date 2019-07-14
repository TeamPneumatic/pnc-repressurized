package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityGasLift;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockGasLift extends BlockPneumaticCraftModeled {
    private static final VoxelShape SHAPE = Block.makeCuboidShape(0, 0, 0, 16, 10, 16);
    private static final AxisAlignedBB BLOCK_BOUNDS = new AxisAlignedBB(0, 0, 0, 1, 10 / 16F, 1);

    public BlockGasLift() {
        super(Material.IRON, "gas_lift");
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
}
