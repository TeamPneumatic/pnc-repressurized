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
import me.desht.pneumaticcraft.common.block.entity.elevator.ElevatorCallerBlockEntity.ElevatorButton;
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
import net.minecraft.core.HolderLookup;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ElevatorBaseBlockEntity extends AbstractAirHandlingBlockEntity implements
        IGUITextFieldSensitive, IRedstoneControl<ElevatorBaseBlockEntity>, IMinWorkingPressure,
        CamouflageableBlockEntity, MenuProvider
{
    private static final List<RedstoneController.RedstoneMode<ElevatorBaseBlockEntity>> REDSTONE_LABELS = ImmutableList.of(
            new RedstoneController.ReceivingRedstoneMode<>("elevator.redstone", new ItemStack(Items.REDSTONE), te -> true),
            new RedstoneController.ReceivingRedstoneMode<>("elevator.caller", new ItemStack(ModBlocks.ELEVATOR_CALLER.get()), te -> true)
    );

    private static final float BUTTON_MARGIN = 0.2F;
    private static final float BUTTON_HEIGHT = 0.06F;
    private static final float BUTTON_SPACING = 0.02F;
    private static final int MAX_BUTTONS_PER_ROW = 9;

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
    @DescSynced
    @GuiSynced
    private final RedstoneController<ElevatorBaseBlockEntity> rsController = new RedstoneController<>(this, REDSTONE_LABELS);
    @DescSynced
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
    private int structureRefreshDelay = -1;
    private boolean suppressRedstoneModePropagation;
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

        if (!nonNullLevel().isClientSide) {
            runScheduledStructureRefresh();
        }

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
            speedMultiplier = (float) (syncedSpeedMult * PacketServerTickTime.getTickTimeMultiplier());
            if (prevCamoState != camoState) {
                fakeFloorTextureUV = ClientUtils.getTextureUV(camoState, Direction.UP);
                fakeFloorTextureTint = camoState != null && camoState.getBlock() instanceof ColorHandlers.ITintableBlock t ?
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
                setTargetExtensionForConnectedElevators(extension);
                queueStateSyncForConnectedElevators();
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

        scheduleStructureRefresh(2);
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

        double newTargetExtension = redstoneInput * maxExtension / 15;
        newTargetExtension = capTargetExtensionForPressure(newTargetExtension);

        if (oldTargetExtension != newTargetExtension) {
            setTargetExtensionForConnectedElevators(newTargetExtension);
            queueStateSyncForConnectedElevators();
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        rsController.parseRedstoneMode(tag);
    }

    @Override
    public void onRedstoneModeChanged(int newModeIdx) {
        if (level == null || level.isClientSide || suppressRedstoneModePropagation) return;

        ElevatorBaseBlockEntity core = getCoreElevatorOrSelf();
        if (core != this) {
            core.onRedstoneModeChanged(newModeIdx);
            return;
        }

        if (multiElevators == null) {
            refreshElevatorStructure();
        }

        forEachConnectedElevatorBase(base -> base.setRedstoneModeFromStructure(newModeIdx));
        queueStateSyncForConnectedElevators();
    }

    private boolean isControlledByRedstone() {
        ElevatorBaseBlockEntity core = getCoreElevator();
        if (core != null && core != this) return core.isControlledByRedstone();
        return getRedstoneController().getCurrentMode() == RS_REDSTONE_MODE;
    }

    private int getRedstoneInputLevel() {
        if (redstoneInputLevel < 0) {
            updateRedstoneInputLevel();
        }
        return Math.max(0, redstoneInputLevel);
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
        ElevatorBaseBlockEntity core = getCoreElevator();
        if (core != null && core != this) return core.getMaxElevatorHeight();

        int max = maxFloorHeight;
        if (multiElevators != null) {
            for (ElevatorBaseBlockEntity base : multiElevators) {
                max = Math.min(max, base.maxFloorHeight);
            }
        }
        return max;
    }

    public void updateMaxElevatorHeight() {
        if (level != null && !level.isClientSide) {
            refreshElevatorStructure();
        }
    }

    public static void updateElevatorsAround(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) return;

        for (int dy = -1; dy <= 1; dy++) {
            BlockPos yPos = pos.offset(0, dy, 0);
            updateElevatorAt(level, yPos);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                updateElevatorAt(level, yPos.relative(dir));
            }
        }
    }

    private static void updateElevatorAt(Level level, BlockPos pos) {
        if (isElevatorBase(level, pos)) {
            level.getBlockEntity(pos, ModBlockEntityTypes.ELEVATOR_BASE.get())
                    .ifPresent(ElevatorBaseBlockEntity::updateMaxElevatorHeight);
        }
    }

    private void scheduleStructureRefresh(int delay) {
        if (level == null || level.isClientSide) return;

        ElevatorBaseBlockEntity core = ElevatorBaseBlock.getCoreBlockEntity(level, worldPosition).orElse(this);
        if (core != this) {
            core.scheduleStructureRefresh(delay);
            return;
        }

        int clampedDelay = Math.max(0, delay);
        if (structureRefreshDelay < 0 || clampedDelay < structureRefreshDelay) {
            structureRefreshDelay = clampedDelay;
        }
    }

    private void runScheduledStructureRefresh() {
        if (structureRefreshDelay >= 0 && structureRefreshDelay-- == 0) {
            structureRefreshDelay = -1;
            refreshElevatorStructure();
        }
    }

    private void refreshElevatorStructure() {
        ElevatorBaseBlockEntity core = ElevatorBaseBlock.getCoreBlockEntity(nonNullLevel(), worldPosition).orElse(null);
        if (core == null || core.isRemoved()) return;
        if (core != this) {
            core.refreshElevatorStructure();
            return;
        }

        List<ElevatorBaseBlockEntity> elevators = findConnectedCoreElevators(nonNullLevel(), worldPosition);
        if (elevators.isEmpty()) return;

        for (ElevatorBaseBlockEntity base : elevators) {
            base.coreElevator = base;
            base.multiElevators = elevators;
            base.multiElevatorCount = elevators.size();
            base.redstoneInputLevel = -1;
            base.structureRefreshDelay = -1;
            assignCoreToVerticalStack(nonNullLevel(), base);
        }

        for (ElevatorBaseBlockEntity base : elevators) {
            base.recalculateMaxElevatorHeight();
        }

        int sharedMaxFloorHeight = elevators.stream()
                .mapToInt(base -> base.maxFloorHeight)
                .min()
                .orElse(0);
        for (ElevatorBaseBlockEntity base : elevators) {
            if (base.maxFloorHeight != sharedMaxFloorHeight) {
                base.maxFloorHeight = sharedMaxFloorHeight;
                base.setChanged();
            }
            if (base.targetExtension > sharedMaxFloorHeight) {
                base.targetExtension = sharedMaxFloorHeight;
                base.setChanged();
            }
        }

        forceUpdateFloors(true);
    }

    private static List<ElevatorBaseBlockEntity> findConnectedCoreElevators(Level level, BlockPos startPos) {
        List<ElevatorBaseBlockEntity> elevators = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> todo = new ArrayDeque<>();
        todo.add(startPos.immutable());

        while (!todo.isEmpty()) {
            BlockPos pos = todo.removeFirst();
            if (!visited.add(pos) || !isCoreBase(level, pos)) continue;

            level.getBlockEntity(pos, ModBlockEntityTypes.ELEVATOR_BASE.get()).ifPresent(base -> {
                elevators.add(base);
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos neighbourPos = pos.relative(dir);
                    if (!visited.contains(neighbourPos) && isCoreBase(level, neighbourPos)) {
                        todo.add(neighbourPos);
                    }
                }
            });
        }

        return elevators;
    }

    private static void assignCoreToVerticalStack(Level level, ElevatorBaseBlockEntity core) {
        BlockPos.MutableBlockPos pos = core.getBlockPos().mutable();
        pos.move(Direction.DOWN);
        while (isElevatorBase(level, pos)) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof ElevatorBaseBlockEntity base) {
                base.coreElevator = core;
                base.multiElevators = null;
                base.multiElevatorCount = 0;
                base.redstoneInputLevel = -1;
                base.structureRefreshDelay = -1;
            }
            pos.move(Direction.DOWN);
        }
    }

    private void recalculateMaxElevatorHeight() {
        Level level = nonNullLevel();
        int frames = 0;
        BlockPos.MutableBlockPos pos = worldPosition.mutable();
        pos.move(Direction.UP);
        while (level.isLoaded(pos) && level.getBlockState(pos).getBlock() == ModBlocks.ELEVATOR_FRAME.get()) {
            frames++;
            pos.move(Direction.UP);
        }

        int elevatorBases = 1;
        pos.set(worldPosition);
        pos.move(Direction.DOWN);
        while (isElevatorBase(level, pos)) {
            elevatorBases++;
            pos.move(Direction.DOWN);
        }

        int newMaxFloorHeight = Math.min(frames, elevatorBases * ConfigHelper.common().machines.elevatorBaseBlocksPerBase.get());
        if (maxFloorHeight != newMaxFloorHeight) {
            maxFloorHeight = newMaxFloorHeight;
            setChanged();
        }
    }

    private static boolean isCoreBase(Level level, BlockPos pos) {
        return isElevatorBase(level, pos) && !isElevatorBase(level, pos.above());
    }

    private static boolean isElevatorBase(Level level, BlockPos pos) {
        return level.isLoaded(pos) && level.getBlockState(pos).getBlock() == ModBlocks.ELEVATOR_BASE.get();
    }

    private void forceUpdateFloors(boolean notifyClient) {
        lastFloorUpdate = nonNullLevel().getGameTime() - 21;
        updateFloors(notifyClient);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
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
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putDouble("extensionD", extension);
        tag.putDouble("targetExtensionD", targetExtension);
        tag.putInt("maxFloorHeight", maxFloorHeight);
    }

    @Override
    public void readFromPacket(CompoundTag tag, HolderLookup.Provider provider) {
        super.readFromPacket(tag, provider);

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
    public void writeToPacket(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeToPacket(tag, provider);

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

    @Override
    public void onNeighborBlockUpdate(BlockPos fromPos) {
        super.onNeighborBlockUpdate(fromPos);
        updateConnections();
        ElevatorBaseBlockEntity core = getCoreElevator();
        if (core != null) {
            core.redstoneInputLevel = -1;
            core.scheduleStructureRefresh(0);
        }
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
        ElevatorBaseBlockEntity core = getCoreElevatorOrSelf();
        if (core != this) {
            core.updateFloors(notifyClient);
            return;
        }

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
                        for (Direction dir : Direction.Plane.HORIZONTAL) {
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

        ElevatorButton[] elevatorButtons = layoutElevatorButtons();

        if (multiElevators != null) {
            for (ElevatorBaseBlockEntity base : multiElevators) {
                base.floorNames = new Int2ObjectOpenHashMap<>(floorNames);
            }
            copyClientFacingStateToVerticalStacks(level);
        }

        for (BlockPos p : callerList) {
            BlockEntity te = level.getBlockEntity(p);
            if (te instanceof ElevatorCallerBlockEntity caller) {
                int callerFloorHeight = p.getY() - getBlockPos().getY() - 2;
                int callerFloor = -1;
                for (ElevatorButton floor : elevatorButtons) {
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

        if (notifyClient && !level.isClientSide) queueStateSyncForConnectedElevators();
    }

    private ElevatorButton[] layoutElevatorButtons() {
        ElevatorButton[] elevatorButtons = new ElevatorButton[floorHeights.length];
        int columns = (elevatorButtons.length - 1) / MAX_BUTTONS_PER_ROW + 1;
        int buttonsPerCol = Math.ceilDiv(floorHeights.length, columns);
        float buttonWidth = (1f - BUTTON_MARGIN * 2 - BUTTON_SPACING * (columns - 1)) / columns;

        float posX = BUTTON_MARGIN;
        float bh = buttonsPerCol * BUTTON_HEIGHT + (buttonsPerCol - 1) * BUTTON_SPACING;
        float startPosY = 1f - (1f - bh) / 2 - BUTTON_HEIGHT;
        float posY = startPosY;
        int row = 0;
        for (int idx = 0; idx < elevatorButtons.length; idx++) {
            int fh = floorHeights[idx];
            elevatorButtons[idx] = new ElevatorButton(posX, posY, buttonWidth, BUTTON_HEIGHT, idx, fh);
            elevatorButtons[idx].setColor(fh == targetExtension ? 0 : 1, 1, fh == targetExtension ? 0 : 1);
            String floorName = floorNames.get(fh);
            if (floorName != null) {
                elevatorButtons[idx].buttonText = floorName;
            } else {
                floorNames.put(fh, elevatorButtons[idx].buttonText);
            }
            if (++row >= buttonsPerCol) {
                row = 0;
                posY = startPosY;
                posX += BUTTON_SPACING + buttonWidth;
            } else {
                posY -= BUTTON_HEIGHT + BUTTON_SPACING;
            }
        }
        return elevatorButtons;
    }

    public void goToFloor(int floor) {
        ElevatorBaseBlockEntity core = getCoreElevatorOrSelf();
        if (core != this) {
            core.goToFloor(floor);
            return;
        }

        if (isControlledByRedstone()) {
            getRedstoneController().setCurrentMode(RS_CALLER_MODE);
        }
        if (floor >= 0 && floor < floorHeights.length) {
            setTargetHeight(floorHeights[floor]);
        }
        updateFloors(false);
        queueStateSyncForConnectedElevators();
    }

    private void setTargetHeight(float height) {
        ElevatorBaseBlockEntity core = getCoreElevatorOrSelf();
        if (core != this) {
            core.setTargetHeight(height);
            return;
        }

        height = Math.min(height, getMaxElevatorHeight());
        setTargetExtensionForConnectedElevators(height);
    }

    public double getTargetExtension() {
        ElevatorBaseBlockEntity core = getCoreElevator();
        if (core != null && core != this) return core.getTargetExtension();
        return targetExtension;
    }

    private void queueStateSyncForConnectedElevators() {
        if (level == null || level.isClientSide) return;

        // Queue a full sync so lazy fields and extra packet data are serialized after all shared state is stable.
        forEachConnectedElevatorBase(base -> {
            base.setChanged();
            base.scheduleDescriptionPacket();
        });
    }

    private ElevatorBaseBlockEntity getCoreElevator() {
        if (coreElevator == null || coreElevator.isRemoved()
                || (nonNullLevel().isClientSide && (nonNullLevel().getGameTime() & 0x3f) == 0)) {
            // bit of a hack; force a recalc every 64 ticks on the client
            coreElevator = ElevatorBaseBlock.getCoreBlockEntity(nonNullLevel(), getBlockPos()).orElse(null);
        }
        return coreElevator;
    }

    public boolean isCoreElevator() {
        return getCoreElevator() == this;
    }

    private ElevatorBaseBlockEntity getCoreElevatorOrSelf() {
        ElevatorBaseBlockEntity core = getCoreElevator();
        return core == null || core.isRemoved() ? this : core;
    }

    private List<ElevatorBaseBlockEntity> getConnectedCoreElevators() {
        ElevatorBaseBlockEntity core = getCoreElevatorOrSelf();
        if (core != this) {
            return core.multiElevators != null ? core.multiElevators : List.of(core);
        }
        return multiElevators != null ? multiElevators : List.of(this);
    }

    private void forEachConnectedElevatorBase(Consumer<ElevatorBaseBlockEntity> consumer) {
        Level level = nonNullLevel();
        Set<BlockPos> seen = new HashSet<>();
        for (ElevatorBaseBlockEntity core : getConnectedCoreElevators()) {
            if (core == null || core.isRemoved()) continue;
            BlockPos.MutableBlockPos pos = core.getBlockPos().mutable();
            while (isElevatorBase(level, pos)) {
                BlockEntity te = level.getBlockEntity(pos);
                if (te instanceof ElevatorBaseBlockEntity base && seen.add(base.getBlockPos())) {
                    consumer.accept(base);
                }
                pos.move(Direction.DOWN);
            }
        }
    }

    private void setRedstoneModeFromStructure(int newModeIdx) {
        if (getRedstoneController().getCurrentMode() == newModeIdx) return;

        suppressRedstoneModePropagation = true;
        try {
            getRedstoneController().setCurrentMode(newModeIdx);
        } finally {
            suppressRedstoneModePropagation = false;
        }
    }

    private void setTargetExtensionForConnectedElevators(double height) {
        for (ElevatorBaseBlockEntity base : getConnectedCoreElevators()) {
            if (base == null || base.isRemoved()) continue;
            if (base.targetExtension != height) {
                base.targetExtension = height;
                base.setChanged();
            }
            copyClientFacingStateToVerticalStack(nonNullLevel(), base);
        }
    }

    private double capTargetExtensionForPressure(double height) {
        double cappedHeight = height;
        for (ElevatorBaseBlockEntity base : getConnectedCoreElevators()) {
            if (base != null && height > base.oldExtension && base.getPressure() < PneumaticValues.MIN_PRESSURE_ELEVATOR) {
                // The connected structure has one target; any under-pressured column prevents upward movement.
                cappedHeight = Math.min(cappedHeight, base.oldExtension);
            }
        }
        return cappedHeight;
    }

    private void copyClientFacingStateToVerticalStacks(Level level) {
        for (ElevatorBaseBlockEntity core : getConnectedCoreElevators()) {
            copyClientFacingStateToVerticalStack(level, core);
        }
    }

    private static void copyClientFacingStateToVerticalStack(Level level, ElevatorBaseBlockEntity core) {
        if (core == null || core.isRemoved()) return;

        BlockPos.MutableBlockPos pos = core.getBlockPos().mutable();
        pos.move(Direction.DOWN);
        while (isElevatorBase(level, pos)) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof ElevatorBaseBlockEntity base) {
                // Non-core bases do not move/render, but client probes and tabs should report the core's shared state.
                base.targetExtension = core.targetExtension;
                base.maxFloorHeight = core.maxFloorHeight;
                base.multiElevatorCount = core.multiElevatorCount;
                base.floorHeights = core.floorHeights.clone();
                base.floorNames = new Int2ObjectOpenHashMap<>(core.floorNames);
                base.setChanged();
            }
            pos.move(Direction.DOWN);
        }
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
        ElevatorBaseBlockEntity core = getCoreElevator();
        if (core != null && core != this) return core.getFloorName(floor);
        return floor < floorHeights.length ? floorNames.get(floorHeights[floor]) : "";
    }

    public void setFloorName(int floor, String name) {
        ElevatorBaseBlockEntity core = getCoreElevatorOrSelf();
        if (core != this) {
            core.setFloorName(floor, name);
            return;
        }

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
                ElevatorBaseBlockEntity core = getCoreElevatorOrSelf();
                core.setTargetHeight(((Double) args[0]).floatValue());
                if (core.isControlledByRedstone()) {
                    core.getRedstoneController().setCurrentMode(RS_CALLER_MODE);
                }
                core.queueStateSyncForConnectedElevators();
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
                ElevatorBaseBlockEntity core = getCoreElevatorOrSelf();
                if ((Boolean) args[0] && core.isControlledByRedstone()
                        || !(Boolean) args[0] && !core.isControlledByRedstone()) {
                    core.getRedstoneController().setCurrentMode(RS_CALLER_MODE);
                    core.queueStateSyncForConnectedElevators();
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
        CamouflageableBlockEntity.onCamouflageChanged(this);
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
