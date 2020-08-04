package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidMixer;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
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

public class BlockFluidMixer extends BlockPneumaticCraft {
    private static final VoxelShape INPUT1 = makeCuboidShape(10, 0, 1, 16,8, 7);
    private static final VoxelShape INPUT2 = makeCuboidShape(0, 0, 1, 6,8, 7);
    private static final VoxelShape MAIN = makeCuboidShape(2, 0, 7, 14,8, 15);
    private static final VoxelShape OUTPUT = makeCuboidShape(5, 8, 8, 11,16, 14);

    private static final VoxelShape[] SHAPES = new VoxelShape[4];
    static {
        // S, W, N, E
        SHAPES[2] = VoxelShapes.or(INPUT1, INPUT2, MAIN, OUTPUT);
        SHAPES[1] = VoxelShapeUtils.rotateY(SHAPES[2], 270);
        SHAPES[0] = VoxelShapeUtils.rotateY(SHAPES[2], 180);
        SHAPES[3] = VoxelShapeUtils.rotateY(SHAPES[2], 90);
    }

    public BlockFluidMixer() {
        super(ModBlocks.defaultProps());
        setDefaultState(getStateContainer().getBaseState()
                .with(BlockPneumaticCraft.NORTH, false)
                .with(BlockPneumaticCraft.SOUTH, false)
                .with(BlockPneumaticCraft.WEST, false)
                .with(BlockPneumaticCraft.EAST, false)
        );
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockPneumaticCraft.NORTH, BlockPneumaticCraft.SOUTH, BlockPneumaticCraft.WEST, BlockPneumaticCraft.EAST);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityFluidMixer.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction d = state.get(directionProperty());
        return SHAPES[d.getHorizontalIndex()];
    }

    @Override
    protected boolean reversePlacementRotation() {
        return true;
    }
}
