package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
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
    public void update() {
    }

    @Override
    public void initializeAsHull(World world, BlockPos pos, EnumFacing... validSides) {
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
    public double getTemperature() {
        return temperature;
    }

    @Override
    public void setThermalResistance(double thermalResistance) {
    }

    @Override
    public double getThermalResistance() {
        return thermalResistance;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
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

}
