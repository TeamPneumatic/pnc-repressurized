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

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public interface IEventSensorSetting extends ISensorSetting {
    /**
     * This method is called when a Forge event of interest is triggered.  Events of interest are:
     * <ul>
     *     <li>{@link PlayerInteractEvent}</li>
     *     <li>{@link ItemEntityPickupEvent}</li>
     *     <li>{@link AttackEntityEvent}</li>
     * </ul>
     * @param event the Forge event (one of PlayerInteractEvent, EntityItemPickupEvent or AttackEntityEvent)
     * @param sensor the Universal Sensor block entity
     * @param range the Universal Sensor's range, in blocks
     * @param textboxText any text which was entered in the sensor configuration's textfield
     * @return the redstone strength which should be emitted
     */
    int emitRedstoneOnEvent(Event event, BlockEntity sensor, int range, String textboxText);

    /**
     * How long should an emitted pulse last for?  5 ticks is a suitable value.
     *
     * @return a redstone pulse length, in ticks
     */
    int getRedstonePulseLength();
}
