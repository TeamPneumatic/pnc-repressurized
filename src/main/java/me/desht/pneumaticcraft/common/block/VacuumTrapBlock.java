package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.common.block.entity.spawning.VacuumTrapBlockEntity;
import me.desht.pneumaticcraft.common.item.SpawnerCoreItem;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.VoxelShapeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED;

public class VacuumTrapBlock extends AbstractPneumaticCraftBlock implements SimpleWaterloggedBlock, PneumaticCraftEntityBlock {
    private static final VoxelShape SHAPE_N_OPEN = VoxelShapeUtils.or(
            Block.box(3, 1, 0, 13, 11, 16),
            Block.box(4, 0, 1, 12, 1, 15),
            Block.box(7, 12, 14, 9, 14, 16),
            Block.box(6, 11, 14, 10, 12, 16),
            Block.box(6, 11, 0, 10, 12, 2),
            Block.box(7, 14, 8, 9, 16, 16),
            Block.box(11, 11, 2, 16, 12, 14),
            Block.box(0, 11, 2, 5, 12, 14)
    );
    private static final VoxelShape SHAPE_E_OPEN = VoxelShapeUtils.rotateY(SHAPE_N_OPEN, 90);
    private static final VoxelShape SHAPE_S_OPEN = VoxelShapeUtils.rotateY(SHAPE_E_OPEN, 90);
    private static final VoxelShape SHAPE_W_OPEN = VoxelShapeUtils.rotateY(SHAPE_S_OPEN, 90);
    private static final VoxelShape[] SHAPES_OPEN = new VoxelShape[] {SHAPE_S_OPEN, SHAPE_W_OPEN, SHAPE_N_OPEN, SHAPE_E_OPEN};

    private static final VoxelShape SHAPE_N_CLOSED = VoxelShapeUtils.or(
            Block.box(3, 1, 0, 13, 11, 16),
            Block.box(4, 0, 1, 12, 1, 15),
            Block.box(7, 12, 14, 9, 14, 16),
            Block.box(6, 11, 14, 10, 12, 16),
            Block.box(6, 11, 0, 10, 12, 2),
            Block.box(7, 14, 8, 9, 16, 16),
            Block.box(8, 11, 2, 13, 12, 14),
            Block.box(3, 11, 2, 8, 12, 14)
    );
    private static final VoxelShape SHAPE_E_CLOSED = VoxelShapeUtils.rotateY(SHAPE_N_CLOSED, 90);
    private static final VoxelShape SHAPE_S_CLOSED = VoxelShapeUtils.rotateY(SHAPE_E_CLOSED, 90);
    private static final VoxelShape SHAPE_W_CLOSED = VoxelShapeUtils.rotateY(SHAPE_S_CLOSED, 90);
    private static final VoxelShape[] SHAPES_CLOSED = new VoxelShape[] {SHAPE_S_CLOSED, SHAPE_W_CLOSED, SHAPE_N_CLOSED, SHAPE_E_CLOSED};

    public VacuumTrapBlock() {
        super(ModBlocks.defaultProps());

        registerDefaultState(defaultBlockState()
                .setValue(OPEN, false)
                .setValue(POWERED, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(DOWN, false)
        );
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(OPEN, POWERED, NORTH, SOUTH, EAST, WEST, DOWN);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {

        if (state.getValue(OPEN)) {
            return SHAPES_OPEN[state.getValue(directionProperty()).get2DDataValue()];
        } else if (!state.getValue(OPEN)) {
            return SHAPES_CLOSED[state.getValue(directionProperty()).get2DDataValue()];
        } else {
            return super.getShape(state, worldIn, pos, context);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        if (player.isShiftKeyDown()) {
            boolean open = state.getValue(OPEN);
            world.setBlockAndUpdate(pos, state.setValue(OPEN, !open));
            world.playSound(player, pos, open ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1f, 0.5f);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return super.use(state, world, pos, player, hand, brtr);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean powered = world.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            if (state.getValue(OPEN) != powered) {
                state = state.setValue(OPEN, powered);
                world.playSound(null, pos, powered ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1f, 0.5f);
            }
            world.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
        }
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new VacuumTrapBlockEntity(pPos, pState);
    }

    public static class ItemBlockVacuumTrap extends BlockItem {
        public ItemBlockVacuumTrap(VacuumTrapBlock blockVacuumTrap) {
            super(blockVacuumTrap, ModItems.defaultProps());
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
            super.appendHoverText(stack, worldIn, tooltip, flagIn);

            CompoundTag tag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
            if (tag != null && tag.contains("Items")) {
                ItemStackHandler handler = new ItemStackHandler(1);
                handler.deserializeNBT(tag.getCompound("Items"));
                if (handler.getStackInSlot(0).getItem() instanceof SpawnerCoreItem) {
                    tooltip.add(xlate("pneumaticcraft.message.vacuum_trap.coreInstalled").withStyle(ChatFormatting.YELLOW));
                }
            }
        }
    }
}
