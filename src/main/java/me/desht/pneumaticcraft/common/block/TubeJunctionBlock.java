package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.AbstractAirHandlingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.TubeJunctionBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

public class TubeJunctionBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    private static final VoxelShape SHAPE = Stream.of(
            Block.box(4, 4, 4, 12, 12, 12),
            Block.box(0, 6, 6, 16, 10, 10),
            Block.box(6, 6, 0, 10, 10, 16),
            Block.box(3, 5, 5, 13, 11, 11),
            Block.box(5, 5, 3, 11, 11, 13)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    private static final Map<Axis, VoxelShape> SHAPES = new EnumMap<>(Axis.class);
    static {
        SHAPES.put(Axis.Y, SHAPE);
        SHAPES.put(Axis.Z, VoxelShapeUtils.rotateX(SHAPE, 90));
        SHAPES.put(Axis.X, VoxelShapeUtils.rotateY(SHAPES.get(Axis.Z), 90));
    }

    public TubeJunctionBlock() {
        super(ModBlocks.defaultProps());

        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.AXIS, Axis.Y));
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(BlockStateProperties.AXIS);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPES.get(pState.getValue(BlockStateProperties.AXIS));
    }

    @Override
    public boolean onWrenched(Level world, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (player != null && player.isCrouching()) {
            return super.onWrenched(world, player, pos, side, hand);
        } else {
            BlockState state = world.getBlockState(pos);
            Axis newAxis = Axis.values()[(state.getValue(BlockStateProperties.AXIS).ordinal() + 1) % Axis.values().length];
            world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.AXIS, newAxis));
            world.getBlockEntity(pos, ModBlockEntityTypes.TUBE_JUNCTION.get()).ifPresent(AbstractAirHandlingBlockEntity::onBlockRotated);
            return true;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = super.getStateForPlacement(ctx);
        return state == null ? null : state.setValue(BlockStateProperties.AXIS, ctx.getClickedFace().getAxis());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TubeJunctionBlockEntity(pPos, pState);
    }
}
