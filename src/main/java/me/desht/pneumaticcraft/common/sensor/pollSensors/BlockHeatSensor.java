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
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.IBlockAndCoordinatePollSensor;
import me.desht.pneumaticcraft.common.heat.HeatExchangerManager;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockHeatSensor implements IBlockAndCoordinatePollSensor {

    @Override
    public String getSensorPath() {
        return "Block/Heat";
    }

    @Override
    public Set<EnumUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(EnumUpgrade.BLOCK_TRACKER);
    }

    @Override
    public int getPollFrequency() {
        return 20;
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public RangedInteger getTextboxIntRange() {
        return new RangedInteger(-273, 2000);  // 0K - 2273K
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText, Set<BlockPos> positions) {
        double temperature = 0D;
        for (BlockPos p : positions) {
            temperature = Math.max(temperature, HeatExchangerManager.getInstance().getLogic(world, p, null)
                    .map(IHeatExchangerLogic::getTemperature).orElse(0d));
            TileEntity te = world.getBlockEntity(p);
            if (te != null) {
                // possibly sided TE?
                for (Direction side : DirectionUtil.VALUES) {
                    temperature = Math.max(temperature, te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, side)
                            .map(IHeatExchangerLogic::getTemperature).orElse(0d));
                }
            }
        }
        return NumberUtils.isCreatable(textBoxText) ?
                temperature - 273 > NumberUtils.toInt(textBoxText) ? 15 : 0 :
                HeatUtil.getComparatorOutput((int) temperature);
    }

    @Override
    public void getAdditionalInfo(List<ITextComponent> info) {
        info.add(xlate("pneumaticcraft.gui.universalSensor.text.thresholdTemp"));
    }
}