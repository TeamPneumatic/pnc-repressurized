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

package me.desht.pneumaticcraft.common.block.entity.utility;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.universal_sensor.IEventSensorSetting;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.api.universal_sensor.ISensorSetting;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.client.gui.UniversalSensorScreen;
import me.desht.pneumaticcraft.common.block.entity.*;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.EmittingRedstoneMode;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.inventory.UniversalSensorMenu;
import me.desht.pneumaticcraft.common.item.GPSToolItem;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.ComputerEventManager;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.GlobalBlockEntityCacheManager;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class UniversalSensorBlockEntity extends AbstractAirHandlingBlockEntity implements
        IGUITextFieldSensitive, IMinWorkingPressure, IRangedTE,
        IRedstoneControl<UniversalSensorBlockEntity>, MenuProvider {

    private static final List<RedstoneMode<UniversalSensorBlockEntity>> REDSTONE_MODES = ImmutableList.of(
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
    private final RedstoneController<UniversalSensorBlockEntity> rsController = new RedstoneController<>(this, REDSTONE_MODES);
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
    @GuiSynced
    public int outOfRange;
    private final RangeManager rangeManager = new RangeManager(this, 0x605050D0);
    private UUID playerId;

    public UniversalSensorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.UNIVERSAL_SENSOR.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_UNIVERSAL_SENSOR, 4);
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        oldDishRotation = dishRotation;
        if (isSensorActive) {
            dishSpeed = Math.min(dishSpeed + 0.2F, 10);
        } else {
            dishSpeed = Math.max(dishSpeed - 0.2F, 0);
        }
        dishRotation += dishSpeed;
    }

    @Override
    public void tickServer() {
        super.tickServer();

        boolean invertedRedstone = rsController.getCurrentMode() == RS_MODE_INVERTED;
        tickTimer++;
        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(sensorSetting);
        if (updateStatus(sensor) == SensorStatus.OK  && getPressure() > getMinWorkingPressure()) {
            isSensorActive = true;
            addAir(-sensor.getAirUsage(getLevel(), getBlockPos()));
            if (sensor instanceof IPollSensorSetting pollSensor) {
                if (tickTimer >= pollSensor.getPollFrequency(this)) {
                    try {
                        pollSensor.setPlayerContext(playerId);
                        int newRedstoneStrength = pollSensor.getRedstoneValue(getLevel(), getBlockPos(), getRange(), sensorGuiText);
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

    private void notifyComputers(Object... params) {
        ComputerEventManager.getInstance().sendEvents(this, "universalSensor", params);
    }

    private SensorStatus updateStatus(ISensorSetting sensor) {
        sensorStatus = SensorStatus.OK;
        if (sensor != null) {
            if (sensor.needsGPSTool() && getItemHandler().getStackInSlot(0).isEmpty()) {
                sensorStatus = SensorStatus.MISSING_GPS;
            } else {
                for (PNCUpgrade upgrade: sensor.getRequiredUpgrades()) {
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

    public AABB getRenderBoundingBox() {
        return rangeManager.shouldShowRange() ? rangeManager.getExtentsAsAABB() : new AABB(getBlockPos());
    }

    public void onEvent(Event event) {
        ISensorSetting sensor = SensorHandler.getInstance().getSensorFromPath(sensorSetting);
        if (sensor instanceof IEventSensorSetting evs && getPressure() >= getMinWorkingPressure()) {
            int newRedstoneStrength = evs.emitRedstoneOnEvent(event, this, getRange(), sensorGuiText);
            if (newRedstoneStrength != 0) {
                redstonePulseCounter = evs.getRedstonePulseLength();
            }
            if (rsController.getCurrentMode() == RS_MODE_INVERTED) {
                newRedstoneStrength = 15 - newRedstoneStrength;
            }
            if (redstonePulseCounter > 0 && ThirdPartyManager.instance().isModTypeLoaded(ThirdPartyManager.ModType.COMPUTER)) {
                if (event instanceof PlayerInteractEvent e) {
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
            UniversalSensorScreen.refreshIfOpen();
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
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        tag.put("Items", itemHandler.serializeNBT(provider));
        tag.putString("sensorSetting", sensorSetting);
        tag.putFloat("dishSpeed", dishSpeed);
        tag.putString("sensorText", sensorGuiText);
        if (playerId != null) tag.putString("playerId", playerId.toString());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        itemHandler.deserializeNBT(provider, tag.getCompound("Items"));
        setSensorSetting(tag.getString("sensorSetting"));
        dishSpeed = tag.getFloat("dishSpeed");
        sensorGuiText = tag.getString("sensorText");
        if (tag.contains("playerId", Tag.TAG_STRING)) {
            playerId = UUID.fromString(tag.getString("playerId"));
        }
        setupGPSPositions();
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
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
                    if (getSensorSetting().isEmpty()) {
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

        rangeManager.setRange(getUpgrades(ModUpgrades.RANGE.get()) + BASE_RANGE);
        setupGPSPositions();
    }

    public boolean areGivenUpgradesInserted(Set<PNCUpgrade> requiredUpgrades) {
        for (PNCUpgrade upgrade : requiredUpgrades) {
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
        if (stack.getItem() instanceof IPositionProvider p) {
            int sensorRange = getRange();
            List<BlockPos> posList = p.getStoredPositions(playerId, stack);
            List<BlockPos> gpsPositions = posList.stream()
                    .filter(pos -> pos != null
                            && Math.abs(pos.getX() - getBlockPos().getX()) <= sensorRange
                            && Math.abs(pos.getY() - getBlockPos().getY()) <= sensorRange
                            && Math.abs(pos.getZ() - getBlockPos().getZ()) <= sensorRange)
                    .toList();
            positions.addAll(gpsPositions);
            outOfRange = posList.size() - gpsPositions.size();
        }
        if (getLevel() != null && getLevel().isClientSide) UniversalSensorScreen.refreshIfOpen();
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
        if (!nonNullLevel().isClientSide) scheduleDescriptionPacket();
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
                    GPSToolItem.setGPSLocation(null, stack, BlockPos.containing((Double) args[1], (Double) args[2], (Double) args[3]));
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
                    return GPSToolItem.getGPSLocation(stack)
                            .map(pos -> new Object[]{pos.getX(), pos.getY(), pos.getZ()})
                            .orElse(new Object[]{0, 0, 0});
                } else {
                    return null;
                }
            }
        });
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return itemHandler;
    }

    @Override
    public RedstoneController<UniversalSensorBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_UNIVERSAL_SENSOR;
    }

    @Override
    public MutableComponent getRedstoneTabTitle() {
        return xlate("pneumaticcraft.gui.tab.redstoneBehaviour.universalSensor.redstoneEmission");
    }

    @Override
    public void setRemoved(){
        super.setRemoved();
        GlobalBlockEntityCacheManager.getInstance(getLevel()).getUniversalSensors().remove(this);
    }

    @Override
    public void clearRemoved(){
        super.clearRemoved();
        GlobalBlockEntityCacheManager.getInstance(getLevel()).getUniversalSensors().add(this);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new UniversalSensorMenu(i, playerInventory, getBlockPos());
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    private class UniversalSensorItemHandler extends ItemStackHandler {
        UniversalSensorItemHandler() {
            super(1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (stack.getItem() instanceof IPositionProvider p) {
                List<BlockPos> l = p.getStoredPositions(playerId, stack);
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
