package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.universal_sensor.IEventSensorSetting;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.api.universal_sensor.ISensorSetting;
import me.desht.pneumaticcraft.client.gui.GuiUniversalSensor;
import me.desht.pneumaticcraft.client.render.RenderRangeLines;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerUniversalSensor;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketRenderRangeLines;
import me.desht.pneumaticcraft.common.pressure.AirHandler;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.util.GlobalTileEntityCacheManager;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TileEntityUniversalSensor extends TileEntityPneumaticBase
        implements IRangeLineShower, IGUITextFieldSensitive, IMinWorkingPressure, IRedstoneControl, INamedContainerProvider {

    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.universalSensor.button.normal",
            "gui.tab.redstoneBehaviour.universalSensor.button.inverted"
    );

    private static final int CUSTOM_UPGRADES_SIZE = 4;

    @DescSynced
    public final boolean[] sidesConnected = new boolean[6];

    @GuiSynced
    private String sensorSetting = "";
    private int tickTimer;
    public int redstoneStrength;
    private int eventTimer;
    public float dishRotation;
    public float oldDishRotation;
    private float dishSpeed;
    @GuiSynced
    private boolean invertedRedstone;
    @DescSynced
    public boolean isSensorActive;
    @GuiSynced
    private String sensorGuiText = ""; //optional parameter text for sensors.
    private boolean requestPollPullEvent;
    private final Set<BlockPos> positions = new HashSet<>();

    private int oldSensorRange; // range used by the range line renderer, to figure out if the range has been changed.
    private final RenderRangeLines rangeLineRenderer = new RenderRangeLines(0x330000FF);

    // todo 1.14 computercraft
    // keep track of the computers so we can raise a os.pullevent.
