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

package me.desht.pneumaticcraft.common.sensor;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.misc.RangedInt;
import me.desht.pneumaticcraft.api.universal_sensor.*;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.block.entity.utility.UniversalSensorBlockEntity;
import me.desht.pneumaticcraft.common.sensor.eventSensors.BlockInteractSensor;
import me.desht.pneumaticcraft.common.sensor.eventSensors.PlayerAttackSensor;
import me.desht.pneumaticcraft.common.sensor.eventSensors.PlayerItemPickupSensor;
import me.desht.pneumaticcraft.common.sensor.pollSensors.*;
import me.desht.pneumaticcraft.common.sensor.pollSensors.entity.EntityInRangeSensor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.Event;

import java.util.*;

public class SensorHandler implements ISensorRegistry {
    private static final SensorHandler INSTANCE = new SensorHandler();

    public static SensorHandler getInstance() {
        return INSTANCE;
    }

    // TODO forge registry for these
    public void init() {
        registerSensor(new EntityInRangeSensor());
        registerSensor(new PlayerAttackSensor());
        registerSensor(new PlayerItemPickupSensor());
        registerSensor(new BlockInteractSensor());
        registerSensor(new WorldDayLightSensor());
        registerSensor(new WorldRainingSensor());
        registerSensor(new WorldTimeSensor());
        registerSensor(new WorldWeatherForecaster());
        registerSensor(new WorldPlayersInServerSensor());
        registerSensor(new WorldTicktimeSensor());
        registerSensor(new WorldGlobalVariableSensor());
        registerSensor(new WorldGlobalVariableAnalogSensor());
        registerSensor(new BlockPresenceSensor());
        registerSensor(new BlockComparatorSensor());
        registerSensor(new BlockRedstoneSensor());
        registerSensor(new BlockLightLevelSensor());
        registerSensor(new BlockHeatSensor());
        registerSensor(new ConstantSensor());
        registerSensor(new TwitchStreamerSensor());
        registerSensor(new PlayerHealthSensor());
    }

    private final Map<String, ISensorSetting> sensors = new LinkedHashMap<>();

    public ISensorSetting getSensorFromPath(String buttonPath) {
        return sensors.get(buttonPath);
    }

    private List<ISensorSetting> getSensorsFromPath(String buttonPath) {
        List<ISensorSetting> matchingSensors = new ArrayList<>();
        for (Map.Entry<String, ISensorSetting> entry : sensors.entrySet()) {
            if (entry.getKey().startsWith(buttonPath)) {
                matchingSensors.add(entry.getValue());
            }
        }
        return matchingSensors;
    }

    public ISensorSetting getSensorByIndex(int index) {
        return getSensorsFromPath("").get(index);
    }

    public String[] getSensorNames() {
        String[] sensorNames = new String[sensors.size()];
        Iterator<String> iterator = sensors.keySet().iterator();
        for (int i = 0; i < sensorNames.length; i++) {
            String sensorPath = iterator.next();
            sensorNames[i] = sensorPath.substring(sensorPath.lastIndexOf('/') + 1);
        }
        return sensorNames;
    }

    /**
     * Get a sensor by its basename
     *
     * @param name the last part of the sensor path
     * @return the sensor settings
     */
    public ISensorSetting getSensorForName(String name) {
        String[] sensorNames = getSensorNames();
        for (int i = 0; i < sensorNames.length; i++) {
            if (sensorNames[i].equals(name)) return getSensorByIndex(i);
        }
        return null;
    }

    public Set<PNCUpgrade> getUniversalSensorUpgrades() {
        Set<PNCUpgrade> upgrades = new HashSet<>();
        for (ISensorSetting sensor : sensors.values()) {
            upgrades.addAll(sensor.getRequiredUpgrades());
        }
        return upgrades;
    }

    public String[] getDirectoriesAtLocation(String path) {
        List<String> directories = new ArrayList<>();
        for (String sensorPath : sensors.keySet()) {
            if (sensorPath.startsWith(path) && !sensorPath.equals(path)) {
                //if path equals "entityTracker/player/" and sensor path equals "entityTracker/player/speed", to directories will "speed" be added.
                String[] folders = sensorPath.substring(path.length()).split("/");
                if (folders[0].isEmpty() && folders.length > 1) {
                    if (!directories.contains(folders[1])) directories.add(folders[1]);
                } else {
                    if (!directories.contains(folders[0])) directories.add(folders[0]);
                }
            }
        }
        String[] directoryArray = directories.toArray(new String[0]);
        Arrays.sort(directoryArray);
        return directoryArray;
    }

