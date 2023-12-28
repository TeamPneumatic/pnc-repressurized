package me.desht.pneumaticcraft.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

public final class CapabilityCache<T> {
    private LazyOptional<T> cachedCapability;
    private final NonNullConsumer<LazyOptional<T>> capabilityInvalidationListener;

    public CapabilityCache() {
        this.cachedCapability = LazyOptional.empty();
        this.capabilityInvalidationListener = l -> {
            if (this.cachedCapability != l) {
                return;
            }

            this.cachedCapability = LazyOptional.empty();
        };
    }

    /**
     * Get and cache the capability for a neighbour of {@param blockEntity} in the given {@param direction}.
     * <br/>
     * This is similar to {@link CapabilityCache#get(Capability, ICapabilityProvider, Direction)},
     * but is for supporting the common use case of getting capabilities from a neighbouring block entity.
     *
     * @param capability  Capability to get and cache.
     * @param blockEntity Originating {@link BlockEntity}.
     * @param direction   Direction from {@param blockEntity} to a neighbouring block entity to look for a capability.
     * @return The cached capability.
     */
    public @NotNull LazyOptional<T> getNeighbouring(@NotNull Capability<T> capability, @NotNull BlockEntity blockEntity, @NotNull Direction direction) {
        return getOrCacheCurrent(() -> getNeighbouringCurrent(capability, blockEntity, direction));
    }

    /**
     * Get and cache the capability from the given capability provider.
     * <br/>
     * For getting the capability of an adjacent {@link BlockEntity},
     * use {@link CapabilityCache#getNeighbouring(Capability, BlockEntity, Direction)}.
     *
     * @param capability         Capability to get and cache.
     * @param capabilityProvider Capability provider.
     * @param direction          Direction to get capability from (can be null).
     * @return The cached capability.
     */
    public @NotNull LazyOptional<T> get(@NotNull Capability<T> capability, @NotNull ICapabilityProvider capabilityProvider, Direction direction) {
        return getOrCacheCurrent(() -> getCurrent(capability, capabilityProvider, direction));
    }

    public void clear() {
        this.cachedCapability = LazyOptional.empty();
    }

    @NotNull
    private LazyOptional<T> getOrCacheCurrent(NonNullSupplier<LazyOptional<T>> currentCapabilityGetter) {
        if (this.cachedCapability.isPresent()) {
            return this.cachedCapability;
        }

        LazyOptional<T> currentCapability = currentCapabilityGetter.get();
        if (this.cachedCapability == currentCapability) {
            return currentCapability;
        }

        this.cachedCapability = currentCapability;

        if (currentCapability.isPresent()) {
            currentCapability.addListener(this.capabilityInvalidationListener);
        }

        return currentCapability;
    }

    private @NotNull LazyOptional<T> getNeighbouringCurrent(@NotNull Capability<T> capability, @NotNull BlockEntity blockEntity, @NotNull Direction direction) {
        Level level = blockEntity.getLevel();
        if (level == null) return LazyOptional.empty();

        // We could check if the neighbouring block is loaded to avoid loading unloaded chunks?
        // This would be a behaviour change from current though.

        BlockEntity neighborTE = level.getBlockEntity(blockEntity.getBlockPos().relative(direction));
        if (neighborTE == null) return LazyOptional.empty();

        return getCurrent(capability, neighborTE, direction.getOpposite());
    }

    private @NotNull LazyOptional<T> getCurrent(@NotNull Capability<T> capability, @NotNull ICapabilityProvider capabilityProvider, Direction direction) {
        LazyOptional<T> cap = capabilityProvider.getCapability(capability, direction);
        if (!cap.isPresent()) return LazyOptional.empty();

        return cap;
    }
}
