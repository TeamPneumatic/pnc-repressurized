package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumPump;
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

import java.util.stream.Stream;

public class BlockVacuumPump extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE_N = Stream.of(
            Block.box(3, 7.75, 7.75, 9, 8.25, 8.25),
            Block.box(15, 5, 5, 16, 11, 11),
            Block.box(9, 5, 4, 15, 12, 12),
            Block.box(3, 11, 5, 9, 11, 11),
            Block.box(0, 1, 3, 16, 5, 13),
            Block.box(13, 0, 4, 15, 1, 6),
            Block.box(13, 0, 10, 15, 1, 12),
            Block.box(1, 0, 4, 3, 1, 6),
            Block.box(1, 0, 10, 3, 1, 12),
            Block.box(3, 5, 5, 9, 11, 5),
            Block.box(3, 5, 11, 9, 11, 11),
            Block.box(0, 5, 5, 3, 11, 11),
            Block.box(12.5, 12, 7, 14.5, 12.25, 9),
            Block.box(0.5, 11, 7, 2.5, 11.25, 9)
    ).reduce((v1, v2) -> {return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);}).get();

    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);

    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_E, SHAPE_S, SHAPE_W, SHAPE_N };

//    private static final VoxelShape COLLISION_SHAPE = Block.box(2, 2, 2, 14, 14, 14);

    public BlockVacuumPump() {
        super(ModBlocks.defaultProps());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction d = state.get(directionProperty());
        return SHAPES[d.getHorizontalIndex()];
    }

//    @Override
//    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
//        return COLLISION_SHAPE;
//    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityVacuumPump.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }
}
