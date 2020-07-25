package me.desht.pneumaticcraft.common.sensor;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.universal_sensor.*;
import me.desht.pneumaticcraft.common.sensor.eventSensors.BlockInteractSensor;
import me.desht.pneumaticcraft.common.sensor.eventSensors.PlayerAttackSensor;
import me.desht.pneumaticcraft.common.sensor.eventSensors.PlayerItemPickupSensor;
import me.desht.pneumaticcraft.common.sensor.pollSensors.*;
import me.desht.pneumaticcraft.common.sensor.pollSensors.entity.EntityInRangeSensor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;

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

    public Set<EnumUpgrade> getUniversalSensorUpgrades() {
        Set<EnumUpgrade> upgrades = new HashSet<>();
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
        List<EnumUpgrade> upgrades = new ArrayList<>(sensor.getRequiredUpgrades());

        upgrades.sort(Comparator.comparing(upgrade -> I18n.format(upgrade.getName())));

        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < upgrades.size(); i++) {
            ret.append(upgrades.get(i).getName()).append(i < upgrades.size() - 1 ? "_" : "/");
        }

        return ret.toString();
    }

    public Set<EnumUpgrade> getRequiredStacksFromText(String path) {
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
        public List<String> getDescription() {
            return coordinateSensor.getDescription();
        }

        @Override
        public int emitRedstoneOnEvent(Event event, TileEntity tile, int sensorRange, String textboxText) {
            TileEntityUniversalSensor teUs = (TileEntityUniversalSensor) tile;
            Set<BlockPos> positions = teUs.getGPSPositions();
            return positions.isEmpty() ? 0 : coordinateSensor.emitRedstoneOnEvent(event, teUs, sensorRange, positions);
        }

        @Override
        public int getRedstonePulseLength() {
            return coordinateSensor.getRedstonePulseLength();
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawAdditionalInfo(MatrixStack matrixStack, FontRenderer fontRenderer) {
            coordinateSensor.drawAdditionalInfo(matrixStack, fontRenderer);
        }

        @Override
        public Set<EnumUpgrade> getRequiredUpgrades() {
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
        public List<String> getDescription() {
            return coordinateSensor.getDescription();
        }

        @Override
        public int getPollFrequency(TileEntity te) {
            TileEntityUniversalSensor us = (TileEntityUniversalSensor) te;
            Set<BlockPos> positions = us.getGPSPositions();
            int mult = positions.isEmpty() ? 1 : positions.size();
            return coordinateSensor.getPollFrequency() * mult;
        }

        @Override
        public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityUniversalSensor) {
                TileEntityUniversalSensor teUs = (TileEntityUniversalSensor) te;
                Set<BlockPos> positions = teUs.getGPSPositions();
                return positions.isEmpty() ? 0 : coordinateSensor.getRedstoneValue(world, pos, sensorRange, textBoxText, positions);
            }
            return 0;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawAdditionalInfo(MatrixStack matrixStack, FontRenderer fontRenderer) {
            coordinateSensor.drawAdditionalInfo(matrixStack, fontRenderer);
        }

        @Override
        public Set<EnumUpgrade> getRequiredUpgrades() {
            return ImmutableSet.copyOf(coordinateSensor.getRequiredUpgrades());
        }

        @Override
        public boolean needsGPSTool() {
            return true;
        }
    }

}
