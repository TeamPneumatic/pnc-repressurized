package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import java.util.stream.Stream;

public class BlockThermopneumaticProcessingPlant extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE_N = Stream.of(
            Block.box(14, 5, 5, 16, 10, 9),
            Block.box(0, 5, 5, 2, 10, 9),
            Block.box(4, 1, 0, 12, 13, 1),
            Block.box(0, 10, 0, 2, 13, 2),
            Block.box(0.5, 13, 14.5, 1.5, 15, 15.5),
            Block.box(14.5, 1, 0.5, 15.5, 15, 1.5),
            Block.box(0.5, 1, 0.5, 1.5, 15, 1.5),
            Block.box(0.5, 11, 1.5, 1.5, 12, 9),
            Block.box(14.5, 11, 1.5, 15.5, 12, 9),
            Block.box(15, 15, 1, 16, 16, 15),
            Block.box(0, 15, 1, 1, 16, 15),
            Block.box(0, 15, 0, 16, 16, 1),
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(9, 13, 11, 16, 13, 16),
            Block.box(0, 13, 11, 7, 13, 16),
            Block.box(16, 1, 11, 16, 13, 16),
            Block.box(7, 1, 11, 7, 13, 16),
            Block.box(9, 1, 11, 9, 13, 16),
            Block.box(0, 1, 11, 0, 13, 16),
            Block.box(9, 1, 16, 16, 13, 16),
            Block.box(0, 1, 16, 7, 13, 16),
            Block.box(14, 1, 9, 16, 13, 11),
            Block.box(0, 1, 9, 2, 13, 11),
            Block.box(2, 1, 1, 14, 16, 11),
            Block.box(14, 10, 0, 16, 13, 2),
            Block.box(14.5, 13, 14.5, 15.5, 15, 15.5),
            Block.box(7, 2.5, 13.5, 9, 3.5, 14.5),
            Block.box(11.5, 13.5, 10.5, 12.5, 14.5, 12.5),
            Block.box(3.5, 13.5, 10.5, 4.5, 14.5, 12.5),
            Block.box(11.5, 13.25, 12.5, 12.5, 14.5, 13.5),
            Block.box(3.5, 13.25, 12.5, 4.5, 14.5, 13.5),
            Block.box(8.75, 2, 13, 9, 4, 15),
            Block.box(7, 2, 13, 7.25, 4, 15),
            Block.box(11, 13, 11, 13, 15, 11.25),
            Block.box(3, 13, 11, 5, 15, 11.25),
            Block.box(11, 13, 12, 13, 13.25, 14),
            Block.box(3, 13, 12, 5, 13.25, 14),
            Block.box(0, 15, 15, 16, 16, 16),
            Block.box(12, 11, 0.5, 14, 12, 1.5),
            Block.box(2, 11, 0.5, 4, 12, 1.5)
    ).reduce((v1, v2) -> {return VoxelShapes.join(v1, v2, IBooleanFunction.OR);}).get();
    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_N, 180);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_N, 270);
    private static final VoxelShape[] SHAPES = { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public BlockThermopneumaticProcessingPlant() {
        super(ModBlocks.defaultProps().noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES[state.getValue(directionProperty()).get2DDataValue()];
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityThermopneumaticProcessingPlant.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }
}
