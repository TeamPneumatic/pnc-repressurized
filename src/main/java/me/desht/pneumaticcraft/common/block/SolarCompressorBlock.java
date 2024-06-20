package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.compressor.SolarCompressorBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SolarCompressorBlock extends AbstractPNCBlockWithBoundingBlocks {
    private static final Vec3i[] BOUNDING_BLOCK_OFFSETS = {
            new Vec3i(0, 1, 0),
            new Vec3i(0, 1, 1),
            new Vec3i(0, 1, -1)};

    private static final VoxelShape SHAPE_S = VoxelShapeUtils.or(
            Block.box(0, 1, 4, 16, 16, 12),
            Block.box(5, 16, 5, 11, 17, 11),
            Block.box(2, 15, 3, 3, 17, 13),
            Block.box(13, 15, 3, 14, 17, 13),
            Block.box(3, 3, 0, 13, 13, 16),
            Block.box(1, 1, 1, 15, 15, 15),
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(7, 17, 7, 9, 31, 9)
    );
    protected static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape SHAPE_N = VoxelShapeUtils.rotateY(SHAPE_W, 90);
    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public SolarCompressorBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
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
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        // Prevents any interactions with bounding blocks of compressor
        if (state.getValue(BOUNDING)) {
            return ItemInteractionResult.FAIL;
        }

        // Only can repair if proper block entity type and is broken
        if (level.getBlockEntity(pos) instanceof SolarCompressorBlockEntity solarCompressor
                && solarCompressor.isBroken()
                && stack.getItem() == ModItems.SOLAR_CELL.get()) {

            if (!level.isClientSide) {
                // Only consumes solar cell when player is not in creative
                if (!player.isCreative()) {
                    player.getMainHandItem().shrink(1);
                }

                solarCompressor.fixBroken();
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1 ,1.5f);
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Allows other interactions if we cannot do repair interaction
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> curInfo, TooltipFlag flag) {
        super.appendHoverText(stack, context, curInfo, flag);

        if (stack.getOrDefault(ModDataComponents.SOLAR_BROKEN, false)) {
            curInfo.add(xlate("pneumaticcraft.gui.tooltip.broken"));
        }
    }

    @Override
    public void addSerializableComponents(List<DataComponentType<?>> list) {
        super.addSerializableComponents(list);
        list.add(ModDataComponents.SOLAR_BROKEN.get());
    }
}
