package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.ThermopneumaticProcessingPlantBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ThermopneumaticProcessingPlantBlock extends AbstractPneumaticCraftBlock
        implements PneumaticCraftEntityBlock, IBlockComparatorSupport
{
    private static final VoxelShape SHAPE_N = VoxelShapeUtils.or(
            Block.box(2, 11, 0.5, 4, 12, 1.5),
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
            Block.box(9, 1, 11, 16, 13, 16),
            Block.box(0, 1, 11, 7, 13, 16),
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
            Block.box(12, 11, 0.5, 14, 12, 1.5)
    );

    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_N, 180);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_N, 270);
    private static final VoxelShape[] SHAPES = { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public ThermopneumaticProcessingPlantBlock() {
        super(ModBlocks.defaultProps().noOcclusion());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(directionProperty()).get2DDataValue()];
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ThermopneumaticProcessingPlantBlockEntity(pPos, pState);
    }
}