    private String getUpgradePrefix(ISensorSetting sensor) {
        List<PNCUpgrade> upgrades = new ArrayList<>(sensor.getRequiredUpgrades());

        upgrades.sort(Comparator.comparing(upgrade -> I18n.get(upgrade.getItem().getDescriptionId())));

        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < upgrades.size(); i++) {
            String suffix = i < upgrades.size() - 1 ? "_" : "/";
            ret.append(upgrades.get(i).getId()).append(suffix);
//            PneumaticCraftUtils.getRegistryName(ModUpgrades.UPGRADES.get(), upgrades.get(i))
//                    .ifPresent(regName -> ret.append(regName).append(suffix));
        }

        return ret.toString();
    }

    public Set<PNCUpgrade> getRequiredStacksFromText(String path) {
        List<ISensorSetting> sensors = getSensorsFromPath(path);
        return sensors.isEmpty() ? Collections.emptySet() : sensors.get(0).getRequiredUpgrades();
    }

    @Override
    public void registerSensor(ISensorSetting sensor) {
        String path = getUpgradePrefix(sensor) + sensor.getSensorPath();
        sensors.put(path, sensor);
    }

    @Override
    public void registerSensor(IBlockAndCoordinateEventSensor sensor) {
        registerSensor(new BlockAndCoordinateEventSensor(sensor));
    }

    @Override
    public void registerSensor(IBlockAndCoordinatePollSensor sensor) {
        registerSensor(new BlockAndCoordinatePollSensor(sensor));
    }

    private static class BlockAndCoordinateEventSensor implements IEventSensorSetting {
        private final IBlockAndCoordinateEventSensor coordinateSensor;

        BlockAndCoordinateEventSensor(IBlockAndCoordinateEventSensor sensor) {
            coordinateSensor = sensor;
        }

        @Override
        public String getSensorPath() {
            return coordinateSensor.getSensorPath();
        }

        @Override
        public boolean needsTextBox() {
            return coordinateSensor.needsTextBox();
        }

        @Override
        public RangedInt getTextboxIntRange() {
            return coordinateSensor.getTextboxIntRange();
        }

        @Override
        public List<String> getDescription() {
            return coordinateSensor.getDescription();
        }

        @Override
        public int emitRedstoneOnEvent(Event event, BlockEntity tile, int sensorRange, String textboxText) {
            UniversalSensorBlockEntity teUs = (UniversalSensorBlockEntity) tile;
            Set<BlockPos> positions = teUs.getGPSPositions();
            return positions.isEmpty() ? 0 : coordinateSensor.emitRedstoneOnEvent(event, teUs, sensorRange, positions);
        }

        @Override
        public int getRedstonePulseLength() {
            return coordinateSensor.getRedstonePulseLength();
        }

        @Override
        public void getAdditionalInfo(List<Component> info) {
            coordinateSensor.getAdditionalInfo(info);
        }

        @Override
        public Set<PNCUpgrade> getRequiredUpgrades() {
            return ImmutableSet.copyOf(coordinateSensor.getRequiredUpgrades());
        }

        @Override
        public boolean needsGPSTool() {
            return true;
        }
    }

    private static class BlockAndCoordinatePollSensor implements IPollSensorSetting {
        private final IBlockAndCoordinatePollSensor coordinateSensor;

        BlockAndCoordinatePollSensor(IBlockAndCoordinatePollSensor sensor) {
            coordinateSensor = sensor;
        }

        @Override
        public String getSensorPath() {
            return coordinateSensor.getSensorPath();
        }

        @Override
        public boolean needsTextBox() {
            return coordinateSensor.needsTextBox();
        }

        @Override
        public RangedInt getTextboxIntRange() {
            return coordinateSensor.getTextboxIntRange();
        }

        @Override
        public List<String> getDescription() {
            return coordinateSensor.getDescription();
        }

        @Override
        public int getPollFrequency(BlockEntity te) {
            UniversalSensorBlockEntity us = (UniversalSensorBlockEntity) te;
            Set<BlockPos> positions = us.getGPSPositions();
            int mult = positions.isEmpty() ? 1 : positions.size();
            return coordinateSensor.getPollFrequency() * mult;
        }

        @Override
        public int getRedstoneValue(Level level, BlockPos pos, int sensorRange, String textBoxText) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof UniversalSensorBlockEntity) {
                UniversalSensorBlockEntity teUs = (UniversalSensorBlockEntity) te;
                Set<BlockPos> positions = teUs.getGPSPositions();
                return positions.isEmpty() ? 0 : coordinateSensor.getRedstoneValue(level, pos, sensorRange, textBoxText, positions);
            }
            return 0;
        }

        @Override
        public void getAdditionalInfo(List<Component> info) {
            coordinateSensor.getAdditionalInfo(info);
        }

        @Override
        public Set<PNCUpgrade> getRequiredUpgrades() {
            return ImmutableSet.copyOf(coordinateSensor.getRequiredUpgrades());
        }

        @Override
        public boolean needsGPSTool() {
            return true;
        }
    }

}
