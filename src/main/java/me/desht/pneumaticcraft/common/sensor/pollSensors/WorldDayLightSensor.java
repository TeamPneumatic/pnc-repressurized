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

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Set;

public class WorldDayLightSensor implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "World/Daylight";
    }

    @Override
    public Set<PNCUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(ModUpgrades.DISPENSER.get());
    }

    @Override
    public boolean needsTextBox() {
        return false;
    }

    @Override
    public int getPollFrequency(BlockEntity te) {
        return 40;
    }

    @Override
    public int getRedstoneValue(Level level, BlockPos pos, int sensorRange, String textBoxText) {
        return updatePower(level, pos);
    }

    private int updatePower(Level worldIn, BlockPos pos) {
        if (worldIn.dimensionType().hasSkyLight()) {
            int i = worldIn.getBrightness(LightLayer.SKY, pos) - worldIn.getSkyDarken();
            float f = worldIn.getSunAngle(1.0F);
            float f1 = f < (float) Math.PI ? 0.0F : (float) Math.PI * 2F;
            f = f + (f1 - f) * 0.2F;
            i = Math.round(i * Mth.cos(f));
            i = Mth.clamp(i, 0, 15);
            return i;
        }
        return 0;
    }
}
