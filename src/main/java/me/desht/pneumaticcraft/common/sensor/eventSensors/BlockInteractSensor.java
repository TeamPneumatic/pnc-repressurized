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
import me.desht.pneumaticcraft.api.universal_sensor.IBlockAndCoordinateEventSensor;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.Set;

public class BlockInteractSensor implements IBlockAndCoordinateEventSensor {

    @Override
    public String getSensorPath() {
        return "Player/Right Click Block";
    }

    @Override
    public Set<PNCUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(ModUpgrades.BLOCK_TRACKER.get());
    }

    @Override
    public boolean needsTextBox() {
        return false;
    }

    @Override
    public int emitRedstoneOnEvent(Event event, BlockEntity sensor, int range, Set<BlockPos> positions) {
        if (event instanceof PlayerInteractEvent interactEvent) {
            return positions.contains(interactEvent.getPos()) ? 15 : 0;
        }
        return 0;
    }

    @Override
    public int getRedstonePulseLength() {
        return 5;
    }
}
