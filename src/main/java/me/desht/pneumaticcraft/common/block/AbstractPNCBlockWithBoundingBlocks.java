package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.IHasBoundingBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public abstract class AbstractPNCBlockWithBoundingBlocks extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    public static final BooleanProperty BOUNDING = BooleanProperty.create("bounding");

    public AbstractPNCBlockWithBoundingBlocks(Properties props) {
        super(props);

        registerDefaultState(defaultBlockState().setValue(BOUNDING, false));
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        // Default empty bounding blocks
        if(state.getValue(BOUNDING)) {
            return Block.box(0,0,0,0,0,0);
        }

        return ALMOST_FULL_SHAPE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return getShape(state, world, pos, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOUNDING);
    }

    @Nullable
    public abstract BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState);

    @Nonnull
    public abstract Vec3i[] getBoundingBlockOffsets();

    public BlockPos getMainPos(BlockState state, LevelReader world, BlockPos pos) {
        // Gets position of main from bounding block based on offset
        if(state.getValue(BOUNDING)
                && world.getBlockEntity(pos) instanceof IHasBoundingBlocks bounding) {

            return pos.subtract(bounding.getOffsetFromMain());
        }

        // Returns current position as block is main
        else {
            return pos;
        }
    }

    @Override
    public boolean canSurvive(BlockState state, @NotNull LevelReader world, @NotNull BlockPos pos) {
        // Returns if main block is present for bounding blocks
        if (state.getValue(BOUNDING)) {
            return world.getBlockState(getMainPos(state, world, pos)).getBlock() == state.getBlock();
        }

        // Returns false for main block if any bounding blocks are not present
        // Does not apply for initial placement
        else if (world.getBlockEntity(pos) instanceof IHasBoundingBlocks blockEntity
                && blockEntity.getBoundingPlaced()) {

            for (Vec3i offset : getBoundingBlockOffsets()) {
                if (world.isEmptyBlock(pos.offset(offset))) {
                    return false;
                }
            }
        }

        // Returns false for main block if any bounding positions are not empty
        // Only applies to initial placement
        else {
            for (Vec3i offset : getBoundingBlockOffsets()) {
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
        if (level.getBlockEntity(mainPos) instanceof IHasBoundingBlocks blockEntity) {
            blockEntity.setMainBlockRemovalLock(true);
        }

        for (Vec3i offset : getBoundingBlockOffsets()) {
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
        if (level.getBlockEntity(mainPos) instanceof IHasBoundingBlocks blockEntity) {
            blockEntity.setBoundingRemoved(true);
            blockEntity.setMainBlockRemovalLock(false);
        }
    }

    /**
     * Places bounding blocks for the main block
     * @param level main block level
     * @param mainPos main block position
     */
    public void placeBoundingBlocks (Level level, BlockPos mainPos) {
        for (Vec3i offset : getBoundingBlockOffsets()) {
            level.setBlock(mainPos.offset(offset), level.getBlockState(mainPos).setValue(BOUNDING, true), Block.UPDATE_ALL);

            // Sets offset for bounding block entity
            if (level.getBlockEntity(mainPos.offset(offset)) instanceof IHasBoundingBlocks blockEntity) {
                blockEntity.setOffsetFromMain(offset);
            }
        }

        // Saves that all bounding blocks have been placed
        // Used later when checking if main block can survive on updates
        if (level.getBlockEntity(mainPos) instanceof IHasBoundingBlocks blockEntity){
            blockEntity.setBoundingPlaced(true);
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
    public void setRotation(Level world, BlockPos pos, Direction rotation) {
        // Only allows rotation of main block
        if (!world.getBlockState(pos).getValue(BOUNDING)) {
            // Prevents main block from being removed gratuitously when bounding blocks are removed
            // from setting the rotation of the main block
            if (world.getBlockEntity(pos) instanceof IHasBoundingBlocks blockEntity) {
                blockEntity.setMainBlockRemovalLock(true);

                super.setRotation(world, pos, rotation);

                blockEntity.setMainBlockRemovalLock(false);
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
    public void onPlace(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pOldState, boolean pIsMoving) {
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
                && world.getBlockEntity(pos) instanceof IHasBoundingBlocks blockEntity
                && !blockEntity.getBoundingRemoved()) {

            removeBoundingBlocks(state, world, pos, null);
        }

        // Removes main block if a bounding block was forcefully removed separately
        // Does not apply when bounding blocks are removed through removeBoundingBlocks method
        else if (world.getBlockEntity(getMainPos(state, world, pos)) instanceof IHasBoundingBlocks blockEntity
                && !blockEntity.getMainBlockRemovalLock()) {

            world.removeBlock(getMainPos(state, world, pos), false);
        }

        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult brtr) {
        // Prevents any interactions with bounding blocks
        if (state.getValue(BOUNDING)) {
            return InteractionResult.FAIL;
        }

        return super.useWithoutItem(state, world, pos, player, brtr);
    }
}
