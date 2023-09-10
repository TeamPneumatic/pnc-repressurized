package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.ManualCompressorBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

public class ManualCompressorBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    private static final VoxelShape SHAPE_S = VoxelShapeUtils.or(
            Block.box(6, 1, 5, 10, 7, 11),
            Block.box(10, 1, 6, 11, 7, 10),
            Block.box(5, 1, 6, 6, 7, 10),
            Block.box(7, 6.5, 7, 9, 7.5, 9),
            Block.box(6, 7, 10, 10, 10, 11),
            Block.box(5, 5, 11, 11, 11, 15),
            Block.box(6, 6, 15, 10, 10, 16),
            Block.box(5, 2.5, 12.001, 6, 5.5, 13.998999999999999),
            Block.box(10, 2.5, 12.001, 11, 5.5, 13.998999999999999),
            Block.box(3, 2, 12, 13, 3, 14),
            Block.box(3, 2, 2, 13, 3, 4),
            Block.box(3, 0, 0, 5, 2, 16),
            Block.box(11, 0, 0, 13, 2, 16),
            Block.box(5, 0, 1, 11, 1, 15)
    );

    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape SHAPE_N = VoxelShapeUtils.rotateY(SHAPE_W, 90);
    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public ManualCompressorBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ManualCompressorBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        BlockEntity be = world.getBlockEntity(pos);

        // Triggers a pump cycle step when manual compressor is right-clicked
        if (be instanceof ManualCompressorBlockEntity manualCompressorBlockEntity
                // Only allows fake players to use compressor if the config is true
                && (ConfigHelper.common().machines.manualCompressorAllowFakePlayers.get() || !(player instanceof FakePlayer))
                // Can only pump if hunger is not empty (does not apply to creative players, or if manual compressor does not consume hunger via config)
                && (ConfigHelper.common().machines.manualCompressorHungerDrainPerCycleStep.get() == 0 || player.isCreative() || player.getFoodData().getFoodLevel() != 0)
                // Can only pump if both hands are empty
                && (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty())) {
            manualCompressorBlockEntity.onPumpCycleStep(player);
            return InteractionResult.SUCCESS;
        }

        return super.use(state, world, pos, player, hand, brtr);
    }
}
