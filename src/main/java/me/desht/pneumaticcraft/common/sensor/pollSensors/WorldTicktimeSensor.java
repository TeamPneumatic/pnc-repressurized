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

package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class WorldTicktimeSensor implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "World/Tick time (lag)";
    }

    @Override
    public Set<PNCUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(ModUpgrades.DISPENSER.get());
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public List<String> getDescription() {
        return ImmutableList.of("pneumaticcraft.gui.universalSensor.desc.world_tick_time");
    }

    @Override
    public int getPollFrequency(BlockEntity te) {
        return 40;
    }

    @Override
    public int getRedstoneValue(Level level, BlockPos pos, int sensorRange, String textBoxText) {
        double worldTickTime = mean(Objects.requireNonNull(level.getServer()).getTickTime(level.dimension())) * 1.0E-6D;
        try {
            int redstoneStrength = (int) (worldTickTime * Double.parseDouble(textBoxText));
            return Math.min(15, redstoneStrength);
        } catch (Exception e) {
            return 0;
        }
    }

    private static long mean(long[] values) {
        long sum = Arrays.stream(values).sum();
        return sum / values.length;
    }

    @Override
    public void getAdditionalInfo(List<Component> info) {
        info.add(Component.literal("Tick Resolution"));
    }
}
