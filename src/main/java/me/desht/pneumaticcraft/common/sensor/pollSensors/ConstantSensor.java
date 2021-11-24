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
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

public class ConstantSensor implements IPollSensorSetting {

    @Override
    public String getSensorPath() {
        return "Constant";
    }

    @Override
    public Set<EnumUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(EnumUpgrade.DISPENSER);
    }

    @Override
    public int getPollFrequency(TileEntity te) {
        return 1;
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public RangedInteger getTextboxIntRange() {
        return RangedInteger.of(0, 16);
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
        try {
            return Math.min(15, Math.max(0, Integer.parseInt(textBoxText)));
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void getAdditionalInfo(List<ITextComponent> info) {
        info.add(new StringTextComponent("Signal Level"));
    }

    @Override
    public int getAirUsage(World world, BlockPos pos) {
        // it's just a constant redstone signal, let's make it free
        return 0;
    }
}