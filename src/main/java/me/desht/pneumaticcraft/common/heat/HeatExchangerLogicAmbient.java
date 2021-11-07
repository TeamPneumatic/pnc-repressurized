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

package me.desht.pneumaticcraft.common.heat;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.desht.pneumaticcraft.common.config.PNCConfig.Common.Heat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class HeatExchangerLogicAmbient extends HeatExchangerLogicConstant {
    static final double BASE_AMBIENT_TEMP = 300; // Forge-defined temperature of water (see FluidAttributes.Builder)

    private static final HeatExchangerLogicAmbient DEFAULT_AIR_EXCHANGER = new HeatExchangerLogicAmbient(BASE_AMBIENT_TEMP);
    private static final Int2ObjectOpenHashMap<HeatExchangerLogicAmbient> exchangers = new Int2ObjectOpenHashMap<>();

    public static HeatExchangerLogicAmbient atPosition(IWorld world, BlockPos pos) {
        if (Heat.ambientTempBiomeModifier == 0 && Heat.ambientTempHeightModifier == 0) {
            return DEFAULT_AIR_EXCHANGER;
        }

        // biome temp of 0.8 is plains: let's call that the baseline - 300K
        // max (vanilla) is 2.0 for desert / nether, min is -0.5 for snowy taiga mountains
        float t = world.getBiome(pos).getTemperature(pos) - 0.8f;

        // In 1.16.2+, vanilla handles temperature reduction as height increases (but not temperature increase underground)
        int y1 = (int)(world.getSeaLevel() * 0.75f);
        int h = pos.getY() < y1 ? y1 - pos.getY() : 0;

        int temp = (int) (BASE_AMBIENT_TEMP + Heat.ambientTempBiomeModifier * t + Heat.ambientTempHeightModifier * h);
        return exchangers.computeIfAbsent(temp, HeatExchangerLogicAmbient::new);
    }

    public static double getAmbientTemperature(IWorld world, BlockPos pos) {
        return atPosition(world, pos).getAmbientTemperature();
    }

    private HeatExchangerLogicAmbient(double temperature) {
        super(temperature, Heat.airThermalResistance);
    }
}
