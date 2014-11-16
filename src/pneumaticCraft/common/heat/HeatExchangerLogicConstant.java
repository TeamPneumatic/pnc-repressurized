package pneumaticCraft.common.heat;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.IHeatExchangerLogic;

/**
 * Used for block like lava/ice, which output a constant heat.
 */
public class HeatExchangerLogicConstant implements IHeatExchangerLogic{
    private final double temperature;
    private final double thermalResistance;

    public HeatExchangerLogicConstant(double temperature){
        this(temperature, 1);
    }

    public HeatExchangerLogicConstant(double temperature, double thermalResistance){
        this.temperature = temperature;
        this.thermalResistance = thermalResistance;
    }

    @Override
    public void update(){}

    @Override
    public void initializeAsHull(World world, int x, int y, int z, ForgeDirection... validSides){}

    @Override
    public void addConnectedExchanger(IHeatExchangerLogic exchanger){}

    @Override
    public void removeConnectedExchanger(IHeatExchangerLogic exchanger){}

    @Override
    public void setTemperature(double temperature){}

    @Override
    public double getTemperature(){
        return temperature;
    }

    @Override
    public void setThermalResistance(double thermalResistance){}

    @Override
    public double getThermalResistance(){
        return thermalResistance;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){}

    @Override
    public void readFromNBT(NBTTagCompound tag){}

    @Override
    public void setThermalCapacity(double capacity){}

    @Override
    public double getThermalCapacity(){
        return 1000;
    }

    @Override
    public void addHeat(double amount){

    }

}
