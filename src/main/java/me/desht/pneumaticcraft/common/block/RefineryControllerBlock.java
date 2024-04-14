package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.processing.RefineryControllerBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RefineryControllerBlock extends AbstractPneumaticCraftBlock
        implements PneumaticCraftEntityBlock, IBlockComparatorSupport
{
    private static final VoxelShape SHAPE_N = VoxelShapeUtils.or(
            Block.box(6, 11, 12.5, 7, 12, 14.5),
            Block.box(14, 10, 0, 16, 13, 2),
            Block.box(14, 10, 14, 16, 13, 16),
            Block.box(0, 10, 0, 2, 13, 2),
            Block.box(0, 10, 14, 2, 13, 16),
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(7.5, 11, 12.5, 8.5, 12, 14.5),
            Block.box(4.5, 11, 12.5, 5.5, 12, 14.5),
            Block.box(3, 11, 12.5, 4, 12, 14.5),
            Block.box(7.5, 12, 13.5, 8.5, 14, 14.5),
            Block.box(6, 12, 13.5, 7, 14, 14.5),
            Block.box(4.5, 12, 13.5, 5.5, 14, 14.5),
            Block.box(3, 12, 13.5, 4, 14, 14.5),
            Block.box(2, 14, 13, 9, 16, 16),
            Block.box(3, 1, 0, 10, 7, 1),
            Block.box(2, 1, 13, 9, 9, 15),
            Block.box(2, 1, 1, 14, 16, 13),
            Block.box(9, 15, 15, 16, 16, 16),
            Block.box(0, 15, 15, 2, 16, 16),
            Block.box(0.5, 1, 0.5, 1.5, 15, 1.5),
            Block.box(14.5, 1, 0.5, 15.5, 15, 1.5),
            Block.box(0.5, 1, 14.5, 1.5, 15, 15.5),
            Block.box(0, 15, 6, 1, 16, 15),
            Block.box(0, 14, 3, 2, 16, 6),
            Block.box(0, 15, 1, 1, 16, 3),
            Block.box(0, 15, 0, 16, 16, 1),
            Block.box(15, 15, 1, 16, 16, 7),
            Block.box(14, 14, 7, 16, 16, 11),
            Block.box(15, 15, 11, 16, 16, 15),
            Block.box(14.5, 1, 14.5, 15.5, 15, 15.5),
            Block.box(1.5, 11, 0.5, 14.5, 12, 1.5),
            Block.box(2.5, 10.5, 10.5, 9, 12.5, 12.5),
            Block.box(2.75, 1, 10.75, 8.75, 11, 12.25)
    );

    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public RefineryControllerBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);

        world.getBlockEntity(pos, ModBlockEntityTypes.REFINERY.get())
                .ifPresent(RefineryControllerBlockEntity::clearOutputCache);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RefineryControllerBlockEntity(pPos, pState);
    }
}
