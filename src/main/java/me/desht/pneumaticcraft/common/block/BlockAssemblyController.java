package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class BlockAssemblyController extends BlockPneumaticCraft {

    private static final VoxelShape BASE_SHAPE = Block.makeCuboidShape(0, 0, 0, 16, 1, 16);
    private static final VoxelShape LEG_SHAPE = Block.makeCuboidShape(7, 2, 7, 9, 12, 9);
    private static final VoxelShape SHAPE = VoxelShapes.or(BASE_SHAPE, LEG_SHAPE);

    public BlockAssemblyController() {
        super(ModBlocks.defaultProps());
        setDefaultState(getStateContainer().getBaseState()
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
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, WEST, EAST);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAssemblyController.class;
    }
}
