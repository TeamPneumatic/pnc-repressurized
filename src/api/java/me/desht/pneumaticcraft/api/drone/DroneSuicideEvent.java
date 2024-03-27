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

import net.neoforged.bus.api.Event;

/**
 * Event posted on the {@code MinecraftForge.EVENT_BUS} just before a Drone executes a Suicide piece. Used internally by
 * PneumaticCraft to handle Amadron requests.
 */
public class DroneSuicideEvent extends Event {
    public final IDrone drone;

    public DroneSuicideEvent(IDrone drone) {
        this.drone = drone;
    }
}
