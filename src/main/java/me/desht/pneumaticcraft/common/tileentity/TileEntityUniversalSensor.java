package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.api.universal_sensor.IEventSensorSetting;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.api.universal_sensor.ISensorSetting;
import me.desht.pneumaticcraft.client.gui.GuiUniversalSensor;
import me.desht.pneumaticcraft.client.util.RangeLines;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerUniversalSensor;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketRenderRangeLines;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.ComputerEventManager;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.util.GlobalTileEntityCacheManager;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TileEntityUniversalSensor extends TileEntityPneumaticBase
        implements IRangeLineShower, IGUITextFieldSensitive, IMinWorkingPressure, IRedstoneControl, INamedContainerProvider {

    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "pneumaticcraft.gui.tab.redstoneBehaviour.universalSensor.button.normal",
            "pneumaticcraft.gui.tab.redstoneBehaviour.universalSensor.button.inverted"
    );

    @GuiSynced
    private String sensorSetting = "";
    private int tickTimer;
    public int redstoneStrength;
    private int redstonePulseCounter;
    public float dishRotation;
    public float oldDishRotation;
    private float dishSpeed;
    @GuiSynced
    private boolean invertedRedstone;
    @DescSynced
    public boolean isSensorActive;
    @GuiSynced
    public String lastSensorExceptionText = "";
    @GuiSynced
    private String sensorGuiText = ""; //optional parameter text for sensors.
    @GuiSynced
    public SensorStatus sensorStatus = SensorStatus.OK;
    private boolean requestPollPullEvent;  // computer support
    private final Set<BlockPos> positions = new HashSet<>();

    private int oldSensorRange; // range used by the range line renderer, to figure out if the range has been changed.
    public final RangeLines rangeLines = new RangeLines(0x600060FF);

    private final ItemStackHandler itemHandler = new UniversalSensorItemHandler();
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> itemHandler);
    @GuiSynced
    public int outOfRange;

    public TileEntityUniversalSensor() {
        super(ModTileEntities.UNIVERSAL_SENSOR.get(), PneumaticValues.DANGER_PRESSURE_UNIVERSAL_SENSOR, PneumaticValues.MAX_PRESSURE_UNIVERSAL_SENSOR, PneumaticValues.VOLUME_UNIVERSAL_SENSOR, 4);
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
                if (!firstRun) rangeLines.startRendering(sensorRange);
            }
            rangeLines.tick(world.rand);
        }
        super.tick();

        if (!getWorld().isRemote) {
            tickTimer++;
            ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(sensorSetting);
            if (updateStatus(sensor) == SensorStatus.OK  && sensor != null && getPressure() > PneumaticValues.MIN_PRESSURE_UNIVERSAL_SENSOR) {
                isSensorActive = true;
                addAir(-sensor.getAirUsage(getWorld(), getPos()));
                if (sensor instanceof IPollSensorSetting) {
                    if (tickTimer >= ((IPollSensorSetting) sensor).getPollFrequency(this)) {
                        try {
                            int newRedstoneStrength = ((IPollSensorSetting) sensor).getRedstoneValue(getWorld(), getPos(), getRange(), sensorGuiText);
                            if (invertedRedstone) newRedstoneStrength = 15 - newRedstoneStrength;
                            if (newRedstoneStrength != redstoneStrength) {
                                redstoneStrength = newRedstoneStrength;
                                if (requestPollPullEvent) {
                                    notifyComputers(redstoneStrength);
                                }
                                updateNeighbours();
                            }
                            tickTimer = 0;
                        } catch (Exception e) {
                            lastSensorExceptionText = e.getMessage() == null ? "" : e.getMessage();
                        }
                    }
                    redstonePulseCounter = 0;
                } else {
                    if (redstonePulseCounter > 0) {
                        redstonePulseCounter--;
                        if (redstonePulseCounter == 0 && redstoneStrength != (invertedRedstone ? 15 : 0)) {
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

    private void notifyComputers(Object... params) {
        ComputerEventManager.getInstance().sendEvents(this, "universalSensor", params);
    }

    private SensorStatus updateStatus(ISensorSetting sensor) {
        sensorStatus = SensorStatus.OK;
        if (sensor != null) {
            if (sensor.needsGPSTool() && getPrimaryInventory().getStackInSlot(0).isEmpty()) {
                sensorStatus = SensorStatus.MISSING_GPS;
            } else {
                for (EnumUpgrade upgrade: sensor.getRequiredUpgrades()) {
                    if (getUpgrades(upgrade) == 0) {
                        sensorStatus = SensorStatus.MISSING_UPGRADE;
                        break;
                    }
                }
            }
        } else {
            sensorStatus = SensorStatus.NO_SENSOR;
        }

        return sensorStatus;
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate() {
        return true;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != Direction.UP;
    }

    /**
     * On client, initiate the wireframe rendering. On server, send a packet to every nearby client to render the box.
     */
    @Override
    public void showRangeLines() {
        if (getWorld().isRemote) {
            rangeLines.startRendering(getRange());
        } else {
            NetworkHandler.sendToAllAround(new PacketRenderRangeLines(this), getWorld(), TileEntityConstants.PACKET_UPDATE_DISTANCE + getRange());
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return rangeLines == null || !rangeLines.shouldRender() ?
                super.getRenderBoundingBox() :
                new AxisAlignedBB(getPos()).grow(getRange());
    }

    public void onEvent(Event event) {
        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(sensorSetting);
        if (sensor instanceof IEventSensorSetting && getPressure() >= getMinWorkingPressure()) {
            int newRedstoneStrength = ((IEventSensorSetting) sensor).emitRedstoneOnEvent(event, this, getRange(), sensorGuiText);
            if (newRedstoneStrength != 0) redstonePulseCounter = ((IEventSensorSetting) sensor).getRedstonePulseLength();
            if (invertedRedstone) newRedstoneStrength = 15 - newRedstoneStrength;
            if (redstonePulseCounter > 0 && ThirdPartyManager.instance().isModTypeLoaded(ThirdPartyManager.ModType.COMPUTER)) {
                if (event instanceof PlayerInteractEvent) {
                    PlayerInteractEvent e = (PlayerInteractEvent) event;
                    notifyComputers(newRedstoneStrength, e.getPos().getX(), e.getPos().getY(), e.getPos().getZ());
                } else {
                    notifyComputers(newRedstoneStrength);
                }
            }
            if (newRedstoneStrength != redstoneStrength) {
                redstoneStrength = newRedstoneStrength;
                updateNeighbours();
            }
        }
    }

    public int getRange() {
        return getUpgrades(EnumUpgrade.RANGE) + 8;
    }

    private void setSensorSetting(String sensorPath) {
        sensorSetting = sensorPath;
        if (getWorld() != null && getWorld().isRemote) {
            GuiUniversalSensor.maybeUpdateButtons();
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

        tag.put("Items", itemHandler.serializeNBT());
        tag.putString("sensorSetting", sensorSetting);
        tag.putBoolean("invertedRedstone", invertedRedstone);
        tag.putFloat("dishSpeed", dishSpeed);
        tag.putString("sensorText", sensorGuiText);

        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);

        itemHandler.deserializeNBT(tag.getCompound("Items"));
        setSensorSetting(tag.getString("sensorSetting"));
        invertedRedstone = tag.getBoolean("invertedRedstone");
        dishSpeed = tag.getFloat("dishSpeed");
        sensorGuiText = tag.getString("sensorText");

        setupGPSPositions();
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
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
        } else if (tag.startsWith("set:")) {
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

    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();

        setupGPSPositions();
    }

    public boolean areGivenUpgradesInserted(Set<EnumUpgrade> requiredUpgrades) {
        for (EnumUpgrade upgrade : requiredUpgrades) {
            if (getUpgrades(upgrade) == 0) {
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
        outOfRange = 0;

        ItemStack stack = itemHandler.getStackInSlot(0);
        if (stack.getItem() instanceof IPositionProvider) {
            int sensorRange = getRange();
            List<BlockPos> posList = ((IPositionProvider) stack.getItem()).getStoredPositions(world, stack);
            List<BlockPos> gpsPositions = posList.stream()
                    .filter(pos -> pos != null
                            && Math.abs(pos.getX() - getPos().getX()) <= sensorRange
                            && Math.abs(pos.getY() - getPos().getY()) <= sensorRange
                            && Math.abs(pos.getZ() - getPos().getZ()) <= sensorRange)
                    .collect(Collectors.toList());
            positions.addAll(gpsPositions);
            outOfRange = posList.size() - gpsPositions.size();
            updateStatus(SensorHandler.getInstance().getSensorFromPath(sensorSetting));
        }
        if (getWorld() != null && getWorld().isRemote) GuiUniversalSensor.maybeUpdateButtons();
    }

    @Override
    public void setText(int textFieldID, String text) {
        sensorGuiText = text;
        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(sensorSetting);
        if (sensor != null) {
            try {
                lastSensorExceptionText = "";
                sensor.notifyTextChange(sensorGuiText);
            } catch (Exception e) {
                lastSensorExceptionText = e.getMessage() == null ? "" : e.getMessage();
            }
        }
        if (!getWorld().isRemote) scheduleDescriptionPacket();
    }

    @Override
    public String getText(int textFieldID) {
        return sensorGuiText;
    }

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
                if (stack.getItem() == ModItems.GPS_TOOL.get()) {
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
                if (stack.getItem() == ModItems.GPS_TOOL.get()) {
                    BlockPos pos = ItemGPSTool.getGPSLocation(world, stack);
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
    public IItemHandler getPrimaryInventory() {
        return itemHandler;
    }

    @Nonnull
    @Override
    protected LazyOptional<IItemHandler> getInventoryCap() {
        return inventoryCap;
    }

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
        return "pneumaticcraft.gui.tab.redstoneBehaviour.universalSensor.redstoneEmission";
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

    private class UniversalSensorItemHandler extends ItemStackHandler {
        UniversalSensorItemHandler() {
            super(1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (stack.getItem() instanceof IPositionProvider) {
                List<BlockPos> l = ((IPositionProvider) stack.getItem()).getStoredPositions(world, stack);
                return !l.isEmpty() && l.get(0) != null;
            }
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setupGPSPositions();
        }
    }

    public enum SensorStatus {
        OK,
        MISSING_GPS,
        MISSING_UPGRADE,
        NO_SENSOR;

        public String getTranslationKey() {
            return "pneumaticcraft.gui.universalSensor.status." + toString().toLowerCase();
        }
    }
}
