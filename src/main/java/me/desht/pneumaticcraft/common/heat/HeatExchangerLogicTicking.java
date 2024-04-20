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

import me.desht.pneumaticcraft.api.heat.HeatBehaviour;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.heat.TemperatureListener;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourManager;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;
import java.util.function.BiPredicate;

public class HeatExchangerLogicTicking implements IHeatExchangerLogic {
    private final Set<IHeatExchangerLogic> hullExchangers = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<IHeatExchangerLogic> connectedExchangers = Collections.newSetFromMap(new IdentityHashMap<>());
    private List<HeatBehaviour> behaviours = new ArrayList<>();
    private List<HeatBehaviour> newBehaviours; // required to prevent CME problems
    private double ambientTemperature = -1;
    private double temperature = HeatExchangerLogicAmbient.BASE_AMBIENT_TEMP;  // degrees Kelvin, 300K by default
    @GuiSynced
    private int temperatureInt = (int) HeatExchangerLogicAmbient.BASE_AMBIENT_TEMP;
    private double thermalResistance = 1;
    private double thermalCapacity = 1;
    private final BitSet connections = new BitSet(6);
    private final List<TemperatureListener> temperatureCallbacks = new ArrayList<>();

    @Override
    public void initializeAsHull(Level level, BlockPos pos, BiPredicate<LevelAccessor,BlockPos> blockFilter, Direction... validSides) {
        if (ambientTemperature < 0) {
            initializeAmbientTemperature(level, pos);
        }

        if (level.isClientSide) return;

        for (IHeatExchangerLogic logic : hullExchangers) {
            removeConnectedExchanger(logic);
        }
        hullExchangers.clear();
        newBehaviours = new ArrayList<>();
        connections.clear();
        for (Direction dir : validSides) {
            if (HeatBehaviourManager.getInstance().addHeatBehaviours(level, pos.relative(dir), dir, blockFilter, this, newBehaviours) > 0) {
                connections.set(dir.get3DDataValue());
            }
            HeatExchangerManager.getInstance().getLogic(level, pos.relative(dir), dir.getOpposite(), blockFilter).ifPresent(logic -> {
                hullExchangers.add(logic);
                addConnectedExchanger(logic);
                connections.set(dir.get3DDataValue());
            });
        }
    }

    @Override
    public boolean isSideConnected(Direction side) {
        return connections.get(side.get3DDataValue());
    }

    @Override
    public void addConnectedExchanger(IHeatExchangerLogic exchanger, boolean reciprocate) {
        connectedExchangers.add(exchanger);
        if (reciprocate) {
            exchanger.addConnectedExchanger(this, false);
        }
    }

    @Override
    public void removeConnectedExchanger(IHeatExchangerLogic exchanger, boolean reciprocate) {
        connectedExchangers.remove(exchanger);
        if (reciprocate) {
            exchanger.removeConnectedExchanger(this, false);
        }
    }

    @Override
    public void initializeAmbientTemperature(Level level, BlockPos pos) {
        ambientTemperature = HeatExchangerLogicAmbient.atPosition(level, pos).getAmbientTemperature();
    }

    @Override
    public double getTemperature() {
        return temperature;
    }

    @Override
    public int getTemperatureAsInt() { return temperatureInt; }

