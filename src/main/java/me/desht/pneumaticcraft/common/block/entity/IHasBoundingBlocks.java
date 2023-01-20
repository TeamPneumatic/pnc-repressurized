package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.common.block.AbstractPNCBlockWithBoundingBlocks;
import me.desht.pneumaticcraft.common.util.BoundingBlockEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.block.AbstractPNCBlockWithBoundingBlocks.BOUNDING;

/**
 * Implement for Block Entities tied to Blocks that extend {@link AbstractPNCBlockWithBoundingBlocks}
 */
@FunctionalInterface
public interface IHasBoundingBlocks {
    /**
     * Gets the {@link BoundingBlockEntityData} linked to the implementing block entity which stores all the data relevant to bounding blocks for the block entity
     * @return the {@link BoundingBlockEntityData} linked to the implementing block entity
     */
    @Nonnull
    BoundingBlockEntityData getBoundingBlockEntityData();

    /**
     * Returns the offset of the calling block entity from the main block entity
     * <br>
     * An offset of 0,0,0 means the calling block entity is the main block entity
     *
     * @return the offset of the calling block entity from the main block entity
     */
    @Nonnull
    default Vec3i getOffsetFromMain() {
        return getBoundingBlockEntityData().offsetFromMain;
    }

    /**
     * Sets the offset of the calling block entity from the main block entity
     * <br>
     * An offset of 0,0,0 means the calling block entity is the main block entity
     * @param offsetFromMain the offset of the calling block entity from the main block entity
     */
    default void setOffsetFromMain(@Nonnull Vec3i offsetFromMain) {
        getBoundingBlockEntityData().offsetFromMain = offsetFromMain;
    }

    /**
     * Returns if all bounding blocks have been placed
     * <br>
     * Called in {@link AbstractPNCBlockWithBoundingBlocks#canSurvive(BlockState,LevelReader, BlockPos) canSurvive()} to ensure the main block survival isn't considered until after all bounding blocks have been placed
     * @return if all bounding blocks have been placed
     */
    default boolean getBoundingPlaced() {
        return getBoundingBlockEntityData().boundingPlaced;
    }

    /**
     * Sets if all bounding blocks have been placed
     * <br>
     * Set to true after placing all bounding blocks in {@link AbstractPNCBlockWithBoundingBlocks#placeBoundingBlocks(Level, BlockPos) placeBoundingBlocks()}
     * @param boundingPlaced if all bounding blocks have been placed
     */
    default void setBoundingPlaced(boolean boundingPlaced) {
        getBoundingBlockEntityData().boundingPlaced = boundingPlaced;
    }

    /**
     * Returns if all bounding blocks have been removed
     * Called in {@link AbstractPNCBlockWithBoundingBlocks#onRemove(BlockState, Level, BlockPos, BlockState, boolean) onRemove()} to check if bounding blocks need to be removed when the main block is removed, preventing redundant removal of bounding blocks
     * @return if all bounding blocks have been removed
     */
    default boolean getBoundingRemoved() {
        return getBoundingBlockEntityData().boundingRemoved;
    }

    /**
     * Sets if all bounding blocks have been removed
     * <br>
     * Set to true after removing all bounding blocks in {@link AbstractPNCBlockWithBoundingBlocks#removeBoundingBlocks(BlockState, Level, BlockPos, Player) removeBoundingBlocks()}
     * @param boundingRemoved if all bounding blocks have been removed
     */
    default void setBoundingRemoved(boolean boundingRemoved) {
        getBoundingBlockEntityData().boundingRemoved = boundingRemoved;
    }

    /**
     * Returns if the main block is locked from being automatically removed
     * <br>
     * Called in {@link AbstractPNCBlockWithBoundingBlocks#onRemove(BlockState, Level, BlockPos, BlockState, boolean) onRemove()} when any bounding blocks are removed.
     * @return if the main block is locked from being automatically removed
     */
    default boolean getMainBlockRemovalLock() {
        return getBoundingBlockEntityData().mainBlockRemovalLock;
    }

    /**
     * Sets if the main block is locked from being automatically removed
     * <br>
     * Set in {@link AbstractPNCBlockWithBoundingBlocks#removeBoundingBlocks(BlockState, Level, BlockPos, Player) removeBoundingBlocks()} and {@link AbstractPNCBlockWithBoundingBlocks#setRotation(Level, BlockPos, Direction) setRotation()} to true at the start of the methods, and then to false at the end of the methods.
     * @param mainBlockRemovalLock if the main block is locked from being automatically removed
     */
    default void setMainBlockRemovalLock(boolean mainBlockRemovalLock) {
        getBoundingBlockEntityData().mainBlockRemovalLock = mainBlockRemovalLock;
    }

    /**
     * Returns if passed block entity is a bounding block entity
     * @param blockEntity block entity to check if a bounding block entity
     * @return if passed block entity is a bounding block entity
     */
    default boolean isBounding(BlockEntity blockEntity) {
        return blockEntity.getBlockState().getValue(BOUNDING);
    }

    /**
     * Finds the main block entity for the passed block entity
     * @param blockEntity block entity to find the main block entity of
     * @return the main block entity of the passed block entity
     */
    default BlockEntity getMain(BlockEntity blockEntity)
    {
        // Finds main block entity for passed bounding block
        if (isBounding(blockEntity)) {
            return Objects.requireNonNullElse(
                    blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos().subtract(getOffsetFromMain())),
                    blockEntity);
        }

        // Returns passed block entity as main block
        else {
            return blockEntity;
        }
    }
}
