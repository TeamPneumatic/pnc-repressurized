/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.item;

import net.minecraft.world.entity.Entity;

/**
 * Implement this and register it via {@code PneumaticRegistry.getItemRegistry().registerMagnetSuppressor() }
 */
@FunctionalInterface
public interface IMagnetSuppressor {
    /**
     * Check if there is a magnet-suppressor near the given entity (which is usually, but not necessarily, an
     * EntityItem).
     *
     * @param e the entity to check
     * @return true if any magnet effects should be suppressed, false otherwise
     */
    boolean shouldSuppressMagnet(Entity e);
}