//    private final CopyOnWriteArrayList<IComputerAccess> attachedComputers = new CopyOnWriteArrayList<>();

    @DescSynced
    public String lastSensorError = "";
    private final UniversalSensorUpgradeHandler customUpgradeHandler = new UniversalSensorUpgradeHandler();

    public TileEntityUniversalSensor() {
        super(ModTileEntityTypes.UNIVERSAL_SENSOR, PneumaticValues.DANGER_PRESSURE_UNIVERSAL_SENSOR, PneumaticValues.MAX_PRESSURE_UNIVERSAL_SENSOR, PneumaticValues.VOLUME_UNIVERSAL_SENSOR, 0);
        for (Item upgrade : SensorHandler.getInstance().getUniversalSensorUpgrades()) {
            addApplicableUpgrade(upgrade);
        }
        addApplicableUpgrade(IItemRegistry.EnumUpgrade.RANGE);
    }

    @Override
    public void tick() {
        oldDishRotation = dishRotation;
        if (isSensorActive) {
            dishSpeed = Math.min(dishSpeed + 0.2F, 10);
        } else {
            dishSpeed = Math.max(dishSpeed - 0.2F, 0);
        }
        dishRotation += dishSpeed;

        if (getWorld().isRemote) {
            int sensorRange = getRange();
            if (oldSensorRange != sensorRange || oldSensorRange == 0) {
                oldSensorRange = sensorRange;
                if (!firstRun) rangeLineRenderer.resetRendering(sensorRange);
            }
            rangeLineRenderer.update();
        }
        super.tick();

        if (!getWorld().isRemote) {
            tickTimer++;
            ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(sensorSetting);
            if (sensor != null && getPressure() > PneumaticValues.MIN_PRESSURE_UNIVERSAL_SENSOR) {
                isSensorActive = true;
                addAir(-sensor.getAirUsage(getWorld(), getPos()));
                if (sensor instanceof IPollSensorSetting) {
                    if (tickTimer >= ((IPollSensorSetting) sensor).getPollFrequency(this)) {
                        try {
                            int newRedstoneStrength = ((IPollSensorSetting) sensor).getRedstoneValue(getWorld(), getPos(), getRange(), sensorGuiText);
                            if (invertedRedstone) newRedstoneStrength = 15 - newRedstoneStrength;
                            if (newRedstoneStrength != redstoneStrength) {
                                redstoneStrength = newRedstoneStrength;
                                // todo 1.14 computercraft
//                                if (requestPollPullEvent) {
//                                    notifyComputers(redstoneStrength);
//                                }
                                updateNeighbours();
                            }
                            tickTimer = 0;
                        } catch (Exception e) {
                            lastSensorError = e.getMessage();
                        }
                    }
                    eventTimer = 0;
                } else {
                    if (eventTimer > 0) {
                        eventTimer--;
                        if (eventTimer == 0 && redstoneStrength != (invertedRedstone ? 15 : 0)) {
                            redstoneStrength = invertedRedstone ? 15 : 0;
                            updateNeighbours();
                        }
                    }
                }
            } else {
                isSensorActive = false;
                if (redstoneStrength != (invertedRedstone ? 15 : 0)) {
                    redstoneStrength = invertedRedstone ? 15 : 0;
                    updateNeighbours();
                }
            }
        }
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        updateConnections();
    }

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        updateConnections();
    }

    private void updateConnections() {
        BlockState newState = AirHandler.getBlockConnectionState(getBlockState(), getAirHandler(null));
        world.setBlockState(pos, newState);
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public boolean canConnectTo(Direction side) {
        return side != Direction.UP;
    }

    /**
     * Will initiate the wireframe rendering. When invoked on the server, it sends a packet to every client to render the box.
     */
    @Override
    public void showRangeLines() {
        if (getWorld().isRemote) {
            rangeLineRenderer.resetRendering(getRange());
        } else {
            NetworkHandler.sendToAllAround(new PacketRenderRangeLines(this), getWorld(), TileEntityConstants.PACKET_UPDATE_DISTANCE + getRange());
        }
    }

    public void renderRangeLines() {
        rangeLineRenderer.render();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (rangeLineRenderer == null || !rangeLineRenderer.isCurrentlyRendering()) return super.getRenderBoundingBox();
        int range = getRange();
        return new AxisAlignedBB(getPos().getX() - range, getPos().getY() - range, getPos().getZ() - range, getPos().getX() + 1 + range, getPos().getY() + 1 + range, getPos().getZ() + 1 + range);
    }

    public void onEvent(Event event) {
        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(sensorSetting);
        if (sensor instanceof IEventSensorSetting && getPressure() > PneumaticValues.MIN_PRESSURE_UNIVERSAL_SENSOR) {
            int newRedstoneStrength = ((IEventSensorSetting) sensor).emitRedstoneOnEvent(event, this, getRange(), sensorGuiText);
            if (newRedstoneStrength != 0) eventTimer = ((IEventSensorSetting) sensor).getRedstonePulseLength();
            if (invertedRedstone) newRedstoneStrength = 15 - newRedstoneStrength;
            // todo 1.14 computercraft
//            if (eventTimer > 0 && ThirdPartyManager.computerCraftLoaded) {
//                if (event instanceof PlayerInteractEvent) {
//                    PlayerInteractEvent e = (PlayerInteractEvent) event;
//                    notifyComputers(newRedstoneStrength, e.getPos().getX(), e.getPos().getY(), e.getPos().getZ());
//                } else {
//                    notifyComputers(newRedstoneStrength);
//                }
//            }
            if (newRedstoneStrength != redstoneStrength) {
                redstoneStrength = newRedstoneStrength;
                updateNeighbours();
            }
        }
    }

    public int getRange() {
        return getUpgrades(IItemRegistry.EnumUpgrade.RANGE) + 2;
    }

    private void setSensorSetting(String sensorPath) {
        sensorSetting = sensorPath;
        if (getWorld() != null && getWorld().isRemote) {
            Screen guiScreen = Minecraft.getInstance().currentScreen;
            if (guiScreen instanceof GuiUniversalSensor) {
                ((GuiUniversalSensor) guiScreen).updateButtons();
            }
        }
    }

    private boolean setSensorSetting(ISensorSetting sensor) {
        if (areGivenUpgradesInserted(sensor.getRequiredUpgrades())) {
            setSensorSetting(sensor.getSensorPath());
            return true;
        } else {
            return false;
        }
    }

    public String getSensorSetting() {
        return sensorSetting;
    }

    @Override
    public void onGuiUpdate() {
        setSensorSetting(sensorSetting);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putString("sensorSetting", sensorSetting);
        tag.putBoolean("invertedRedstone", invertedRedstone);
        tag.putFloat("dishSpeed", dishSpeed);
        tag.putString("sensorText", sensorGuiText);
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        setSensorSetting(tag.getString("sensorSetting"));
        invertedRedstone = tag.getBoolean("invertedRedstone");
        dishSpeed = tag.getFloat("dishSpeed");
        sensorGuiText = tag.getString("sensorText");
        setupGPSPositions();
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        if (tag.equals("back")) {
            // the 'back' button
            String[] folders = getSensorSetting().split("/");
            String newPath = getSensorSetting().replace(folders[folders.length - 1], "");
            if (newPath.endsWith("/")) {
                newPath = newPath.substring(0, newPath.length() - 1);
            }
            setSensorSetting(newPath);
            setText(0, "");
        } else if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            invertedRedstone = !invertedRedstone;
            redstoneStrength = 15 - redstoneStrength;
            updateNeighbours();
        } else if (tag.startsWith("tag:")) {
            try {
                int t = Integer.parseInt(tag.split(":")[1]);
                String[] directories = SensorHandler.getInstance().getDirectoriesAtLocation(getSensorSetting());
                if (t / 10 <= directories.length) { // <= because of the redstone button being 0.
                    if (getSensorSetting().equals("")) {
                        setSensorSetting(directories[t / 10 - 1]);
                    } else {
                        setSensorSetting(getSensorSetting() + "/" + directories[t / 10 - 1]);
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public boolean areGivenUpgradesInserted(Set<Item> requiredItems) {
        for (Item requiredItem : requiredItems) {
            if (getUpgrades(requiredItem) == 0) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    public Set<BlockPos> getGPSPositions() {
        return positions;
    }

    private void setupGPSPositions() {
        positions.clear();

        List<BlockPos> gpsPositions = new ArrayList<>();
        int sensorRange = getRange();
        for (int i = 0; i < getUpgradeHandler().getSlots(); i++) {
            ItemStack gps = getUpgradeHandler().getStackInSlot(i);
            if (gps.getItem() == ModItems.GPS_TOOL) {
                BlockPos pos = ItemGPSTool.getGPSLocation(gps);
                if (pos != null
                        && Math.abs(pos.getX() - getPos().getX()) <= sensorRange
                        && Math.abs(pos.getY() - getPos().getY()) <= sensorRange
                        && Math.abs(pos.getZ() - getPos().getZ()) <= sensorRange) {
                    gpsPositions.add(pos);
                }
            }
        }

        if (gpsPositions.size() == 1) {
            positions.add(gpsPositions.get(0));
        } else if (gpsPositions.size() > 1) {
            int minX = Math.min(gpsPositions.get(0).getX(), gpsPositions.get(1).getX());
            int minY = Math.min(gpsPositions.get(0).getY(), gpsPositions.get(1).getY());
            int minZ = Math.min(gpsPositions.get(0).getZ(), gpsPositions.get(1).getZ());
            int maxX = Math.max(gpsPositions.get(0).getX(), gpsPositions.get(1).getX());
            int maxY = Math.max(gpsPositions.get(0).getY(), gpsPositions.get(1).getY());
            int maxZ = Math.max(gpsPositions.get(0).getZ(), gpsPositions.get(1).getZ());
            for (int x = minX; x <= maxX; x++) {
                for (int y = Math.min(255, maxY); y >= minY && y >= 0; y--) {
                    for (int z = minZ; z <= maxZ; z++) {
                        positions.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
    }

    @Override
    public void setText(int textFieldID, String text) {
        sensorGuiText = text;
        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(sensorSetting);
        if (sensor != null) {
            try {
                lastSensorError = "";
                sensor.notifyTextChange(sensorGuiText);
            } catch (Exception e) {
                lastSensorError = e.getMessage();
            }
        }
        if (!getWorld().isRemote) scheduleDescriptionPacket();
    }

    @Override
    public String getText(int textFieldID) {
        return sensorGuiText;
    }

    /*
     * COMPUTERCRAFT API
     */
//    @Override
//    public String getType() {
//        return "universalSensor";
//    }

    @Override
    public void addLuaMethods(LuaMethodRegistry registry) {
        super.addLuaMethods(registry);

        registry.registerLuaMethod(new LuaMethod("getSensorNames") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return SensorHandler.getInstance().getSensorNames();
            }
        });

        registry.registerLuaMethod(new LuaMethod("setSensor") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 0, 1, "sensor_name?");
                if (args.length == 1) {
                    ISensorSetting sensor;
                    if (args[0] instanceof String) {
                        sensor = SensorHandler.getInstance().getSensorForName((String) args[0]);
                    } else {
                        sensor = SensorHandler.getInstance().getSensorByIndex(((Double) args[0]).intValue() - 1);
                    }
                    if (sensor != null) return new Object[]{setSensorSetting(sensor)};
                    throw new IllegalArgumentException("Invalid sensor name/index: " + args[0]);
                } else {
                    setSensorSetting("");
                    return new Object[]{true};
                }
            }
        });

        registry.registerLuaMethod(new LuaMethod("getSensor") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                ISensorSetting curSensor = SensorHandler.getInstance().getSensorFromPath(getSensorSetting());
                return curSensor == null ? null : new Object[]{getSensorSetting().substring(getSensorSetting().lastIndexOf('/') + 1)};
            }
        });

        registry.registerLuaMethod(new LuaMethod("setTextfield") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "textfield_value");
                setText(0, (String) args[0]);
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("getTextfield") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{getText(0)};
            }
        });

        registry.registerLuaMethod(new LuaMethod("isSensorEventBased") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[]{SensorHandler.getInstance().getSensorFromPath(getSensorSetting()) instanceof IEventSensorSetting};
            }
        });

        registry.registerLuaMethod(new LuaMethod("getSensorValue") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                ISensorSetting s = SensorHandler.getInstance().getSensorFromPath(getSensorSetting());
                if (s instanceof IPollSensorSetting) {
                    requestPollPullEvent = true;
                    return new Object[]{redstoneStrength};
                } else if (s != null) {
                    throw new IllegalArgumentException("The selected sensor is pull event based. You can't poll the value.");
                } else {
                    throw new IllegalArgumentException("There's no sensor selected!");
                }
            }
        });

        registry.registerLuaMethod(new LuaMethod("setGPSToolCoordinate") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 4, "slot, x, y, z");
                ItemStack stack = getUpgradeHandler().getStackInSlot(((Double) args[0]).intValue() - 1); //minus one, as lua is 1-oriented.
                if (stack.getItem() == ModItems.GPS_TOOL) {
                    ItemGPSTool.setGPSLocation(stack, new BlockPos((Double) args[1], (Double) args[2], (Double) args[3]));
                    return new Object[]{true};
                } else {
                    return new Object[]{false};
                }
            }

        });

        registry.registerLuaMethod(new LuaMethod("getGPSToolCoordinate") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "upgrade_slot");
                ItemStack stack = getUpgradeHandler().getStackInSlot(((Double) args[0]).intValue() - 1); //minus one, as lua is 1-oriented.
                if (stack.getItem() == ModItems.GPS_TOOL) {
                    BlockPos pos = ItemGPSTool.getGPSLocation(stack);
                    if (pos != null) {
                        return new Object[]{pos.getX(), pos.getY(), pos.getZ()};
                    } else {
                        return new Object[]{0, 0, 0};
                    }
                } else {
                    return null;
                }
            }
        });
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    // todo 1.14 computercraft
//    @Override
//    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
//    public void attach(IComputerAccess computer){
//        attachedComputers.add(computer);
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
//    public void detach(IComputerAccess computer){
//        attachedComputers.remove(computer);
//    }
//
//    /**
//     * Called on a event sensor
//     *
//     * @param arguments
//     */
//    @Optional.Method(modid = ModIds.COMPUTERCRAFT)
//    private void notifyComputers(Object... arguments) {
//        for (IComputerAccess computer : attachedComputers) {
//            computer.queueEvent(getType(), arguments);
//        }
//    }

    @Override
    public int getRedstoneMode() {
        return invertedRedstone ? 1 : 0;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_UNIVERSAL_SENSOR;
    }

    @Override
    public String getRedstoneTabTitle() {
        return "gui.tab.redstoneBehaviour.universalSensor.redstoneEmission";
    }

    @Override
    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
    }
    
    @Override
    public void remove(){
        super.remove();
        GlobalTileEntityCacheManager.getInstance().universalSensors.remove(this);
    }
    
    @Override
    public void validate(){
        super.validate();
        GlobalTileEntityCacheManager.getInstance().universalSensors.add(this);
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerUniversalSensor(i, playerInventory, getPos());
    }

    @Override
    public UpgradeHandler getUpgradeHandler() {
        return customUpgradeHandler;
    }

    private class UniversalSensorUpgradeHandler extends UpgradeHandler {
        UniversalSensorUpgradeHandler() {
            super(CUSTOM_UPGRADES_SIZE);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return itemStack.isEmpty() || getApplicableUpgrades().contains(itemStack.getItem()) || itemStack.getItem() == ModItems.GPS_TOOL;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            if (!getWorld().isRemote && !getSensorSetting().isEmpty()
                    && !areGivenUpgradesInserted(SensorHandler.getInstance().getRequiredStacksFromText(getSensorSetting()))) {
                setSensorSetting("");
            }

            setupGPSPositions();
        }
    }

}
