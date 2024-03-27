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

package me.desht.pneumaticcraft.api.universal_sensor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.Event;

import java.util.Set;

public interface IBlockAndCoordinateEventSensor extends IBaseSensor {
    /**
     * Extended version of the normal emitRedstoneOnEvent. This method will only invoke with a valid GPS tool, and when
     * all the coordinates are within range.
     *
     * @param event the Forge event (one of PlayerInteractEvent, EntityItemPickupEvent or AttackEntityEvent)
     * @param sensor the Universal Sensor block entity
     * @param range the Universal Sensor's range, in blocks
     * @param positions When a GPS Tool is inserted this contains the position of that tool. If a GPS Area Tool is
     *                  inserted this is set of all positions in that area.
     * @return the redstone level that should be emitted
     */
    int emitRedstoneOnEvent(Event event, BlockEntity sensor, int range, Set<BlockPos> positions);

    /**
     * See {@link IEventSensorSetting#getRedstonePulseLength()}
     *
     * @return a redstone pulse length, in ticks
     */
    int getRedstonePulseLength();
}
