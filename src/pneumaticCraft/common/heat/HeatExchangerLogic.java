package pneumaticCraft.common.heat;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.common.network.GuiSynced;

public class HeatExchangerLogic implements IHeatExchangerLogic{
    private final Set<IHeatExchangerLogic> hullExchangers = new HashSet<IHeatExchangerLogic>();
    private final Set<IHeatExchangerLogic> connectedExchangers = new HashSet<IHeatExchangerLogic>();
    @GuiSynced
    private double temperature = 295;//degrees Kelvin, 20 degrees by default.
    private double thermalResistance = 1;
    private double thermalCapacity = 1;
    private static boolean isAddingOrRemovingLogic;

    @Override
    public void initializeAsHull(World world, int x, int y, int z, ForgeDirection... validSides){
        for(IHeatExchangerLogic logic : hullExchangers) {
            removeConnectedExchanger(logic);
        }
        hullExchangers.clear();
        for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            if(isSideValid(validSides, d)) {
                IHeatExchangerLogic logic = HeatExchangerManager.getInstance().getLogic(world, x + d.offsetX, y + d.offsetY, z + d.offsetZ, d.getOpposite());
                if(logic != null) {
                    hullExchangers.add(logic);
                    addConnectedExchanger(logic);
                }
            }
        }
    }

    private boolean isSideValid(ForgeDirection[] validSides, ForgeDirection side){
        if(validSides.length == 0) return true;
        for(ForgeDirection d : validSides) {
            if(d == side) return true;
        }
        return false;
    }

    @Override
    public void addConnectedExchanger(IHeatExchangerLogic exchanger){
        connectedExchangers.add(exchanger);
        if(!isAddingOrRemovingLogic) {
            isAddingOrRemovingLogic = true;
            exchanger.addConnectedExchanger(this);
            isAddingOrRemovingLogic = false;
        }
    }

    @Override
    public void removeConnectedExchanger(IHeatExchangerLogic exchanger){
        connectedExchangers.remove(exchanger);
        if(!isAddingOrRemovingLogic) {
            isAddingOrRemovingLogic = true;
            exchanger.removeConnectedExchanger(this);
            isAddingOrRemovingLogic = false;
        }
    }

    @Override
    public double getTemperature(){
        return temperature;
    }

    @Override
    public void setTemperature(double temperature){
        this.temperature = temperature;
    }

    @Override
    public void setThermalResistance(double thermalResistance){
        this.thermalResistance = thermalResistance;
    }

    @Override
    public double getThermalResistance(){
        return thermalResistance;
    }

    @Override
    public void setThermalCapacity(double capacity){
        thermalCapacity = capacity;
    }

    @Override
    public double getThermalCapacity(){
        return thermalCapacity;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        tag.setDouble("temperature", temperature);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        temperature = tag.getDouble("temperature");
    }

    @Override
    public void update(){
        if(getThermalCapacity() < 0.1D) {
            temperature = 295;
            return;
        }
        for(IHeatExchangerLogic logic : connectedExchangers) {
            if(logic.getThermalCapacity() < 0.1D) {
                logic.setTemperature(295);
                continue;
            }
            double deltaTemp = logic.getTemperature() - getTemperature();

            double totalResistance = thermalResistance + logic.getThermalResistance();
            deltaTemp /= getTickingHeatExchangers();//As the connected logics also will tick, we should prevent dispersing more when more are connected.
            deltaTemp /= totalResistance;

            double maxDeltaTemp = (logic.getTemperature() * logic.getThermalCapacity() - temperature * getThermalCapacity()) / 2;//Calculate the heat needed to exactly equalize the heat.
            if(maxDeltaTemp >= 0 && deltaTemp > maxDeltaTemp || maxDeltaTemp <= 0 && deltaTemp < maxDeltaTemp) deltaTemp = maxDeltaTemp;
            temperature += deltaTemp / getThermalCapacity();
            logic.setTemperature(logic.getTemperature() - deltaTemp / logic.getThermalCapacity());
        }
    }

    private int getTickingHeatExchangers(){
        int tickingHeatExchangers = 1;
        for(IHeatExchangerLogic logic : connectedExchangers) {
            if(logic instanceof HeatExchangerLogic) tickingHeatExchangers++;
        }
        return tickingHeatExchangers;
    }

    @Override
    public void addHeat(double amount){
        temperature += amount / getThermalCapacity();
        temperature = Math.max(0, Math.min(2273, temperature));
    }

}
