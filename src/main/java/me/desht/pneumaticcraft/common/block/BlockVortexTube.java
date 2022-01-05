package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVortexTube;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import java.util.stream.Stream;

public class BlockVortexTube extends BlockPneumaticCraft implements ColorHandlers.IHeatTintable {

    private static final VoxelShape SHAPE_N = Stream.of(
            Block.box(12, 4, 15, 13, 12, 16),
            Block.box(5, 5, 12, 11, 11, 13),
            Block.box(4, 4, 4, 12, 12, 12),
            Block.box(6, 6, 2, 10, 10, 3),
            Block.box(6, 6, 13, 10, 10, 14),
            Block.box(5, 10, 2, 6, 11, 3),
            Block.box(10, 10, 2, 11, 11, 3),
            Block.box(5, 5, 2, 6, 6, 3),
            Block.box(10, 5, 2, 11, 6, 3),
            Block.box(9, 7, 0.75, 10, 9, 1.75),
            Block.box(6, 7, 0.75, 7, 9, 1.75),
            Block.box(7, 9, 0.75, 9, 10, 1.75),
            Block.box(6, 5, 14.25, 10, 11, 15.25),
            Block.box(5, 6, 14.25, 6, 10, 15.25),
            Block.box(10, 6, 14.25, 11, 10, 15.25),
            Block.box(7, 6, 0.75, 9, 7, 1.75),
            Block.box(4, 4, 14, 12, 12, 15),
            Block.box(5, 5, 1, 11, 11, 2),
            Block.box(4, 12, 15, 12, 13, 16),
            Block.box(5, 11, 0, 11, 12, 1),
            Block.box(3, 4, 15, 4, 12, 16),
            Block.box(4, 5, 0, 5, 11, 1),
            Block.box(4, 3, 15, 12, 4, 16),
            Block.box(5, 4, 0, 11, 5, 1),
            Block.box(11, 5, 0, 12, 11, 1),
            Block.box(1, 0, 0, 15, 1, 1),
            Block.box(1, 0, 15, 15, 1, 16),
            Block.box(0, 0, 0, 1, 16, 1),
            Block.box(0, 0, 15, 1, 16, 16),
            Block.box(1, 15, 0, 15, 16, 1),
            Block.box(1, 15, 15, 15, 16, 16),
            Block.box(15, 0, 0, 16, 16, 1),
            Block.box(15, 0, 15, 16, 16, 16),
            Block.box(0, 0, 1, 1, 1, 15),
            Block.box(15, 0, 1, 16, 1, 15),
            Block.box(0, 15, 1, 1, 16, 15),
            Block.box(15, 15, 1, 16, 16, 15),
            Block.box(5, 5, 3, 11, 11, 4),
            Block.box(10, 5, 13, 11, 6, 14),
            Block.box(10, 10, 13, 11, 11, 14),
            Block.box(5, 5, 13, 6, 6, 14),
            Block.box(5, 10, 13, 6, 11, 14)
    ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();

    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape SHAPE_U = VoxelShapeUtils.rotateX(SHAPE_N, 90);
    private static final VoxelShape SHAPE_D = VoxelShapeUtils.rotateX(SHAPE_S, 90);

    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_D, SHAPE_U, SHAPE_N, SHAPE_S, SHAPE_W, SHAPE_E };

//    private static final VoxelShape[] SHAPES = new VoxelShape[] {  // DUNSWE order
//            Block.box(0,0, 0, 15, 15, 15),
//            Block.box(0,1, 1, 15, 16, 16),
//            Block.box(1,0, 0, 16, 15, 15),
//            Block.box(0,0, 1, 15, 15, 16),
//            Block.box(0,0, 0, 15, 15, 15),
//            Block.box(1,0, 1, 16, 15, 16),
//    };

    public BlockVortexTube() {
        super(ModBlocks.defaultProps());

        registerDefaultState(getStateDefinition().any()
            .setValue(DOWN, false)
            .setValue(UP, false)
            .setValue(NORTH, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(EAST, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONNECTION_PROPERTIES);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityVortexTube.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES[getRotation(state).get3DDataValue()];
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
