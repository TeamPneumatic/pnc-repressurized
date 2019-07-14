package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Used for block like lava/ice, which output a constant heat.
 */
public class HeatExchangerLogicConstant implements IHeatExchangerLogic {
    private final double temperature;
    private final double thermalResistance;

    public HeatExchangerLogicConstant(double temperature) {
        this(temperature, 1);
    }

    public HeatExchangerLogicConstant(double temperature, double thermalResistance) {
        this.temperature = temperature;
        this.thermalResistance = thermalResistance;
    }

    @Override
    public void tick() {
    }

    @Override
    public void initializeAsHull(World world, BlockPos pos, Direction... validSides) {
    }

    @Override
    public void initializeAmbientTemperature(World world, BlockPos pos) {
    }

    @Override
    public void addConnectedExchanger(IHeatExchangerLogic exchanger) {
    }

    @Override
    public void removeConnectedExchanger(IHeatExchangerLogic exchanger) {
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
    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
    }
}
