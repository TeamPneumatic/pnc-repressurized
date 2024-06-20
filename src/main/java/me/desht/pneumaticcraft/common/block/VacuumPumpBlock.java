package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.utility.VacuumPumpBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class VacuumPumpBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    private static final VoxelShape SHAPE_N = VoxelShapeUtils.or(
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
    );

    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.rotateY(SHAPE_E, 90);
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);

    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_E, SHAPE_S, SHAPE_W, SHAPE_N };

    public VacuumPumpBlock() {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new VacuumPumpBlockEntity(pPos, pState);
    }
}
