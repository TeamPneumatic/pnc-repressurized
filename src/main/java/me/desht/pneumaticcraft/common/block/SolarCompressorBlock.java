package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.common.block.entity.SolarCompressorBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SolarCompressorBlock extends AbstractPNCBlockWithBoundingBlocks {
    private static final Vec3i[] BOUNDING_BLOCK_OFFSETS = {
            new Vec3i(0, 1, 0),
            new Vec3i(0, 1, 1),
            new Vec3i(0, 1, -1)};

    private static final VoxelShape SHAPE_S = Stream.of(
            Block.box(0, 1, 4, 16, 16, 12),
            Block.box(5, 16, 5, 11, 17, 11),
            Block.box(2, 15, 3, 3, 17, 13),
            Block.box(13, 15, 3, 14, 17, 13),
            Block.box(3, 3, 0, 13, 13, 16),
            Block.box(1, 1, 1, 15, 15, 15),
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(7, 17, 7, 9, 31, 9)
        ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    protected static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape SHAPE_N = VoxelShapeUtils.rotateY(SHAPE_W, 90);
    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public SolarCompressorBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        // Empty bounding blocks
        if(state.getValue(BOUNDING)) {
            return Block.box(0,0,0,0,0,0);
        }

        // Rotating main block
        Direction d = state.getValue(directionProperty());
        return SHAPES[d.get2DDataValue()];
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return getShape(state, world, pos, context);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SolarCompressorBlockEntity(pPos, pState);
    }

    @Override
    public @Nullable Vec3i[] getBoundingBlockOffsets() {
        return BOUNDING_BLOCK_OFFSETS;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        // Prevents any interactions with bounding blocks of compressor
        if (state.getValue(BOUNDING)) {
            return InteractionResult.FAIL;
        }

        // Only can repair if proper block entity type and is broken
        if (world.getBlockEntity(pos) instanceof SolarCompressorBlockEntity solarCompressor
                && solarCompressor.isBroken()) {

            if (player.getMainHandItem().getItem() == ModItems.SOLAR_CELL.get()) {
                // Only consumes solar cell when player is not in creative
                if (!player.isCreative()) {
                    player.getMainHandItem().shrink(1);
                }

                solarCompressor.fixBroken();
                world.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1 ,1.5f);

                return InteractionResult.SUCCESS;
            }
        }

        // Allows other interactions if cannot do repair interaction
        return super.use(state, world, pos, player, hand, brtr);
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter world, List<Component> curInfo, TooltipFlag flag) {
        super.appendHoverText(stack, world, curInfo, flag);

        if (stack.hasTag()) {
            CompoundTag subTag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
            if (subTag != null && subTag.contains(NBTKeys.NBT_BROKEN)) {
                if (subTag.getBoolean(NBTKeys.NBT_BROKEN)) {
                    curInfo.add(xlate("pneumaticcraft.gui.tooltip.broken"));
                }
            }
        }
    }
}
