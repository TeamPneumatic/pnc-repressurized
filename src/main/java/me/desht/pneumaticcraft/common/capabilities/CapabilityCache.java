package me.desht.pneumaticcraft.common.capabilities;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * Generic capability cache.
 * <br/>
 * Typical usage is to {@link CapabilityCache#get()} and use it if it is present, otherwise call {@link CapabilityCache#set(LazyOptional)} to update the capability cache.
 */
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

    public @NotNull LazyOptional<T> get() {
        return this.cachedCapability;
    }

    /**
     * Sets the cached capability.
     * Also handles registering an invalidation listener to clear the cached capability.
     *
     * @param cap Capability to set.
     * @return The capability that was set.
     */
    public @NotNull LazyOptional<T> set(LazyOptional<T> cap) {
        if (this.cachedCapability == cap) {
            return cap;
        }

        if (!cap.isPresent()) {
            this.cachedCapability = LazyOptional.empty();
            return this.cachedCapability;
        }

        this.cachedCapability = cap;
        cap.addListener(this.capabilityInvalidationListener);
        return this.cachedCapability;
    }

    public void clear() {
        this.cachedCapability = LazyOptional.empty();
    }
}
