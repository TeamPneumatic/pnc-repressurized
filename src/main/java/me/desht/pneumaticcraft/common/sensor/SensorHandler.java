package me.desht.pneumaticcraft.common.sensor;

import me.desht.pneumaticcraft.api.universalSensor.*;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.sensor.eventSensors.BlockInteractSensor;
import me.desht.pneumaticcraft.common.sensor.eventSensors.PlayerAttackSensor;
import me.desht.pneumaticcraft.common.sensor.eventSensors.PlayerItemPickupSensor;
import me.desht.pneumaticcraft.common.sensor.pollSensors.*;
import me.desht.pneumaticcraft.common.sensor.pollSensors.entity.EntityInRangeSensor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.Rectangle;

import java.util.*;

public class SensorHandler implements ISensorRegistry {
    private static final SensorHandler INSTANCE = new SensorHandler();

    public static SensorHandler getInstance() {
        return INSTANCE;
    }

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
        registerSensor(new BlockPresenceSensor());
        registerSensor(new BlockMetadataSensor());
        registerSensor(new BlockComparatorSensor());
        registerSensor(new BlockRedstoneSensor());
        registerSensor(new BlockLightLevelSensor());
        registerSensor(new BlockHeatSensor());
        registerSensor(new UserSetSensor());
        registerSensor(new TwitchStreamerSensor());
        registerSensor(new PlayerHealthSensor());
    }

    private final Map<String, ISensorSetting> sensors = new LinkedHashMap<>();

    public ISensorSetting getSensorFromPath(String buttonPath) {
        return sensors.get(buttonPath);
    }

    public List<ISensorSetting> getSensorsFromPath(String buttonPath) {
        List<ISensorSetting> matchingSensors = new ArrayList<ISensorSetting>();
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
     * The last part of the path
     *
     * @param name
     * @return
     */
    public ISensorSetting getSensorForName(String name) {
        String[] sensorNames = getSensorNames();
        for (int i = 0; i < sensorNames.length; i++) {
            if (sensorNames[i].equals(name)) return getSensorByIndex(i);
        }
        return null;
    }

    public List<String> getUpgradeInfo() {
        List<String> text = new ArrayList<String>();
        text.add(TextFormatting.GRAY + "The following combinations of upgrades are used in sensors to work:");

        Set<Set<Item>> upgrades = new HashSet<Set<Item>>();
        for (ISensorSetting sensor : sensors.values()) {
            upgrades.add(sensor.getRequiredUpgrades());
        }

        for (Set<Item> requiredStacks : upgrades) {
            StringBuilder upgradeTitle = new StringBuilder();
            for (Item stack : requiredStacks) {
                upgradeTitle.append(I18n.format(stack.getUnlocalizedName() + ".name")).append(" + ");
            }
            upgradeTitle = new StringBuilder(TextFormatting.BLACK + "-" + upgradeTitle.substring(0, upgradeTitle.length() - 3)
                    .replace("Machine Upgrade: ", ""));
            text.add(upgradeTitle.toString());
        }
        return text;
    }

    public Set<Item> getUniversalSensorUpgrades() {
        Set<Item> items = new HashSet<>();
        for (ISensorSetting sensor : sensors.values()) {
            items.addAll(sensor.getRequiredUpgrades());
        }
        return items;
    }

    public String[] getDirectoriesAtLocation(String path) {
        List<String> directories = new ArrayList<>();
        for (String sensorPath : sensors.keySet()) {
            if (sensorPath.startsWith(path) && !sensorPath.equals(path)) {

                //if path equals "entityTracker/player/" and sensor path equals "entityTracker/player/speed", to directories will "speed" be added.
                String[] folders = sensorPath.substring(path.length()).split("/");
                if (folders[0].equals("") && folders.length > 1) {
                    if (!directories.contains(folders[1])) directories.add(folders[1]);
                } else {
                    if (!directories.contains(folders[0])) directories.add(folders[0]);
                }

            }
        }
        String[] directoryArray = directories.toArray(new String[directories.size()]);
        Arrays.sort(directoryArray);
        return directoryArray;
    }

    private String getUpgradePrefix(ISensorSetting sensor) {
        List<Item> upgrades = new ArrayList<>(sensor.getRequiredUpgrades());

        upgrades.sort(Comparator.comparing(Item::getUnlocalizedName));

        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < upgrades.size(); i++) {
            ret.append(upgrades.get(i).getUnlocalizedName()).append(i < upgrades.size() - 1 ? "_" : "/");
        }

        return ret.toString();
    }

    public Set<Item> getRequiredStacksFromText(String text) {
        List<ISensorSetting> sensors = getSensorsFromPath(text);
        return sensors.isEmpty() ? new HashSet<>() : sensors.get(0).getRequiredUpgrades();
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

    private class BlockAndCoordinateEventSensor implements IEventSensorSetting {
        private final IBlockAndCoordinateEventSensor coordinateSensor;

        public BlockAndCoordinateEventSensor(IBlockAndCoordinateEventSensor sensor) {
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
            return positions == null ? 0 : coordinateSensor.emitRedstoneOnEvent(event, teUs, sensorRange, positions);
        }

        @Override
        public int getRedstonePulseLength() {
            return coordinateSensor.getRedstonePulseLength();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void drawAdditionalInfo(FontRenderer fontRenderer) {
            coordinateSensor.drawAdditionalInfo(fontRenderer);
        }

        @Override
        public Rectangle needsSlot() {
            return coordinateSensor.needsSlot();
        }

        @Override
        public Set<Item> getRequiredUpgrades() {
            Set<Item> upgrades = new HashSet<Item>(coordinateSensor.getRequiredUpgrades());
            upgrades.add(Itemss.GPS_TOOL);
            return upgrades;
        }
    }

    private class BlockAndCoordinatePollSensor implements IPollSensorSetting {
        private final IBlockAndCoordinatePollSensor coordinateSensor;

        public BlockAndCoordinatePollSensor(IBlockAndCoordinatePollSensor sensor) {
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
            int mult = positions == null ? 1 : positions.size();
            return coordinateSensor.getPollFrequency() * mult;
        }

        @Override
        public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityUniversalSensor) {
                TileEntityUniversalSensor teUs = (TileEntityUniversalSensor) te;
                Set<BlockPos> positions = teUs.getGPSPositions();
                return positions == null ? 0 : coordinateSensor.getRedstoneValue(world, pos, sensorRange, textBoxText, positions);
            }
            return 0;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void drawAdditionalInfo(FontRenderer fontRenderer) {
            coordinateSensor.drawAdditionalInfo(fontRenderer);
        }

        @Override
        public Rectangle needsSlot() {
            return coordinateSensor.needsSlot();
        }

        @Override
        public Set<Item> getRequiredUpgrades() {
            Set<Item> upgrades = new HashSet<Item>(coordinateSensor.getRequiredUpgrades());
            upgrades.add(Itemss.GPS_TOOL);
            return upgrades;
        }
    }

}
