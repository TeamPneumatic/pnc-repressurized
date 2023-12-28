package me.desht.pneumaticcraft.common.util;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public final class CapabilityUtils {
    private CapabilityUtils() {
    }

    /**
     * Gets capability of a neighbouring {@link BlockEntity}.
     *
     * @param capability  Capability to get.
     * @param blockEntity Originating {@link BlockEntity}.
     * @param direction   Direction from {@param blockEntity} to a neighbouring block entity to look for a capability.
     * @return The capability on the neighbouring {@link BlockEntity}.
     */
    public static <T> @NotNull LazyOptional<T> getNeighbourCap(@NotNull Capability<T> capability, @NotNull BlockEntity blockEntity, @NotNull Direction direction) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return LazyOptional.empty();
        }

        // We could check if the neighbouring block is loaded to avoid loading unloaded chunks?
        // This would be a behaviour change from current though.

        BlockEntity neighbourTE = level.getBlockEntity(blockEntity.getBlockPos().relative(direction));
        if (neighbourTE == null) {
            return LazyOptional.empty();
        }

        return neighbourTE.getCapability(capability, direction.getOpposite());
    }
}
