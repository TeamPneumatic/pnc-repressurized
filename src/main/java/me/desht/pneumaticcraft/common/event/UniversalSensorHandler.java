/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import me.desht.pneumaticcraft.common.util.GlobalTileEntityCacheManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class UniversalSensorHandler {
    @SubscribeEvent
    public void onInteraction(PlayerInteractEvent event) {
        sendEventToSensors(event.getEntity().level, event);
    }

    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
        sendEventToSensors(event.getEntity().level, event);
    }

    @SubscribeEvent
    public void onItemPickUp(EntityItemPickupEvent event) {
        sendEventToSensors(event.getEntity().level, event);
    }

    private void sendEventToSensors(Level world, Event event) {
        if (!world.isClientSide) {
            for (TileEntityUniversalSensor sensor : GlobalTileEntityCacheManager.getInstance().universalSensors) {
                sensor.onEvent(event);
            }
        }
    }
}
