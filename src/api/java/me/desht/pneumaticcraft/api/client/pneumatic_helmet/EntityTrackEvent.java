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

package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired when a helmet Block Tracker is about to track an entity. Can be canceled to prevent tracking.
 * Posted on MinecraftForge.EVENT_BUS
 *
 * @author MineMaarten
 */
public class EntityTrackEvent extends Event implements ICancellableEvent {
    public final Entity trackingEntity;

    public EntityTrackEvent(Entity trackingEntity) {
        this.trackingEntity = trackingEntity;
    }

}
