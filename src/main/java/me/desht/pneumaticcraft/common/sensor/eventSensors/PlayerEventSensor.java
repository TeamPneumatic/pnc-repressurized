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

package me.desht.pneumaticcraft.common.sensor.eventSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.universal_sensor.IEventSensorSetting;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Set;

abstract class PlayerEventSensor implements IEventSensorSetting {

    @Override
    public String getSensorPath() {
        return "Player";
    }

    @Override
    public Set<PNCUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(ModUpgrades.ENTITY_TRACKER.get());
    }

    @Override
    public int emitRedstoneOnEvent(Event event, BlockEntity sensor, int range, String textboxText) {
        if (event instanceof PlayerEvent playerEvent) {
            Player player = playerEvent.getEntity();
            if (Math.abs(player.getX() - sensor.getBlockPos().getX() + 0.5D) < range + 0.5D
                    && Math.abs(player.getY() - sensor.getBlockPos().getY() + 0.5D) < range + 0.5D
                    && Math.abs(player.getZ() - sensor.getBlockPos().getZ() + 0.5D) < range + 0.5D)
            {
                return emitRedstoneOnEvent(playerEvent, sensor, range);
            }
        }
        return 0;
    }

    protected abstract int emitRedstoneOnEvent(PlayerEvent event, BlockEntity sensor, int range);

    @Override
    public int getRedstonePulseLength() {
        return 5;
    }

}
