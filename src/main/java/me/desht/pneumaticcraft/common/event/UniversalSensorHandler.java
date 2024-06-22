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

import me.desht.pneumaticcraft.common.block.entity.utility.UniversalSensorBlockEntity;
import me.desht.pneumaticcraft.common.util.GlobalBlockEntityCacheManager;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class UniversalSensorHandler {
    @SubscribeEvent
    public void onInteraction(PlayerInteractEvent.RightClickBlock event) {
        sendEventToSensors(event.getEntity().level(), event);
    }

    @SubscribeEvent
    public void onInteraction(PlayerInteractEvent.RightClickItem event) {
        sendEventToSensors(event.getEntity().level(), event);
    }

    @SubscribeEvent
    public void onPlayerAttack(AttackEntityEvent event) {
        sendEventToSensors(event.getEntity().level(), event);
    }

    @SubscribeEvent
    public void onItemPickUp(ItemEntityPickupEvent.Post event) {
        sendEventToSensors(event.getItemEntity().level(), event);
    }

    private void sendEventToSensors(Level level, Event event) {
        if (!level.isClientSide) {
            for (UniversalSensorBlockEntity sensor : GlobalBlockEntityCacheManager.getInstance(level).getUniversalSensors()) {
                sensor.onEvent(event);
            }
        }
    }
}
