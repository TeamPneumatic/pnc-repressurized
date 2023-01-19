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
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SolarCompressorBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    public static final BooleanProperty BOUNDING = BooleanProperty.create("bounding");
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
    private static final VoxelShape SHAPE_W = VoxelShapeUtils.rotateY(SHAPE_S, 90);
    private static final VoxelShape SHAPE_N = VoxelShapeUtils.rotateY(SHAPE_W, 90);
    private static final VoxelShape SHAPE_E = VoxelShapeUtils.rotateY(SHAPE_N, 90);
    private static final VoxelShape[] SHAPES = new VoxelShape[] { SHAPE_S, SHAPE_W, SHAPE_N, SHAPE_E };

    public SolarCompressorBlock() {
        super(ModBlocks.defaultProps());

        registerDefaultState(getStateDefinition().any()
                .setValue(BOUNDING, false));
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if(state.getValue(BOUNDING)) {
            return Block.box(0,0,0,0,0,0);
        }
        else {
            Direction d = state.getValue(directionProperty());
            return SHAPES[d.get2DDataValue()];
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if(state.getValue(BOUNDING)) {
            return Block.box(0,0,0,0,0,0);
        }
        else {
            Direction d = state.getValue(directionProperty());
            return SHAPES[d.get2DDataValue()];
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOUNDING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SolarCompressorBlockEntity(pPos, pState);
    }

    public BlockPos getMainPos(BlockState state, LevelReader world, BlockPos pos) {
        // Gets position of main from bounding block based on offset
        if(state.getValue(BOUNDING)
                && world.getBlockEntity(pos) instanceof SolarCompressorBlockEntity bounding) {

            return pos.subtract(bounding.offsetFromMain);
        }

        // Returns current position as block is main
        else {
            return pos;
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        // Returns if main block is present for bounding blocks
        if (state.getValue(BOUNDING)) {
            return world.getBlockState(getMainPos(state, world, pos)).getBlock() == state.getBlock();
        }

        // Returns false for main block if any bounding blocks are not present
        // Does not apply for initial placement
        else if (world.getBlockEntity(pos) instanceof SolarCompressorBlockEntity solarCompressor
                && solarCompressor.boundingPlaced) {

            for (Vec3i offset : BOUNDING_BLOCK_OFFSETS) {
                if (world.isEmptyBlock(pos.offset(offset))) {
                    return false;
                }
            }
        }

        // Returns false for main block if any bounding positions are not empty
        // Only applies to initial placement
        else {
            for (Vec3i offset : BOUNDING_BLOCK_OFFSETS) {
                if (!world.isEmptyBlock(pos.offset(offset))) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Removes the connected bounding blocks of the main block
     * @param state main block state
     * @param level main block level
     * @param mainPos main block position
     * @param player player to spawn break particles around, null if no particles should spawn
     */
    public void removeBoundingBlocks (BlockState state, Level level, BlockPos mainPos, @Nullable Player player) {
        // Prevents the bounding blocks from causing unwanted removals until all have been removed
        if (level.getBlockEntity(mainPos) instanceof SolarCompressorBlockEntity solarCompressor) {
            solarCompressor.mainBlockRemovalLock = true;
        }

        for (Vec3i offset : BOUNDING_BLOCK_OFFSETS) {
            // Gets bounding block's position
            BlockPos offsetPos = mainPos.offset(offset);

            if (level.getBlockState(offsetPos).getBlock() == this) {
                // Spawns particles for destroyed bounding block if player present
                if (player != null) {
                    spawnDestroyParticles(level, player, offsetPos, state);
                }

                // Destroys bounding block
                level.removeBlock(offsetPos, false);
            }
        }

        // Saves that all bounding blocks have been removed
        // Used later to prevent main block being removed unnecessarily
        if (level.getBlockEntity(mainPos) instanceof SolarCompressorBlockEntity solarCompressor) {
            solarCompressor.boundingRemoved = true;
            solarCompressor.mainBlockRemovalLock = false;
        }
    }

    /**
     * Places bounding blocks for the main block
     * @param level main block level
     * @param mainPos main block position
     */
    public void placeBoundingBlocks (Level level, BlockPos mainPos) {
        for (Vec3i offset : BOUNDING_BLOCK_OFFSETS) {
            level.setBlock(mainPos.offset(offset), level.getBlockState(mainPos).setValue(BOUNDING, true), Block.UPDATE_ALL);

            // Sets offset for bounding block entity
            if (level.getBlockEntity(mainPos.offset(offset)) instanceof SolarCompressorBlockEntity solarCompressor) {
                solarCompressor.offsetFromMain = offset;
            }
        }

        // Saves that all bounding blocks have been placed
        // Used later when checking if main block can survive on updates
        if (level.getBlockEntity(mainPos) instanceof SolarCompressorBlockEntity solarCompressor){
            solarCompressor.boundingPlaced = true;
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockPos mainPos = getMainPos(state, level, pos);
        BlockState mainBlockState = level.getBlockState(mainPos);

        // Redirects destroy to main block for bounding blocks
        if (state.getValue(BOUNDING)) {
            onDestroyedByPlayer(mainBlockState, level, mainPos, player, willHarvest, mainBlockState.getFluidState());

            return false;
        }

        // Destroys all bounding blocks
        else {
            removeBoundingBlocks(state, level, pos, player);
            return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        }
    }

    @Override
    protected void setRotation(Level world, BlockPos pos, Direction rotation) {
        // Only allows rotation of main block
        if (!world.getBlockState(pos).getValue(BOUNDING)) {
            // Prevents main block from being removed gratuitously when bounding blocks are removed
            // from setting the rotation of the main block
            if (world.getBlockEntity(pos) instanceof SolarCompressorBlockEntity solarCompressor) {
                solarCompressor.mainBlockRemovalLock = true;

                super.setRotation(world, pos, rotation);

                solarCompressor.mainBlockRemovalLock = false;
            }
        }
    }

    @Override
    public boolean onWrenched(Level world, Player player, BlockPos pos, Direction face, InteractionHand hand) {
        BlockState state = world.getBlockState(pos);

        // Redirects wrenching to main block for bounding blocks
        if (state.getValue(BOUNDING)) {
            return onWrenched(world, player, getMainPos(state, world, pos), face, hand);
        }

        // Destroy bounding blocks on wrench pickup
        if (player != null && player.isShiftKeyDown()) {
           removeBoundingBlocks(state, world, pos, null);
        }

        return super.onWrenched(world, player, pos, face, hand);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);

        // Creates bounding blocks for main block
        if (!pState.getValue(BOUNDING)) {
            placeBoundingBlocks(pLevel, pPos);
        }
    }

    // Ensures that all blocks are broken when one block breaks
    // Common breaks should handle everything on their own, this is a failsafe
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        // Removes bounding blocks for main block if they weren't already removed
        if (!state.getValue(BOUNDING)
                && world.getBlockEntity(pos) instanceof SolarCompressorBlockEntity solarCompressor
                && !solarCompressor.boundingRemoved) {

            removeBoundingBlocks(state, world, pos, null);
        }

        // Removes main block if a bounding block was forcefully removed separately
        // Does not apply when bounding blocks are removed through removeBoundingBlocks method
        else if (world.getBlockEntity(getMainPos(state, world, pos)) instanceof SolarCompressorBlockEntity solarCompressor
                && !solarCompressor.mainBlockRemovalLock) {

            world.removeBlock(getMainPos(state, world, pos), false);
        }

        super.onRemove(state, world, pos, newState, isMoving);
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
