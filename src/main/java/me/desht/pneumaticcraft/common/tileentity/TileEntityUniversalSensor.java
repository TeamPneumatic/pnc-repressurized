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

package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.api.universal_sensor.IEventSensorSetting;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.api.universal_sensor.ISensorSetting;
import me.desht.pneumaticcraft.client.gui.GuiUniversalSensor;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerUniversalSensor;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.ComputerEventManager;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.EmittingRedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.util.GlobalTileEntityCacheManager;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class TileEntityUniversalSensor extends TileEntityPneumaticBase implements
        IGUITextFieldSensitive, IMinWorkingPressure, IRangedTE,
        IRedstoneControl<TileEntityUniversalSensor>, INamedContainerProvider {

    private static final List<RedstoneMode<TileEntityUniversalSensor>> REDSTONE_MODES = ImmutableList.of(
            new EmittingRedstoneMode<>("universalSensor.normal", new ItemStack(Items.REDSTONE), te -> true),
            new EmittingRedstoneMode<>("universalSensor.inverted", new ItemStack(Items.REDSTONE_TORCH), te -> true)
    );
    private static final byte RS_MODE_NORMAL = 0;
    private static final byte RS_MODE_INVERTED = 1;
    private static final int BASE_RANGE = 8;  // range with no upgrades installed

    @GuiSynced
    private String sensorSetting = "";
    private int tickTimer;
    public int redstoneStrength;
    private int redstonePulseCounter;
    public float dishRotation;
    public float oldDishRotation;
    private float dishSpeed;
    @GuiSynced
    private final RedstoneController<TileEntityUniversalSensor> rsController = new RedstoneController<>(this, REDSTONE_MODES);
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

    private final ItemStackHandler itemHandler = new UniversalSensorItemHandler();
    private final LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> itemHandler);
    @GuiSynced
    public int outOfRange;
    private final RangeManager rangeManager = new RangeManager(this, 0x605050D0);

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

        super.tick();

        if (!getLevel().isClientSide) {
            boolean invertedRedstone = rsController.getCurrentMode() == RS_MODE_INVERTED;
            tickTimer++;
            ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(sensorSetting);
            if (updateStatus(sensor) == SensorStatus.OK  && sensor != null && getPressure() > PneumaticValues.MIN_PRESSURE_UNIVERSAL_SENSOR) {
                isSensorActive = true;
                addAir(-sensor.getAirUsage(getLevel(), getBlockPos()));
                if (sensor instanceof IPollSensorSetting) {
                    if (tickTimer >= ((IPollSensorSetting) sensor).getPollFrequency(this)) {
                        try {
                            int newRedstoneStrength = ((IPollSensorSetting) sensor).getRedstoneValue(getLevel(), getBlockPos(), getRange(), sensorGuiText);
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

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return rangeManager.shouldShowRange() ? rangeManager.getExtents() : super.getRenderBoundingBox();
    }

    public void onEvent(Event event) {
        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(sensorSetting);
        if (sensor instanceof IEventSensorSetting && getPressure() >= getMinWorkingPressure()) {
            int newRedstoneStrength = ((IEventSensorSetting) sensor).emitRedstoneOnEvent(event, this, getRange(), sensorGuiText);
            if (newRedstoneStrength != 0) redstonePulseCounter = ((IEventSensorSetting) sensor).getRedstonePulseLength();
            if (rsController.getCurrentMode() == RS_MODE_INVERTED) newRedstoneStrength = 15 - newRedstoneStrength;
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

    @Override
    public RangeManager getRangeManager() {
        return rangeManager;
    }

    private void setSensorSetting(String sensorPath) {
        sensorSetting = sensorPath;
        if (getLevel() != null && getLevel().isClientSide) {
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
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);

        tag.put("Items", itemHandler.serializeNBT());
        tag.putString("sensorSetting", sensorSetting);
        tag.putFloat("dishSpeed", dishSpeed);
        tag.putString("sensorText", sensorGuiText);

        return tag;
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);

        itemHandler.deserializeNBT(tag.getCompound("Items"));
        setSensorSetting(tag.getString("sensorSetting"));
        if (tag.contains("invertedRedstone")) {
            // TODO remove in 1.17 - legacy compat
            rsController.setCurrentMode(tag.getBoolean("invertedRedstone") ? RS_MODE_INVERTED : RS_MODE_NORMAL);
        }
        dishSpeed = tag.getFloat("dishSpeed");
        sensorGuiText = tag.getString("sensorText");

        setupGPSPositions();
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        if (rsController.parseRedstoneMode(tag)) {
            redstoneStrength = 15 - redstoneStrength;
            updateNeighbours();
            return;
        }

        if (tag.equals("back")) {
            // the 'back' button
            String[] folders = getSensorSetting().split("/");
            String newPath = getSensorSetting().replace(folders[folders.length - 1], "");
            if (newPath.endsWith("/")) {
                newPath = newPath.substring(0, newPath.length() - 1);
            }
            setSensorSetting(newPath);
            setText(0, "");
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

        rangeManager.setRange(getUpgrades(EnumUpgrade.RANGE) + BASE_RANGE);
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
            List<BlockPos> posList = ((IPositionProvider) stack.getItem()).getStoredPositions(level, stack);
            List<BlockPos> gpsPositions = posList.stream()
                    .filter(pos -> pos != null
                            && Math.abs(pos.getX() - getBlockPos().getX()) <= sensorRange
                            && Math.abs(pos.getY() - getBlockPos().getY()) <= sensorRange
                            && Math.abs(pos.getZ() - getBlockPos().getZ()) <= sensorRange)
                    .collect(Collectors.toList());
            positions.addAll(gpsPositions);
            outOfRange = posList.size() - gpsPositions.size();
        }
        if (getLevel() != null && getLevel().isClientSide) GuiUniversalSensor.maybeUpdateButtons();
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
        if (!getLevel().isClientSide) scheduleDescriptionPacket();
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
                    BlockPos pos = ItemGPSTool.getGPSLocation(level, stack);
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
    public RedstoneController<TileEntityUniversalSensor> getRedstoneController() {
        return rsController;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_UNIVERSAL_SENSOR;
    }

    @Override
    public IFormattableTextComponent getRedstoneTabTitle() {
        return xlate("pneumaticcraft.gui.tab.redstoneBehaviour.universalSensor.redstoneEmission");
    }

    @Override
    public void setRemoved(){
        super.setRemoved();
        GlobalTileEntityCacheManager.getInstance().universalSensors.remove(this);
    }

    @Override
    public void clearRemoved(){
        super.clearRemoved();
        GlobalTileEntityCacheManager.getInstance().universalSensors.add(this);
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerUniversalSensor(i, playerInventory, getBlockPos());
    }

    private class UniversalSensorItemHandler extends ItemStackHandler {
        UniversalSensorItemHandler() {
            super(1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (stack.getItem() instanceof IPositionProvider) {
                List<BlockPos> l = ((IPositionProvider) stack.getItem()).getStoredPositions(level, stack);
                return !l.isEmpty() && l.get(0) != null;
            }
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setupGPSPositions();
        }
    }

    public enum SensorStatus implements ITranslatableEnum {
        OK,
        MISSING_GPS,
        MISSING_UPGRADE,
        NO_SENSOR;

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.universalSensor.status." + toString().toLowerCase(Locale.ROOT);
        }
    }
}
