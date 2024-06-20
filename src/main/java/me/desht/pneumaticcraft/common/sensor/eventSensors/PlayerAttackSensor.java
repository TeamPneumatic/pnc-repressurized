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

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public class PlayerAttackSensor extends PlayerEventSensor {

    @Override
    public String getSensorPath() {
        return super.getSensorPath() + "/Player Attack";
    }

    @Override
    public boolean needsTextBox() {
        return false;
    }

    @Override
    public int emitRedstoneOnEvent(Event event, BlockEntity sensor, int range) {
        return event instanceof AttackEntityEvent ? 15 : 0;
    }
}
