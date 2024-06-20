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

package me.desht.pneumaticcraft.api.drone;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

/**
 * Some methods to control a drone-like object's movement. "Drone-like" means it could be a real Drone, or a
 * Programmable Controller.
 * <p>
 * Retrieve an instance of this via {@link IDrone#getPathNavigator()}.
 */
@ApiStatus.NonExtendable
public interface IPathNavigator {
    /**
     * Instruct the drone to move to the given position. Note that any currently-running program may subsequently
     * override this. Note that if the drone cannot path to the given position, it may attempt to teleport.
     * @param x target X position
     * @param y target Y position
     * @param z target Z position
     * @return true if the drone is able to path to the given position, false otherwise
     */
    boolean moveToXYZ(double x, double y, double z);

    /**
     * Instruct the drone to move to the given entity. Note that any currently-running program may subsequently
     * override this. Note that if the drone cannot path to the given entity, it may attempt to teleport.
     * @param entity the target entity
     * @return true if the drone is able to path to the given entity, false otherwise
     */
    boolean moveToEntity(Entity entity);

    /**
     * Check if the drone has an active path currently set.
     * @return true if the drone has no path, or the current path is complete, false if there is still an active path
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean hasNoPath();
    
    /**
     * Check if the drone is firing up its teleportation device. Drones about to teleport will show some ender-style
     * particles above themselves.
     * @return true if the drone is about to teleport, false otherwise
     */
    boolean isGoingToTeleport();
}
