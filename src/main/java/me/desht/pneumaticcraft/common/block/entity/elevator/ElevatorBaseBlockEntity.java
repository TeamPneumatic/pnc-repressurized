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

package me.desht.pneumaticcraft.common.block.entity.elevator;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.ElevatorBaseBlock;
import me.desht.pneumaticcraft.common.block.entity.*;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.event.MiscEventHandler;
import me.desht.pneumaticcraft.common.inventory.ElevatorMenu;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound.MovingSoundFocus;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BlockEntityConstants;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ElevatorBaseBlockEntity extends AbstractAirHandlingBlockEntity implements
        IGUITextFieldSensitive, IRedstoneControl<ElevatorBaseBlockEntity>, IMinWorkingPressure,
        CamouflageableBlockEntity, MenuProvider
{
    private static final List<RedstoneController.RedstoneMode<ElevatorBaseBlockEntity>> REDSTONE_LABELS = ImmutableList.of(
            new RedstoneController.ReceivingRedstoneMode<>("elevator.redstone", new ItemStack(Items.REDSTONE), te -> true),
            new RedstoneController.ReceivingRedstoneMode<>("elevator.caller", new ItemStack(ModBlocks.ELEVATOR_CALLER.get()), te -> true)
    );

    private static final float BUTTON_HEIGHT = 0.06F;
    private static final float BUTTON_SPACING = 0.02F;
    private static final byte RS_REDSTONE_MODE = 0;
    private static final byte RS_CALLER_MODE = 1;

    @DescSynced
    @LazySynced
    public double extension;
    @DescSynced
    private double targetExtension;
    @DescSynced
    double syncedSpeedMult;  // speed multiplier, calculated on server, sync'd to client
    @DescSynced
    public int multiElevatorCount;  // number of elevator columns in the multiblock
    @GuiSynced
    private final RedstoneController<ElevatorBaseBlockEntity> rsController = new RedstoneController<>(this, REDSTONE_LABELS);
    @GuiSynced
    private int maxFloorHeight;
    @DescSynced
    private int chargingUpgrades; // needs to be sync'd since it affects elevator descent rate

    public double oldExtension;
    private boolean isStopped = true;
    // top elevator of the vertical stack (not to be confused with multiElevators, which is horizontal connections
    private ElevatorBaseBlockEntity coreElevator;
    // horizontally-connected multiblock; will always be non-null after init, even when only one elevator
    private List<ElevatorBaseBlockEntity> multiElevators;
    public int[] floorHeights = new int[0]; // list of every floor of Elevator Callers.
    private Int2ObjectMap<String> floorNames = new Int2ObjectOpenHashMap<>();
    private int redstoneInputLevel = -1; // current redstone input level (-1 = re-check)
    private BlockState camoState;
    private BlockState prevCamoState;
    public int ticksRunning;  // ticks since elevator started moving (0 = stopped)
    private final IntList floorList = new IntArrayList();
    private final List<BlockPos> callerList = new ArrayList<>();
    private long lastFloorUpdate = 0L;
    public float[] fakeFloorTextureUV;
    public int fakeFloorTextureTint;
    public int lightAbove;

    public ElevatorBaseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ELEVATOR_BASE.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_ELEVATOR, 4);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @Override
    public void tickCommonPre() {
        oldExtension = extension;

        super.tickCommonPre();

        if (!isCoreElevator()) {
            extension = 0f;
            return;
        }

        double speedMultiplier;
        if (!nonNullLevel().isClientSide) {
            if (isControlledByRedstone()) {
                handleRedstoneControl();
            }
            speedMultiplier = syncedSpeedMult = getSpeedMultiplierFromUpgrades();
            chargingUpgrades = getUpgrades(ModUpgrades.CHARGING.get());  // sync'd to client to adjust elevator speed as appropriate
            MiscEventHandler.needsTPSSync(getLevel());
        } else {
            speedMultiplier = (float) (syncedSpeedMult * PacketServerTickTime.tickTimeMultiplier);
            if (prevCamoState != camoState) {
                fakeFloorTextureUV = ClientUtils.getTextureUV(camoState, Direction.UP);
                fakeFloorTextureTint = camoState.getBlock() instanceof ColorHandlers.ITintableBlock t ?
                        0xFF000000 | t.getTintColor(camoState, level, getBlockPos(), 0) :
                        0xFFFFFFFF;
                prevCamoState = camoState;
            }
            if ((nonNullLevel().getGameTime() & 0xf) == 0) {
                // kludge to prevent elevator TER rendering unlit sometimes
                lightAbove = ClientUtils.getLightAt(worldPosition.above());
            }
        }

        if (extension < targetExtension) {
            if (!nonNullLevel().isClientSide && getPressure() < PneumaticValues.MIN_PRESSURE_ELEVATOR) {
                targetExtension = extension;
            }
            double moveBy = extension < targetExtension - BlockEntityConstants.ELEVATOR_SLOW_EXTENSION ?
                    BlockEntityConstants.ELEVATOR_SPEED_FAST * speedMultiplier :
                    BlockEntityConstants.ELEVATOR_SPEED_SLOW * speedMultiplier;
            extension = Math.min(targetExtension, extension + moveBy);
            addAir((int) ((oldExtension - extension) * PneumaticValues.USAGE_ELEVATOR * (getSpeedUsageMultiplierFromUpgrades() / speedMultiplier)));
        } else if (extension > targetExtension) {
            double chargingSlowdown = 1.0 - chargingUpgrades * 0.1;
            double moveBy = extension > targetExtension + BlockEntityConstants.ELEVATOR_SLOW_EXTENSION ?
                    BlockEntityConstants.ELEVATOR_SPEED_FAST * speedMultiplier * chargingSlowdown:
                    BlockEntityConstants.ELEVATOR_SPEED_SLOW * speedMultiplier * chargingSlowdown;
            extension = Math.max(targetExtension, extension - moveBy);
            if (!nonNullLevel().isClientSide && chargingUpgrades > 0 && getPressure() < airHandler.getDangerPressure() - 0.1f) {
                float mul = 0.15f * Math.min(4, chargingUpgrades);
                addAir((int) ((oldExtension - extension) * PneumaticValues.USAGE_ELEVATOR * mul * (getSpeedUsageMultiplierFromUpgrades() / speedMultiplier)));
            }
        }

        if (PneumaticCraftUtils.epsilonEquals(oldExtension, extension) && !isStopped) {
            // just arrived
            isStopped = true;
            ticksRunning = 0;
            playStopStartSound();
            if (!nonNullLevel().isClientSide) updateFloors(false);
        } else if (!PneumaticCraftUtils.epsilonEquals(oldExtension, extension) && isStopped) {
            // just departed
            isStopped = false;
            playStopStartSound();
        }
        if (!isStopped) ticksRunning++;
    }

    @Override
    public void onLoad() {
        super.onLoad();

        connectAsMultiblock();
    }

    private void playStopStartSound() {
        if (shouldPlaySounds()) {
            if (nonNullLevel().isClientSide()) {
                nonNullLevel().playLocalSound(getBlockPos().getX() + 0.5, getBlockPos().getY() + extension, getBlockPos().getZ() + 0.5,
                        isStopped ? ModSounds.ELEVATOR_RISING_STOP.get() : ModSounds.ELEVATOR_RISING_START.get(),
                        SoundSource.BLOCKS, ConfigHelper.client().sound.elevatorVolumeStartStop.get().floatValue(), 1.0F, true);
            } else if (!isStopped) {
                NetworkHandler.sendToAllTracking(new PacketPlayMovingSound(MovingSounds.Sound.ELEVATOR, MovingSoundFocus.of(getCoreElevator())), this);
            }
        }
    }

    private boolean shouldPlaySounds() {
        return !(getCachedNeighbor(Direction.EAST) instanceof ElevatorBaseBlockEntity)
                && !(getCachedNeighbor(Direction.SOUTH) instanceof ElevatorBaseBlockEntity);
    }

    public boolean isStopped() {
        return isStopped;
    }

    private void handleRedstoneControl() {
        double oldTargetExtension = targetExtension;
        float maxExtension = getMaxElevatorHeight();

        int redstoneInput = getRedstoneInputLevel();
        if (multiElevators != null) {
            for (ElevatorBaseBlockEntity base : multiElevators) {
                redstoneInput = Math.max(redstoneInput, base.getRedstoneInputLevel());
            }
        }

        targetExtension = redstoneInput * maxExtension / 15;
        if (targetExtension > oldExtension && getPressure() < PneumaticValues.MIN_PRESSURE_ELEVATOR) {
            // we can descend at any time, but only ascend when there's sufficient pressure
            targetExtension = oldExtension;
        }

        if (oldTargetExtension != targetExtension) {
            sendDescPacketFromAllElevators();
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        rsController.parseRedstoneMode(tag);
    }

    @Override
    public void onRedstoneModeChanged(int newModeIdx) {
        if (multiElevators != null) {
            for (ElevatorBaseBlockEntity base : multiElevators) {
                base.getRedstoneController().setCurrentMode(newModeIdx);
            }
        }

        int i = -1;
        BlockEntity te = nonNullLevel().getBlockEntity(getBlockPos().relative(Direction.DOWN));
        while (te instanceof ElevatorBaseBlockEntity elevator) {
            elevator.getRedstoneController().setCurrentMode(newModeIdx);
            i--;
            te = nonNullLevel().getBlockEntity(getBlockPos().offset(0, i, 0));
        }
    }

    private boolean isControlledByRedstone() {
        return getRedstoneController().getCurrentMode() == RS_REDSTONE_MODE;
    }

    private int getRedstoneInputLevel() {
        if (redstoneInputLevel < 0) {
            updateRedstoneInputLevel();
        }
        return redstoneInputLevel;
    }

    private void updateRedstoneInputLevel() {
        if (multiElevators == null) return;

        int maxRedstone = getMaxRedstone();
        for (ElevatorBaseBlockEntity base : multiElevators) {
            base.redstoneInputLevel = maxRedstone;
        }
    }

    private int getMaxRedstone() {
        int maxRedstone = 0;
        for (ElevatorBaseBlockEntity base : multiElevators) {
            BlockPos.MutableBlockPos pos1 = base.getBlockPos().mutable();
            while (nonNullLevel().getBlockState(pos1).getBlock() == ModBlocks.ELEVATOR_BASE.get()) {
                maxRedstone = Math.max(maxRedstone, nonNullLevel().getBestNeighborSignal(pos1));
                if (maxRedstone == 15) return 15;
                pos1.move(Direction.DOWN);
            }
        }
        return maxRedstone;
    }

    public float getMaxElevatorHeight() {
        int max = maxFloorHeight;
        if (multiElevators != null) {
            for (ElevatorBaseBlockEntity base : multiElevators) {
                max = Math.min(max, base.maxFloorHeight);
            }
        }
        return max;
    }

    public void updateMaxElevatorHeight() {
        int i = -1;
        do {
            i++;
        } while (nonNullLevel().getBlockState(getBlockPos().offset(0, i + 1, 0)).getBlock() == ModBlocks.ELEVATOR_FRAME.get());
        int elevatorBases = 0;
        do {
            elevatorBases++;
        } while (nonNullLevel().getBlockState(getBlockPos().offset(0, -elevatorBases, 0)).getBlock() == ModBlocks.ELEVATOR_BASE.get());

        maxFloorHeight = Math.min(i, elevatorBases * ConfigHelper.common().machines.elevatorBaseBlocksPerBase.get());
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("extensionD")) {
            extension = tag.getDouble("extensionD");
            targetExtension = tag.getDouble("targetExtensionD");
        } else {
            extension = tag.getFloat("extension");
            targetExtension = tag.getFloat("targetExtension");
        }
        maxFloorHeight = tag.getInt("maxFloorHeight");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("extensionD", extension);
        tag.putDouble("targetExtensionD", targetExtension);
        tag.putInt("maxFloorHeight", maxFloorHeight);
    }

    @Override
    public void readFromPacket(CompoundTag tag) {
        super.readFromPacket(tag);

        camoState = CamouflageableBlockEntity.readCamo(tag);
        floorHeights = tag.getIntArray("floorHeights");

        floorNames.clear();
        ListTag floorNameList = tag.getList("floorNames", Tag.TAG_COMPOUND);
        for (int i = 0; i < floorNameList.size(); i++) {
            CompoundTag floorName = floorNameList.getCompound(i);
            floorNames.put(floorName.getInt("floorHeight"), floorName.getString("floorName"));
        }
    }

    @Override
    public void writeToPacket(CompoundTag tag) {
        super.writeToPacket(tag);

        CamouflageableBlockEntity.writeCamo(tag, camoState);
        tag.putIntArray("floorHeights", floorHeights);

        ListTag floorNameList = new ListTag();
        floorNames.forEach((height, name) -> {
            CompoundTag floorNameTag = new CompoundTag();
            floorNameTag.putInt("floorHeight", height);
            floorNameTag.putString("floorName", name);
            floorNameList.add(floorNameTag);
        });
        tag.put("floorNames", floorNameList);
    }

    private void connectAsMultiblock() {
        multiElevators = null;
        if (isCoreElevator()) {
            multiElevators = new ArrayList<>();
            Stack<ElevatorBaseBlockEntity> todo = new Stack<>();
            todo.add(this);
            while (!todo.isEmpty()) {
                ElevatorBaseBlockEntity curElevator = todo.pop();
                if (curElevator.isCoreElevator() && !multiElevators.contains(curElevator)) {
                    multiElevators.add(curElevator);
                    curElevator.multiElevators = multiElevators;
                    for (Direction face : DirectionUtil.HORIZONTALS) {
                        BlockEntity te = curElevator.getCachedNeighbor(face);
                        if (te instanceof ElevatorBaseBlockEntity && !te.isRemoved()) {
                            todo.push((ElevatorBaseBlockEntity) te);
                        }
                    }
                }
            }
            multiElevatorCount = multiElevators.size();
        }
    }

    @Override
    public void onNeighborBlockUpdate(BlockPos fromPos) {
        super.onNeighborBlockUpdate(fromPos);
        getCoreElevator().updateRedstoneInputLevel();
        connectAsMultiblock();
        updateConnections();
    }

    /**
     * Called when a block neighbour changes: if this elevator has no elevators above it, set it as a core
     * elevator, and inform all elevators below us of that fact.
     */
    private void updateConnections() {
        if (nonNullLevel().getBlockState(getBlockPos().above()).getBlock() != ModBlocks.ELEVATOR_BASE.get()) {
            coreElevator = this;
        } else {
            coreElevator = null; // force recalc
        }
    }

    /**
     * Elevator above us has just been broken; move its upgrades and camo to this elevator base, if possible.
     */
    public void moveUpgradesFromAbove() {
        BlockEntity brokenTE = nonNullLevel().getBlockEntity(getBlockPos().relative(Direction.UP));
        if (brokenTE instanceof ElevatorBaseBlockEntity) {
            camoState = ((ElevatorBaseBlockEntity) brokenTE).camoState;
            sendDescriptionPacket();

            for (int i = 0; i < getUpgradeHandler().getSlots(); i++) {
                ItemStack stack = ((ElevatorBaseBlockEntity) brokenTE).getUpgradeHandler().getStackInSlot(i);
                ItemStack excess = ItemHandlerHelper.insertItem(getUpgradeHandler(), stack, false);
                if (!excess.isEmpty()) PneumaticCraftUtils.dropItemOnGround(excess, level, getBlockPos());
                ((ElevatorBaseBlockEntity) brokenTE).getUpgradeHandler().setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public void updateFloors(boolean notifyClient) {
        Level level = nonNullLevel();
        if (level.getGameTime() - lastFloorUpdate > 20) {
            callerList.clear();
            floorList.clear();
            if (multiElevators != null) {  // should always be the case, even if only a single member
                int yOffset = 0;
                int worldHeight = level.dimensionType().logicalHeight();
                BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
                scanLoop: while (worldPosition.getY() + yOffset < worldHeight - 2) {
                    boolean registeredThisFloor = false;
                    for (ElevatorBaseBlockEntity base : multiElevators) {
                        for (Direction dir : DirectionUtil.HORIZONTALS) {
                            mut.set(base.getBlockPos());
                            mut.move(dir.getStepX(), yOffset + 2, dir.getStepZ());
                            if (base.nonNullLevel().getBlockState(mut).getBlock() == ModBlocks.ELEVATOR_CALLER.get()) {
                                callerList.add(mut.immutable());
                                if (!registeredThisFloor) floorList.add(yOffset);
                                registeredThisFloor = true;
                            }
                        }
                    }
                    yOffset++;
                    for (ElevatorBaseBlockEntity base : multiElevators) {
                        if (base.nonNullLevel().getBlockState(base.getBlockPos().above(yOffset)).getBlock() != ModBlocks.ELEVATOR_FRAME.get()) {
                            break scanLoop;
                        }
                    }
                }

                for (ElevatorBaseBlockEntity base : multiElevators) {
                    base.floorHeights = floorList.intStream().toArray();
                }
            }
            lastFloorUpdate = level.getGameTime();
        }

        ElevatorCallerBlockEntity.ElevatorButton[] elevatorButtons = new ElevatorCallerBlockEntity.ElevatorButton[floorHeights.length];
        int columns = (elevatorButtons.length - 1) / 12 + 1;
        for (int j = 0; j < columns; j++) {
            for (int i = j * 12; i < floorHeights.length && i < j * 12 + 12; i++) {
                elevatorButtons[i] = new ElevatorCallerBlockEntity.ElevatorButton(0.2F + 0.6F / columns * j, 0.5F + (Math.min(floorHeights.length, 12) - 2) * (BUTTON_SPACING + BUTTON_HEIGHT) / 2 - i % 12 * (BUTTON_HEIGHT + BUTTON_SPACING), 0.58F / columns, BUTTON_HEIGHT, i, floorHeights[i]);
                elevatorButtons[i].setColor(floorHeights[i] == targetExtension ? 0 : 1, 1, floorHeights[i] == targetExtension ? 0 : 1);
                String floorName = floorNames.get(floorHeights[i]);
                if (floorName != null) {
                    elevatorButtons[i].buttonText = floorName;
                } else {
                    floorNames.put(floorHeights[i], elevatorButtons[i].buttonText);
                }
            }
        }

        if (multiElevators != null) {
            for (ElevatorBaseBlockEntity base : multiElevators) {
                base.floorNames = new Int2ObjectOpenHashMap<>(floorNames);
            }
        }

        for (BlockPos p : callerList) {
            BlockEntity te = level.getBlockEntity(p);
            if (te instanceof ElevatorCallerBlockEntity caller) {
                int callerFloorHeight = p.getY() - getBlockPos().getY() - 2;
                int callerFloor = -1;
                for (ElevatorCallerBlockEntity.ElevatorButton floor : elevatorButtons) {
                    if (floor.floorHeight == callerFloorHeight) {
                        callerFloor = floor.floorNumber;
                        break;
                    }
                }
                if (callerFloor == -1) {
                    Log.error("Error while updating elevator floors! This will cause a indexOutOfBoundsException, index = -1");
                }
                caller.setEmittingRedstone(PneumaticCraftUtils.epsilonEquals(targetExtension, extension, 0.1)
                        && PneumaticCraftUtils.epsilonEquals(extension, callerFloorHeight, 0.1));
                caller.setFloors(elevatorButtons, callerFloor);
            }
        }

        if (notifyClient && !level.isClientSide) sendDescPacketFromAllElevators();
    }

    public void goToFloor(int floor) {
        if (getCoreElevator().isControlledByRedstone()) {
            getCoreElevator().getRedstoneController().setCurrentMode(RS_CALLER_MODE);
        }
        if (floor >= 0 && floor < floorHeights.length) {
            setTargetHeight(floorHeights[floor]);
        }
        updateFloors(false);
        sendDescPacketFromAllElevators();
    }

    private void setTargetHeight(float height) {
        height = Math.min(height, getMaxElevatorHeight());
        if (multiElevators != null) {
            for (ElevatorBaseBlockEntity base : multiElevators) {
                base.targetExtension = height;
            }
        }
    }

    public double getTargetExtension() {
        return targetExtension;
    }

    private void sendDescPacketFromAllElevators() {
        if (multiElevators != null) {
            for (ElevatorBaseBlockEntity base : multiElevators) {
                base.sendDescriptionPacket();
            }
        } else {
            sendDescriptionPacket();
        }
    }

    private ElevatorBaseBlockEntity getCoreElevator() {
        if (coreElevator == null || (nonNullLevel().isClientSide && (nonNullLevel().getGameTime() & 0x3f) == 0)) {
            // bit of a hack; force a recalc every 64 ticks on the client
            coreElevator = ElevatorBaseBlock.getCoreTileEntity(nonNullLevel(), getBlockPos());
        }
        return coreElevator;
    }

    public boolean isCoreElevator() {
        return getCoreElevator() == this;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        // only connect to other elevator bases on the UP face
        return side != Direction.UP || nonNullLevel().getBlockState(worldPosition.relative(side)).getBlock() == ModBlocks.ELEVATOR_BASE.get();
    }

    @Override
    public void setText(int textFieldID, String text) {
        setFloorName(textFieldID, text);
    }

    @Override
    public String getText(int textFieldID) {
        return getFloorName(textFieldID);
    }

    public String getFloorName(int floor) {
        return floor < floorHeights.length ? floorNames.get(floorHeights[floor]) : "";
    }

    public void setFloorName(int floor, String name) {
        if (floor < floorHeights.length) {
            floorNames.put(floorHeights[floor], name);
            updateFloors(true);
        }
    }

    @Override
    public boolean isGuiUseableByPlayer(Player par1EntityPlayer) {
        return nonNullLevel().getBlockEntity(getBlockPos()) == this;
    }

    @Override
    public void addLuaMethods(LuaMethodRegistry registry) {
        super.addLuaMethods(registry);

        registry.registerLuaMethod(new LuaMethod("setHeight") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "height (in blocks)");
                setTargetHeight(((Double) args[0]).floatValue());
                if (getCoreElevator().isControlledByRedstone()) {
                    getCoreElevator().getRedstoneController().setCurrentMode(RS_CALLER_MODE);
                }
                getCoreElevator().sendDescPacketFromAllElevators();
                return null;
            }
        });

        registry.registerLuaMethod(new LuaMethod("getCurrentHeight") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[] { getCoreElevator().extension };
            }
        });
        registry.registerLuaMethod(new LuaMethod("getTargetHeight") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[] { getCoreElevator().targetExtension };
            }
        });
        registry.registerLuaMethod(new LuaMethod("getVelocity") {
            @Override
            public Object[] call(Object[] args) {
                requireNoArgs(args);
                return new Object[] { getCoreElevator().extension - getCoreElevator().oldExtension };
            }
        });

        registry.registerLuaMethod(new LuaMethod("setExternalControl") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "true/false");
                if ((Boolean) args[0] && getCoreElevator().isControlledByRedstone()
                        || !(Boolean) args[0] && !getCoreElevator().isControlledByRedstone()) {
                    getCoreElevator().getRedstoneController().setCurrentMode(RS_CALLER_MODE);
                }
                return null;
            }
        });
    }

    @Override
    public IItemHandler getItemHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return null;
    }

    @Override
    public RedstoneController<ElevatorBaseBlockEntity> getRedstoneController() {
        return rsController;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_ELEVATOR;
    }

    @Override
    public BlockState getCamouflage() {
        return camoState;
    }

    @Override
    public void setCamouflage(BlockState state) {
        camoState = state;
        CamouflageableBlockEntity.syncToClient(this);
    }

    @Override
    public MutableComponent getRedstoneTabTitle() {
        return xlate("pneumaticcraft.gui.tab.redstoneBehaviour.elevator.controlBy");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new ElevatorMenu(i, playerInventory, getBlockPos());
    }
}
