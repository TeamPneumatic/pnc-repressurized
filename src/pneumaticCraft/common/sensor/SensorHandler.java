package pneumaticCraft.common.sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.universalSensor.IBlockAndCoordinateEventSensor;
import pneumaticCraft.api.universalSensor.IBlockAndCoordinatePollSensor;
import pneumaticCraft.api.universalSensor.IEventSensorSetting;
import pneumaticCraft.api.universalSensor.IPollSensorSetting;
import pneumaticCraft.api.universalSensor.ISensorSetting;
import pneumaticCraft.api.universalSensor.SensorRegistrator;
import pneumaticCraft.api.universalSensor.SensorRegistrator.ISensorRegistrator;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.sensor.eventSensors.BlockInteractSensor;
import pneumaticCraft.common.sensor.eventSensors.PlayerAttackSensor;
import pneumaticCraft.common.sensor.eventSensors.PlayerItemPickupSensor;
import pneumaticCraft.common.sensor.pollSensors.BlockComparatorSensor;
import pneumaticCraft.common.sensor.pollSensors.BlockHeatSensor;
import pneumaticCraft.common.sensor.pollSensors.BlockLightLevelSensor;
import pneumaticCraft.common.sensor.pollSensors.BlockMetadataSensor;
import pneumaticCraft.common.sensor.pollSensors.BlockPresenceSensor;
import pneumaticCraft.common.sensor.pollSensors.BlockRedstoneSensor;
import pneumaticCraft.common.sensor.pollSensors.PlayerHealthSensor;
import pneumaticCraft.common.sensor.pollSensors.TwitchStreamerSensor;
import pneumaticCraft.common.sensor.pollSensors.UserSetSensor;
import pneumaticCraft.common.sensor.pollSensors.WorldDayLightSensor;
import pneumaticCraft.common.sensor.pollSensors.WorldGlobalVariableSensor;
import pneumaticCraft.common.sensor.pollSensors.WorldPlayersInServerSensor;
import pneumaticCraft.common.sensor.pollSensors.WorldRainingSensor;
import pneumaticCraft.common.sensor.pollSensors.WorldTicktimeSensor;
import pneumaticCraft.common.sensor.pollSensors.WorldTimeSensor;
import pneumaticCraft.common.sensor.pollSensors.WorldWeatherForecaster;
import pneumaticCraft.common.sensor.pollSensors.entity.EntityInRangeSensor;
import pneumaticCraft.common.tileentity.TileEntityUniversalSensor;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SensorHandler implements ISensorRegistrator{

    public static SensorHandler instance(){
        return (SensorHandler)SensorRegistrator.sensorRegistrator;
    }

    public static void init(){
        SensorRegistrator.sensorRegistrator = new SensorHandler();
        SensorRegistrator.sensorRegistrator.registerSensor(new EntityInRangeSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new PlayerAttackSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new PlayerItemPickupSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new BlockInteractSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new WorldDayLightSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new WorldRainingSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new WorldTimeSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new WorldWeatherForecaster());
        SensorRegistrator.sensorRegistrator.registerSensor(new WorldPlayersInServerSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new WorldTicktimeSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new WorldGlobalVariableSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new BlockPresenceSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new BlockMetadataSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new BlockComparatorSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new BlockRedstoneSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new BlockLightLevelSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new BlockHeatSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new UserSetSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new TwitchStreamerSensor());
        SensorRegistrator.sensorRegistrator.registerSensor(new PlayerHealthSensor());
    }

    private final List<ISensorSetting> sensors = new ArrayList<ISensorSetting>();
    private final List<String> sensorPaths = new ArrayList<String>();

    public ISensorSetting getSensorFromPath(String buttonPath){
        for(int i = 0; i < sensorPaths.size(); i++) {
            if(sensorPaths.get(i).equals(buttonPath)) return sensors.get(i);
        }
        return null;
    }

    public ISensorSetting getSensorByIndex(int index){
        return sensors.get(index);
    }

    public String[] getSensorNames(){
        String[] sensorNames = new String[sensorPaths.size()];
        for(int i = 0; i < sensorNames.length; i++) {
            sensorNames[i] = sensorPaths.get(i).substring(sensorPaths.get(i).lastIndexOf('/') + 1);
        }
        return sensorNames;
    }

    /**
     * The last part of the path
     * @param name
     * @return
     */
    public ISensorSetting getSensorForName(String name){
        String[] sensorNames = getSensorNames();
        for(int i = 0; i < sensorNames.length; i++) {
            if(sensorNames[i].equals(name)) return sensors.get(i);
        }
        return null;
    }

    public List<String> getUpgradeInfo(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.GRAY + "The following combinations of upgrades are used in sensors to work:");
        for(String sensorPath : sensorPaths) {
            ItemStack[] requiredStacks = getRequiredStacksFromText(sensorPath.split("/")[0]);
            String upgradeTitle = "";
            for(ItemStack stack : requiredStacks) {
                upgradeTitle = upgradeTitle + stack.getDisplayName() + " + ";
            }
            upgradeTitle = EnumChatFormatting.BLACK + "-" + upgradeTitle.substring(0, upgradeTitle.length() - 3).replace("Machine Upgrade: ", "");
            if(!text.contains(upgradeTitle)) text.add(upgradeTitle);
        }
        return text;
    }

    public void addMachineUpgradeInfo(List tooltip, int upgradeMeta){
        for(String sensorPath : sensorPaths) {
            ItemStack[] requiredStacks = getRequiredStacksFromText(sensorPath);
            for(ItemStack stack : requiredStacks) {
                if(stack.getItem() == Itemss.machineUpgrade && stack.getItemDamage() == upgradeMeta) {
                    tooltip.add(Blockss.universalSensor.getUnlocalizedName());
                    return;
                }
            }
        }
    }

    private String sortRequiredUpgrades(String path){
        String[] requiredUpgrades = path.split("/")[0].split("_");
        PneumaticCraftUtils.sortStringArrayAlphabetically(requiredUpgrades);
        String newPath = "";
        for(String upgrade : requiredUpgrades) {
            newPath = newPath + upgrade + "_";
        }
        return newPath.substring(0, newPath.length() - 1) + path.replace(path.split("/")[0], "");//cut off the last '_'
    }

    public String[] getDirectoriesAtLocation(String path){
        List<String> directories = new ArrayList<String>();
        for(String sensorPath : sensorPaths) {
            if(sensorPath.startsWith(path) && !sensorPath.equals(path)) {

                //if path equals "entityTracker/player/" and sensor path equals "entityTracker/player/speed", to directories will "speed" be added.
                String[] folders = sensorPath.substring(path.length()).split("/");
                if(folders[0].equals("") && folders.length > 1) {
                    if(!directories.contains(folders[1])) directories.add(folders[1]);
                } else {
                    if(!directories.contains(folders[0])) directories.add(folders[0]);
                }

            }
        }
        String[] directoryArray = directories.toArray(new String[directories.size()]);
        PneumaticCraftUtils.sortStringArrayAlphabetically(directoryArray);
        return directoryArray;
    }

    public ItemStack[] getRequiredStacksFromText(String buttonText){
        String[] stacks = buttonText.split("/")[0].split("_");
        List<ItemStack> itemStacks = new ArrayList<ItemStack>();
        for(String stack : stacks) {
            if(stack.equals("entityTracker")) {
                itemStacks.add(new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_ENTITY_TRACKER));
            } else if(stack.equals("blockTracker")) {
                itemStacks.add(new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_BLOCK_TRACKER));
            } else if(stack.equals("volume")) {
                itemStacks.add(new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_VOLUME_DAMAGE));
            } else if(stack.equals("dispenser")) {
                itemStacks.add(new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE));
            } else if(stack.equals("speed")) {
                itemStacks.add(new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE));
            } else if(stack.equals("itemLife")) {
                itemStacks.add(new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_ITEM_LIFE));
            } else if(stack.equals("itemSearch")) {
                itemStacks.add(new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_SEARCH_DAMAGE));
            } else if(stack.equals("coordinateTracker")) {
                itemStacks.add(new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_COORDINATE_TRACKER_DAMAGE));
            } else if(stack.equals("range")) {
                itemStacks.add(new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_RANGE));
            } else if(stack.equals("security")) {
                itemStacks.add(new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_SECURITY));
            } else if(stack.equals("gpsTool")) {
                itemStacks.add(new ItemStack(Itemss.GPSTool));
            }
        }
        return itemStacks.toArray(new ItemStack[itemStacks.size()]);
    }

    @Override
    public void registerSensor(ISensorSetting sensor){
        sensors.add(sensor);
        sensorPaths.add(sortRequiredUpgrades(sensor.getSensorPath()));
    }

    @Override
    public void registerSensor(IBlockAndCoordinateEventSensor sensor){
        registerSensor(new BlockAndCoordinateEventSensor(sensor));
    }

    @Override
    public void registerSensor(IBlockAndCoordinatePollSensor sensor){
        registerSensor(new BlockAndCoordinatePollSensor(sensor));
    }

    private class BlockAndCoordinateEventSensor implements IEventSensorSetting{
        private final IBlockAndCoordinateEventSensor coordinateSensor;

        public BlockAndCoordinateEventSensor(IBlockAndCoordinateEventSensor sensor){
            coordinateSensor = sensor;
        }

        @Override
        public String getSensorPath(){
            return coordinateSensor.getSensorPath();
        }

        @Override
        public boolean needsTextBox(){
            return coordinateSensor.needsTextBox();
        }

        @Override
        public List<String> getDescription(){
            return coordinateSensor.getDescription();
        }

        @Override
        public int emitRedstoneOnEvent(Event event, TileEntity tile, int sensorRange, String textboxText){
            TileEntityUniversalSensor teUs = (TileEntityUniversalSensor)tile;
            Set<ChunkPosition> positions = teUs.getGPSPositions();
            return positions == null ? 0 : coordinateSensor.emitRedstoneOnEvent(event, teUs, sensorRange, positions);
        }

        @Override
        public int getRedstonePulseLength(){
            return coordinateSensor.getRedstonePulseLength();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void drawAdditionalInfo(FontRenderer fontRenderer){
            coordinateSensor.drawAdditionalInfo(fontRenderer);
        }

        @Override
        public Rectangle needsSlot(){
            return coordinateSensor.needsSlot();
        }
    }

    private class BlockAndCoordinatePollSensor implements IPollSensorSetting{
        private final IBlockAndCoordinatePollSensor coordinateSensor;

        public BlockAndCoordinatePollSensor(IBlockAndCoordinatePollSensor sensor){
            coordinateSensor = sensor;
        }

        @Override
        public String getSensorPath(){
            return coordinateSensor.getSensorPath();
        }

        @Override
        public boolean needsTextBox(){
            return coordinateSensor.needsTextBox();
        }

        @Override
        public List<String> getDescription(){
            return coordinateSensor.getDescription();
        }

        @Override
        public int getPollFrequency(TileEntity te){
            TileEntityUniversalSensor us = (TileEntityUniversalSensor)te;
            Set<ChunkPosition> positions = us.getGPSPositions();
            int mult = positions == null ? 1 : positions.size();
            return coordinateSensor.getPollFrequency() * mult;
        }

        @Override
        public int getRedstoneValue(World world, int x, int y, int z, int sensorRange, String textBoxText){
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntityUniversalSensor) {
                TileEntityUniversalSensor teUs = (TileEntityUniversalSensor)te;
                Set<ChunkPosition> positions = teUs.getGPSPositions();
                return positions == null ? 0 : coordinateSensor.getRedstoneValue(world, x, y, z, sensorRange, textBoxText, positions);
            }
            return 0;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void drawAdditionalInfo(FontRenderer fontRenderer){
            coordinateSensor.drawAdditionalInfo(fontRenderer);
        }

        @Override
        public Rectangle needsSlot(){
            return coordinateSensor.needsSlot();
        }
    }

}
