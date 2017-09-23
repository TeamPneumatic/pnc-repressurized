package me.desht.pneumaticcraft.common.heat;

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourManager;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class HeatExchangerLogic implements IHeatExchangerLogic {
    private final Set<IHeatExchangerLogic> hullExchangers = new HashSet<IHeatExchangerLogic>();
    private final Set<IHeatExchangerLogic> connectedExchangers = new HashSet<IHeatExchangerLogic>();
    private List<HeatBehaviour> behaviours = new ArrayList<HeatBehaviour>();
    private List<HeatBehaviour> newBehaviours;//Required to prevent a CME
    @GuiSynced
    private double temperature = 295;//degrees Kelvin, 20 degrees by default.
    private double thermalResistance = 1;
    private double thermalCapacity = 1;
    private static boolean isAddingOrRemovingLogic;

    @Override
    public void initializeAsHull(World world, BlockPos pos, EnumFacing... validSides) {
        if (world.isRemote) return;
        for (IHeatExchangerLogic logic : hullExchangers) {
            removeConnectedExchanger(logic);
        }
        hullExchangers.clear();
        newBehaviours = new ArrayList<HeatBehaviour>();
        for (EnumFacing d : EnumFacing.VALUES) {
            if (isSideValid(validSides, d)) {
                HeatBehaviourManager.getInstance().addHeatBehaviours(world, pos.offset(d), this, newBehaviours);
                IHeatExchangerLogic logic = HeatExchangerManager.getInstance().getLogic(world, pos.offset(d), d.getOpposite());
                if (logic != null) {
                    hullExchangers.add(logic);
                    addConnectedExchanger(logic);
                }
            }
        }
    }

    private boolean isSideValid(EnumFacing[] validSides, EnumFacing side) {
        if (validSides.length == 0) return true;
        for (EnumFacing d : validSides) {
            if (d == side) return true;
        }
        return false;
    }

    @Override
    public void addConnectedExchanger(IHeatExchangerLogic exchanger) {
        connectedExchangers.add(exchanger);
        if (!isAddingOrRemovingLogic) {
            isAddingOrRemovingLogic = true;
            exchanger.addConnectedExchanger(this);
            isAddingOrRemovingLogic = false;
        }
    }

    @Override
    public void removeConnectedExchanger(IHeatExchangerLogic exchanger) {
        connectedExchangers.remove(exchanger);
        if (!isAddingOrRemovingLogic) {
            isAddingOrRemovingLogic = true;
            exchanger.removeConnectedExchanger(this);
            isAddingOrRemovingLogic = false;
        }
    }

    @Override
    public double getTemperature() {
        return temperature;
    }

    @Override
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public void setThermalResistance(double thermalResistance) {
        this.thermalResistance = thermalResistance;
    }

    @Override
    public double getThermalResistance() {
        return thermalResistance;
    }

    @Override
    public void setThermalCapacity(double capacity) {
        thermalCapacity = capacity;
    }

    @Override
    public double getThermalCapacity() {
        return thermalCapacity;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setDouble("temperature", temperature);
        NBTTagList tagList = new NBTTagList();
        for (HeatBehaviour behaviour : behaviours) {
            NBTTagCompound t = new NBTTagCompound();
            t.setString("id", behaviour.getId());
            behaviour.writeToNBT(t);
            tagList.appendTag(t);
        }
        tag.setTag("behaviours", tagList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        temperature = tag.getDouble("temperature");
        behaviours.clear();
        NBTTagList tagList = tag.getTagList("behaviours", 10);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound t = tagList.getCompoundTagAt(i);
            HeatBehaviour behaviour = HeatBehaviourManager.getInstance().getBehaviourForId(t.getString("id"));
            if (behaviour != null) {
                behaviour.readFromNBT(t);
                behaviours.add(behaviour);
            }
        }
    }

    @Override
    public void update() {
        if (getThermalCapacity() < 0.1D) {
            temperature = 295;
            return;
        }
        if (newBehaviours != null) {
            List<HeatBehaviour> oldBehaviours = behaviours;
            behaviours = newBehaviours;
            newBehaviours = null;
            for (HeatBehaviour oldBehaviour : oldBehaviours) {//Transfer over equal heat behaviour's info.
                int equalBehaviourIndex = behaviours.indexOf(oldBehaviour);
                if (equalBehaviourIndex >= 0) {
                    NBTTagCompound tag = new NBTTagCompound();
                    oldBehaviour.writeToNBT(tag);
                    behaviours.get(equalBehaviourIndex).readFromNBT(tag);
                }
            }
        }
        Iterator<HeatBehaviour> iterator = behaviours.iterator();
        while (iterator.hasNext()) {
            HeatBehaviour behaviour = iterator.next();
            if (behaviour.getWorld() != null) {//upon loading from NBT the world is null. gets initialized once 'initializeAsHull' is invoked.
                if (behaviour.isApplicable()) {
                    behaviour.update();
                } else {
                    iterator.remove();
                }
            }
        }
        for (IHeatExchangerLogic logic : connectedExchangers) {
            exchange(logic, this, getTickingHeatExchangers());//As the connected logics also will tick, we should prevent dispersing more when more are connected.
        }
    }

    public static void exchange(IHeatExchangerLogic logic, IHeatExchangerLogic logic2) {
        exchange(logic, logic2, 1);
    }

    public static void exchange(IHeatExchangerLogic logic, IHeatExchangerLogic logic2, double dispersionDivider) {
        if (logic.getThermalCapacity() < 0.1D) {
            logic.setTemperature(295);
            return;
        }
        double deltaTemp = logic.getTemperature() - logic2.getTemperature();

        double totalResistance = logic2.getThermalResistance() + logic.getThermalResistance();
        deltaTemp /= dispersionDivider;
        deltaTemp /= totalResistance;

        double maxDeltaTemp = (logic.getTemperature() * logic.getThermalCapacity() - logic2.getTemperature() * logic2.getThermalCapacity()) / 2;//Calculate the heat needed to exactly equalize the heat.
        if (maxDeltaTemp >= 0 && deltaTemp > maxDeltaTemp || maxDeltaTemp <= 0 && deltaTemp < maxDeltaTemp)
            deltaTemp = maxDeltaTemp;
        logic2.addHeat(deltaTemp);
        logic.addHeat(-deltaTemp);
    }

    private int getTickingHeatExchangers() {
        int tickingHeatExchangers = 1;
        for (IHeatExchangerLogic logic : connectedExchangers) {
            if (logic instanceof HeatExchangerLogic) tickingHeatExchangers++;
        }
        return tickingHeatExchangers;
    }

    @Override
    public void addHeat(double amount) {
        temperature += amount / getThermalCapacity();
        temperature = Math.max(0, Math.min(2273, temperature));
    }

}
