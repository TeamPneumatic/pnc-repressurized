package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.common.util.CapabilityUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

/**
 * Capability cache for neighbours of a {@link BlockEntity}.
 * <br/>
 * Typical usage is to {@link NeighbouringCapabilityCache#get(Direction)} and use it if it is present,
 * otherwise call {@link NeighbouringCapabilityCache#set(Capability, BlockEntity, Direction)} to update the capability cache.
 */
public class NeighbouringCapabilityCache<T> {
    private final Map<Direction, CapabilityCache<T>> neighbouringCapabilityCaches;

    public NeighbouringCapabilityCache() {
        this.neighbouringCapabilityCaches = new EnumMap<>(Direction.class);
    }

    /**
     * Get cached capability for a neighbouring {@link BlockEntity}.
     *
     * @param direction Direction from the original block entity to a neighbouring block entity.
     * @return The cached capability.
     */
    public @NotNull LazyOptional<T> get(@NotNull Direction direction) {
        CapabilityCache<T> cache = this.neighbouringCapabilityCaches.get(direction);
        if (cache == null) {
            return LazyOptional.empty();
        }

        return cache.get();
    }

    /**
     * Sets the cached capability for a neighbouring {@link BlockEntity}.
     * Also handles registering an invalidation listener to clear the cached capability.
     *
     * @param capability  Capability to get and cache.
     * @param blockEntity Originating {@link BlockEntity}.
     * @param direction   Direction from {@param blockEntity} to a neighbouring block entity to look for a capability.
     * @return The cached capability.
     */
    public @NotNull LazyOptional<T> set(@NotNull Capability<T> capability, @NotNull BlockEntity blockEntity, @NotNull Direction direction) {
        CapabilityCache<T> cache = this.neighbouringCapabilityCaches.get(direction);
        if (cache == null) {
            cache = new CapabilityCache<>();
            this.neighbouringCapabilityCaches.put(direction, cache);
        }

        return cache.set(CapabilityUtils.getNeighbourCap(capability, blockEntity, direction));
    }

    public void clear() {
        for (Direction dir : Direction.values()) {
            clear(dir);
        }
    }

    public void clear(@NotNull Direction dir) {
        CapabilityCache<T> cache = this.neighbouringCapabilityCaches.get(dir);
        if (cache == null) {
            return;
        }

        cache.clear();
    }
}
