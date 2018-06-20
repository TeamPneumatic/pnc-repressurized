package me.desht.pneumaticcraft.api.item;

import net.minecraft.entity.Entity;

/**
 * Implement this and register it via {@code PneumaticRegistry.getItemRegistry().registerMagnetSuppressor() }
 */
public interface IMagnetSuppressor {
    /**
     * Check if there is magnet-suppressor near the given entity (which is usually, but not necessarily, an
     * EntityItem).
     *
     * @param e the entity to check
     * @return true if any magnet effects should be suppressed, false otherwise
     */
    boolean shouldSuppressMagnet(Entity e);
}
