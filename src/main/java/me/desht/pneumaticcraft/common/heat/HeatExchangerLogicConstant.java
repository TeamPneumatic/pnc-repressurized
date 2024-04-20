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

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.function.BiPredicate;

/**
 * Used for blocks like lava/ice, which have a constant heat. These are effectively an infinite heat source/sink,
 * unless there is a transitioning heat behaviour attached.
 */
public class HeatExchangerLogicConstant implements IHeatExchangerLogic {
    private final double temperature;
    private final double thermalResistance;

    public HeatExchangerLogicConstant(double temperature, double thermalResistance) {
        this.temperature = temperature;
        this.thermalResistance = thermalResistance;
    }

    @Override
    public void tick() {
    }

    @Override
    public void initializeAsHull(Level level, BlockPos pos, BiPredicate<LevelAccessor,BlockPos> loseHeatToAir, Direction... validSides) {
    }

    @Override
    public void initializeAmbientTemperature(Level level, BlockPos pos) {
    }

    @Override
    public void setTemperature(double temperature) {
    }

    @Override
    public double getAmbientTemperature() {
        return temperature;
    }

    @Override
    public double getTemperature() {
        return temperature;
    }

    @Override
    public int getTemperatureAsInt() {
        return (int) temperature;
    }

    @Override
    public void setThermalResistance(double thermalResistance) {
    }

    @Override
    public double getThermalResistance() {
        return thermalResistance;
    }


    @Override
    public void setThermalCapacity(double capacity) {
    }

    @Override
    public double getThermalCapacity() {
        return 1000;
    }

    @Override
    public void addHeat(double amount) {
    }

    @Override
    public boolean isSideConnected(Direction side) {
        return true;
    }
}
