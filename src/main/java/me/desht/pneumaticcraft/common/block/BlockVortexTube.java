package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVortexTube;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BlockVortexTube extends BlockPneumaticCraft {

    private static final VoxelShape[] SHAPES = new VoxelShape[] {  // DUNSWE order
            Block.makeCuboidShape(0,0, 0, 15, 15, 15),
            Block.makeCuboidShape(0,1, 1, 15, 16, 16),
            Block.makeCuboidShape(1,0, 0, 16, 15, 15),
            Block.makeCuboidShape(0,0, 1, 15, 15, 16),
            Block.makeCuboidShape(0,0, 0, 15, 15, 15),
            Block.makeCuboidShape(1,0, 1, 16, 15, 16),
    };

    public BlockVortexTube() {
        super(ModBlocks.defaultProps());

        setDefaultState(getStateContainer().getBaseState()
            .with(DOWN, false)
            .with(UP, false)
            .with(NORTH, false)
            .with(SOUTH, false)
            .with(WEST, false)
            .with(EAST, false)
        );
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(CONNECTION_PROPERTIES);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityVortexTube.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES[getRotation(state).getIndex()];
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }
}
