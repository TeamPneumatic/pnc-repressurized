package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityRefineryController;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.stream.Stream;

public class BlockRefineryController extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE_N = Stream.of(
            Block.makeCuboidShape(6, 11, 12.5, 7, 12, 14.5),
            Block.makeCuboidShape(14, 10, 0, 16, 13, 2),
            Block.makeCuboidShape(14, 10, 14, 16, 13, 16),
            Block.makeCuboidShape(0, 10, 0, 2, 13, 2),
            Block.makeCuboidShape(0, 10, 14, 2, 13, 16),
            Block.makeCuboidShape(0, 0, 0, 16, 1, 16),
            Block.makeCuboidShape(7.5, 11, 12.5, 8.5, 12, 14.5),
            Block.makeCuboidShape(4.5, 11, 12.5, 5.5, 12, 14.5),
            Block.makeCuboidShape(3, 11, 12.5, 4, 12, 14.5),
            Block.makeCuboidShape(7.5, 12, 13.5, 8.5, 14, 14.5),
            Block.makeCuboidShape(6, 12, 13.5, 7, 14, 14.5),
            Block.makeCuboidShape(4.5, 12, 13.5, 5.5, 14, 14.5),
            Block.makeCuboidShape(3, 12, 13.5, 4, 14, 14.5),
            Block.makeCuboidShape(2, 14, 13, 9, 16, 16),
            Block.makeCuboidShape(3, 1, 0, 10, 7, 1),
            Block.makeCuboidShape(2, 1, 13, 9, 9, 15),
            Block.makeCuboidShape(2, 1, 1, 14, 16, 13),
            Block.makeCuboidShape(9, 15, 15, 16, 16, 16),
            Block.makeCuboidShape(0, 15, 15, 2, 16, 16),
            Block.makeCuboidShape(0.5, 1, 0.5, 1.5, 15, 1.5),
            Block.makeCuboidShape(14.5, 1, 0.5, 15.5, 15, 1.5),
            Block.makeCuboidShape(0.5, 1, 14.5, 1.5, 15, 15.5),
            Block.makeCuboidShape(0, 15, 6, 1, 16, 15),
            Block.makeCuboidShape(0, 14, 3, 2, 16, 6),
            Block.makeCuboidShape(0, 15, 1, 1, 16, 3),
            Block.makeCuboidShape(0, 15, 0, 16, 16, 1),
            Block.makeCuboidShape(15, 15, 1, 16, 16, 7),
            Block.makeCuboidShape(14, 14, 7, 16, 16, 11),
            Block.makeCuboidShape(15, 15, 11, 16, 16, 15),
            Block.makeCuboidShape(14.5, 1, 14.5, 15.5, 15, 15.5),
            Block.makeCuboidShape(1.5, 11, 0.5, 14.5, 12, 1.5),
            Block.makeCuboidShape(2.5, 10.5, 10.5, 9, 12.5, 12.5),
            Block.makeCuboidShape(2.75, 1, 10.75, 8.75, 11, 12.25)
    ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();

    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public BlockRefineryController() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityRefineryController.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction d = state.get(directionProperty());
        return SHAPES[d.getHorizontalIndex()];
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);

        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityRefineryController.class)
                .ifPresent(TileEntityRefineryController::cacheRefineryOutputs);
    }
}
