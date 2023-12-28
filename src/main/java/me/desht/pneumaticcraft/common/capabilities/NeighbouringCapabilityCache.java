package me.desht.pneumaticcraft.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

public class NeighbouringCapabilityCache<T> {
    private final Map<Direction, CapabilityCache<T>> neighbouringCapabilityCaches;

    public NeighbouringCapabilityCache() {
        this.neighbouringCapabilityCaches = new EnumMap<>(Direction.class);
    }

    /**
     * Get cached capability from a neighbouring {@link BlockEntity}.
     *
     * @param capability  Capability to get and cache.
     * @param blockEntity Originating {@link BlockEntity}.
     * @param direction   Direction from {@param blockEntity} to a neighbouring block entity to look for a capability.
     * @return The cached capability.
     */
    public @NotNull LazyOptional<T> get(@NotNull Capability<T> capability, @NotNull BlockEntity blockEntity, @NotNull Direction direction) {
        CapabilityCache<T> cache = this.neighbouringCapabilityCaches.get(direction);
        if (cache == null) {
            cache = new CapabilityCache<>();
            this.neighbouringCapabilityCaches.put(direction, cache);
        }

        return cache.getNeighbouring(capability, blockEntity, direction);
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