    @Override
    public void setTemperature(double temperature) {
        if (temperature != this.temperature) {
            double prevTemperature = this.temperature;
            this.temperature = temperature;
            this.temperatureInt = (int) temperature;
            if (Math.abs(prevTemperature - temperature) > 0.001) {
                temperatureCallbacks.forEach(l -> l.onTemperatureChanged(prevTemperature, temperature));
            }
        }
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
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("temperature", temperature);
        ListTag tagList = new ListTag();
        for (HeatBehaviour behaviour : behaviours) {
            CompoundTag t = behaviour.serializeNBT();
            t.putString("id", behaviour.getId().toString());
            tagList.add(t);
        }
        tag.put("behaviours", tagList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        temperature = nbt.getDouble("temperature");
        temperatureInt = (int) temperature;
        behaviours.clear();
        ListTag tagList = nbt.getList("behaviours", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag t = tagList.getCompound(i);
            HeatBehaviour behaviour = HeatBehaviourManager.getInstance().createBehaviour(new ResourceLocation(t.getString("id")));
            if (behaviour != null) {
                behaviour.deserializeNBT(t);
                behaviours.add(behaviour);
            }
        }
    }

    @Override
    public void tick() {
        temperatureInt = (int) temperature;

        if (getThermalCapacity() < 0.1D) {
            setTemperature(ambientTemperature);
            return;
        }
        if (newBehaviours != null) {
            List<HeatBehaviour> oldBehaviours = behaviours;
            behaviours = newBehaviours;
            newBehaviours = null;
            // Transfer over equal heat behaviour's info.
            for (HeatBehaviour oldBehaviour : oldBehaviours) {
                int equalBehaviourIndex = behaviours.indexOf(oldBehaviour);
                if (equalBehaviourIndex >= 0) {
                    behaviours.get(equalBehaviourIndex).deserializeNBT(oldBehaviour.serializeNBT());
                }
            }
        }
        Iterator<HeatBehaviour> iterator = behaviours.iterator();
        while (iterator.hasNext()) {
            HeatBehaviour behaviour = iterator.next();
            // upon loading from NBT the world is null. gets initialized once 'initializeAsHull' is invoked.
            if (behaviour.getWorld() != null) {
                if (behaviour.isApplicable()) {
                    behaviour.tick();
                } else {
                    iterator.remove();
                }
            }
        }
        for (IHeatExchangerLogic logic : connectedExchangers) {
            // Counting the connected ticking heat exchangers here is important, since they will all tick;
            // this count acts as a divider so the total heat dispersal is constant
            exchange(logic, this, getTickingHeatExchangers());
        }
    }

    @Override
    public double getAmbientTemperature() {
        return ambientTemperature;
    }

    @Override
    public void addTemperatureListener(TemperatureListener listener) {
        temperatureCallbacks.add(listener);
    }

    @Override
    public void removeTemperatureListener(TemperatureListener listener) {
        temperatureCallbacks.remove(listener);
    }

    public static void exchange(IHeatExchangerLogic logic, IHeatExchangerLogic logic2) {
        exchange(logic, logic2, 1);
    }

    private static void exchange(IHeatExchangerLogic logic, IHeatExchangerLogic logic2, double dispersionDivider) {
        if (logic.getThermalCapacity() < 0.1D) {
            logic.setTemperature(logic.getAmbientTemperature());
            return;
        }
        double deltaTemp = logic.getTemperature() - logic2.getTemperature();

        double totalResistance = logic2.getThermalResistance() + logic.getThermalResistance();
        deltaTemp /= dispersionDivider;
        deltaTemp /= totalResistance;

        // Calculate the heat needed to exactly equalize the heat.
        double maxDeltaTemp = (logic.getTemperature() * logic.getThermalCapacity() - logic2.getTemperature() * logic2.getThermalCapacity()) / 2;
        if (maxDeltaTemp >= 0 && deltaTemp > maxDeltaTemp || maxDeltaTemp <= 0 && deltaTemp < maxDeltaTemp)
            deltaTemp = maxDeltaTemp;
        logic2.addHeat(deltaTemp);
        logic.addHeat(-deltaTemp);
    }

    private int getTickingHeatExchangers() {
        int tickingHeatExchangers = 1;
        for (IHeatExchangerLogic logic : connectedExchangers) {
            if (logic instanceof HeatExchangerLogicTicking) tickingHeatExchangers++;
        }
        return tickingHeatExchangers;
    }

    @Override
    public void addHeat(double amount) {
        setTemperature(Mth.clamp(temperature + amount / getThermalCapacity(), 0, 2273));
    }

    @Override
    public <T extends HeatBehaviour> Optional<T> getHeatBehaviour(BlockPos pos, Class<T> cls) {
        for (HeatBehaviour behaviour : behaviours) {
            if (behaviour.getPos().equals(pos) && cls.isAssignableFrom(behaviour.getClass())) {
                return Optional.of(cls.cast(behaviour));
            }
        }
        return Optional.empty();
    }
}
