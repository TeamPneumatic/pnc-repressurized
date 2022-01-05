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
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.block.BlockElevatorBase;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerElevator;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound.SoundSource;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.ReceivingRedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorCaller.ElevatorButton;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class TileEntityElevatorBase extends TileEntityPneumaticBase implements
        IGUITextFieldSensitive, IRedstoneControl<TileEntityElevatorBase>, IMinWorkingPressure,
        ICamouflageableTE, INamedContainerProvider
{
    private static final List<RedstoneMode<TileEntityElevatorBase>> REDSTONE_LABELS = ImmutableList.of(
            new ReceivingRedstoneMode<>("elevator.redstone", new ItemStack(Items.REDSTONE), te -> true),
            new ReceivingRedstoneMode<>("elevator.caller", new ItemStack(ModBlocks.ELEVATOR_CALLER.get()), te -> true)
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
    private final RedstoneController<TileEntityElevatorBase> rsController = new RedstoneController<>(this, REDSTONE_LABELS);
    @GuiSynced
    private int maxFloorHeight;
    @DescSynced
    private int chargingUpgrades; // needs to be sync'd since it affects elevator descent rate

    public double oldExtension;
    private boolean isStopped = true;
    // top elevator of the vertical stack (not to be confused with multiElevators, which is horizontal connections
    private TileEntityElevatorBase coreElevator;
    // horizontally-connected multiblock; will always be non-null after init, even when only one elevator
    private List<TileEntityElevatorBase> multiElevators;
    public int[] floorHeights = new int[0]; // list of every floor of Elevator Callers.
    private HashMap<Integer, String> floorNames = new HashMap<>();
    private int redstoneInputLevel; // current redstone input level
    private BlockState camoState;
    private BlockState prevCamoState;
    public int ticksRunning;  // ticks since elevator started moving (0 = stopped)
    private final List<Integer> floorList = new ArrayList<>();
    private final List<BlockPos> callerList = new ArrayList<>();
    private long lastFloorUpdate = 0L;
    public float[] fakeFloorTextureUV;
    public int lightAbove;

    public TileEntityElevatorBase() {
        super(ModTileEntities.ELEVATOR_BASE.get(), PneumaticValues.DANGER_PRESSURE_ELEVATOR, PneumaticValues.MAX_PRESSURE_ELEVATOR, PneumaticValues.VOLUME_ELEVATOR, 4);
    }

    @Override
    public void tick() {
        oldExtension = extension;

        super.tick();

        if (!isCoreElevator()) {
            extension = 0f;
            return;
        }

        double speedMultiplier;
        if (!getLevel().isClientSide) {
            if (isControlledByRedstone()) {
                handleRedstoneControl();
            }
            speedMultiplier = syncedSpeedMult = getSpeedMultiplierFromUpgrades();
            chargingUpgrades = getUpgrades(EnumUpgrade.CHARGING);  // sync'd to client to adjust elevator speed as appropriate
        } else {
            speedMultiplier = (float) (syncedSpeedMult * PacketServerTickTime.tickTimeMultiplier);
            if (prevCamoState != camoState) {
                fakeFloorTextureUV = ClientUtils.getTextureUV(camoState, Direction.UP);
                prevCamoState = camoState;
            }
            if ((getLevel().getGameTime() & 0xf) == 0) {
                // kludge to prevent elevator TER rendering unlit sometimes
                lightAbove = ClientUtils.getLightAt(worldPosition.above());
            }
        }

        if (extension < targetExtension) {
            if (!getLevel().isClientSide && getPressure() < PneumaticValues.MIN_PRESSURE_ELEVATOR) {
                targetExtension = extension;
            }
            double moveBy = extension < targetExtension - TileEntityConstants.ELEVATOR_SLOW_EXTENSION ?
                    TileEntityConstants.ELEVATOR_SPEED_FAST * speedMultiplier :
                    TileEntityConstants.ELEVATOR_SPEED_SLOW * speedMultiplier;
            extension = Math.min(targetExtension, extension + moveBy);
            addAir((int) ((oldExtension - extension) * PneumaticValues.USAGE_ELEVATOR * (getSpeedUsageMultiplierFromUpgrades() / speedMultiplier)));
        } else if (extension > targetExtension) {
            double chargingSlowdown = 1.0 - chargingUpgrades * 0.1;
            double moveBy = extension > targetExtension + TileEntityConstants.ELEVATOR_SLOW_EXTENSION ?
                    TileEntityConstants.ELEVATOR_SPEED_FAST * speedMultiplier * chargingSlowdown:
                    TileEntityConstants.ELEVATOR_SPEED_SLOW * speedMultiplier * chargingSlowdown;
            extension = Math.max(targetExtension, extension - moveBy);
            if (!getLevel().isClientSide && chargingUpgrades > 0 && getPressure() < airHandler.getDangerPressure() - 0.1f) {
                float mul = 0.15f * Math.min(4, chargingUpgrades);
                addAir((int) ((oldExtension - extension) * PneumaticValues.USAGE_ELEVATOR * mul * (getSpeedUsageMultiplierFromUpgrades() / speedMultiplier)));
            }
        }

        if (PneumaticCraftUtils.epsilonEquals(oldExtension, extension) && !isStopped) {
            // just arrived
            isStopped = true;
            ticksRunning = 0;
            playStopStartSound();
            if (!getLevel().isClientSide) updateFloors(false);
        } else if (!PneumaticCraftUtils.epsilonEquals(oldExtension, extension) && isStopped) {
            // just departed
            isStopped = false;
            playStopStartSound();
        }
        if (!isStopped) ticksRunning++;
    }

    @Override
    protected void onFirstServerTick() {
        super.onFirstServerTick();

        connectAsMultiblock();
    }

    private void playStopStartSound() {
        if (shouldPlaySounds()) {
            if (getLevel().isClientSide()) {
                getLevel().playLocalSound(getBlockPos().getX() + 0.5, getBlockPos().getY() + extension, getBlockPos().getZ() + 0.5,
                        isStopped ? ModSounds.ELEVATOR_RISING_STOP.get() : ModSounds.ELEVATOR_RISING_START.get(),
                        SoundCategory.BLOCKS, ConfigHelper.client().sound.elevatorVolumeStartStop.get().floatValue(), 1.0F, true);
            } else if (!isStopped) {
                NetworkHandler.sendToAllTracking(new PacketPlayMovingSound(MovingSounds.Sound.ELEVATOR, SoundSource.of(getCoreElevator())), this);
            }
        }
    }

    private boolean shouldPlaySounds() {
        return !(getCachedNeighbor(Direction.EAST) instanceof TileEntityElevatorBase)
                && !(getCachedNeighbor(Direction.SOUTH) instanceof TileEntityElevatorBase);
    }

    public boolean isStopped() {
        return isStopped;
    }

    private void handleRedstoneControl() {
        double oldTargetExtension = targetExtension;
        float maxExtension = getMaxElevatorHeight();

        int redstoneInput = redstoneInputLevel;
        if (multiElevators != null) {
            for (TileEntityElevatorBase base : multiElevators) {
                redstoneInput = Math.max(redstoneInputLevel, base.redstoneInputLevel);
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
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        rsController.parseRedstoneMode(tag);
    }

    @Override
    public void onRedstoneModeChanged(int newModeIdx) {
        if (multiElevators != null) {
            for (TileEntityElevatorBase base : multiElevators) {
                base.getRedstoneController().setCurrentMode(newModeIdx);
            }
        }

        int i = -1;
        TileEntity te = getLevel().getBlockEntity(getBlockPos().relative(Direction.DOWN));
        while (te instanceof TileEntityElevatorBase) {
            ((TileEntityElevatorBase) te).getRedstoneController().setCurrentMode(newModeIdx);
            i--;
            te = getLevel().getBlockEntity(getBlockPos().offset(0, i, 0));
        }
    }

    private boolean isControlledByRedstone() {
        return getRedstoneController().getCurrentMode() == RS_REDSTONE_MODE;
    }

    private void updateRedstoneInputLevel() {
        if (multiElevators == null) return;

        int maxRedstone = getMaxRedstone();
        for (TileEntityElevatorBase base : multiElevators) {
            base.redstoneInputLevel = maxRedstone;
        }
    }

    private int getMaxRedstone() {
        int maxRedstone = 0;
        for (TileEntityElevatorBase base : multiElevators) {
            BlockPos.Mutable pos1 = base.getBlockPos().mutable();
            while (getLevel().getBlockState(pos1).getBlock() == ModBlocks.ELEVATOR_BASE.get()) {
                maxRedstone = Math.max(maxRedstone, getLevel().getBestNeighborSignal(pos1));
                if (maxRedstone == 15) return 15;
                pos1.move(Direction.DOWN);
            }
        }
        return maxRedstone;
    }

    public float getMaxElevatorHeight() {
        int max = maxFloorHeight;
        if (multiElevators != null) {
            for (TileEntityElevatorBase base : multiElevators) {
                max = Math.min(max, base.maxFloorHeight);
            }
        }
        return max;
    }

    public void updateMaxElevatorHeight() {
        int i = -1;
        do {
            i++;
        } while (getLevel().getBlockState(getBlockPos().offset(0, i + 1, 0)).getBlock() == ModBlocks.ELEVATOR_FRAME.get());
        int elevatorBases = 0;
        do {
            elevatorBases++;
        } while (getLevel().getBlockState(getBlockPos().offset(0, -elevatorBases, 0)).getBlock() == ModBlocks.ELEVATOR_BASE.get());

        maxFloorHeight = Math.min(i, elevatorBases * ConfigHelper.common().machines.elevatorBaseBlocksPerBase.get());
        setChanged();
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        if (tag.contains("extensionD")) {
            extension = tag.getDouble("extensionD");
            targetExtension = tag.getDouble("targetExtensionD");
        } else {
            extension = tag.getFloat("extension");
            targetExtension = tag.getFloat("targetExtension");
        }
        if (!tag.contains("maxFloorHeight")) {//backwards compatibility implementation.
            updateMaxElevatorHeight();
        } else {
            maxFloorHeight = tag.getInt("maxFloorHeight");
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        tag.putDouble("extensionD", extension);
        tag.putDouble("targetExtensionD", targetExtension);
        tag.putInt("maxFloorHeight", maxFloorHeight);
        return tag;
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);

        camoState = ICamouflageableTE.readCamo(tag);
        floorHeights = tag.getIntArray("floorHeights");

        floorNames.clear();
        ListNBT floorNameList = tag.getList("floorNames", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < floorNameList.size(); i++) {
            CompoundNBT floorName = floorNameList.getCompound(i);
            floorNames.put(floorName.getInt("floorHeight"), floorName.getString("floorName"));
        }
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);

        ICamouflageableTE.writeCamo(tag, camoState);
        tag.putIntArray("floorHeights", floorHeights);

        ListNBT floorNameList = new ListNBT();
        floorNames.forEach((height, name) -> {
            CompoundNBT floorNameTag = new CompoundNBT();
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
            Stack<TileEntityElevatorBase> todo = new Stack<>();
            todo.add(this);
            while (!todo.isEmpty()) {
                TileEntityElevatorBase curElevator = todo.pop();
                if (curElevator.isCoreElevator() && !multiElevators.contains(curElevator)) {
                    multiElevators.add(curElevator);
                    curElevator.multiElevators = multiElevators;
                    for (Direction face : DirectionUtil.HORIZONTALS) {
                        TileEntity te = curElevator.getCachedNeighbor(face);
                        if (te instanceof TileEntityElevatorBase && !te.isRemoved()) {
                            todo.push((TileEntityElevatorBase) te);
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
        if (getLevel().getBlockState(getBlockPos().relative(Direction.UP)).getBlock() != ModBlocks.ELEVATOR_BASE.get()) {
            coreElevator = this;
//            int i = -1;
//            TileEntity te = getWorld().getTileEntity(getPos().offset(Direction.DOWN));
//            while (te instanceof TileEntityElevatorBase) {
//                ((TileEntityElevatorBase) te).coreElevator = this;
//                i--;
//                te = getWorld().getTileEntity(getPos().add(0, i, 0));
//            }
        } else {
            coreElevator = null; // force recalc
        }
    }

    /**
     * Elevator above us has just been broken; move its upgrades & camo to this elevator base, if possible.
     */
    public void moveUpgradesFromAbove() {
        TileEntity brokenTE = getLevel().getBlockEntity(getBlockPos().relative(Direction.UP));
        if (brokenTE instanceof TileEntityElevatorBase) {
            camoState = ((TileEntityElevatorBase) brokenTE).camoState;
            sendDescriptionPacket();

            for (int i = 0; i < getUpgradeHandler().getSlots(); i++) {
                ItemStack stack = ((TileEntityElevatorBase) brokenTE).getUpgradeHandler().getStackInSlot(i);
                ItemStack excess = ItemHandlerHelper.insertItem(getUpgradeHandler(), stack, false);
                if (!excess.isEmpty()) PneumaticCraftUtils.dropItemOnGround(excess, level, getBlockPos());
                ((TileEntityElevatorBase) brokenTE).getUpgradeHandler().setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public void updateFloors(boolean notifyClient) {
        if (level.getGameTime() - lastFloorUpdate > 20) {
            callerList.clear();
            floorList.clear();
            if (multiElevators != null) {  // should always be the case, even if only a single member
                int yOffset = 0;
                int worldHeight = level.dimensionType().logicalHeight();
                BlockPos.Mutable mut = new BlockPos.Mutable();
                scanLoop: while (worldPosition.getY() + yOffset < worldHeight - 2) {
                    boolean registeredThisFloor = false;
                    for (TileEntityElevatorBase base : multiElevators) {
                        for (Direction dir : DirectionUtil.HORIZONTALS) {
                            mut.set(base.getBlockPos());
                            mut.move(dir.getStepX(), yOffset + 2, dir.getStepZ());
                            if (base.level.getBlockState(mut).getBlock() == ModBlocks.ELEVATOR_CALLER.get()) {
                                callerList.add(mut.immutable());
                                if (!registeredThisFloor) floorList.add(yOffset);
                                registeredThisFloor = true;
                            }
                        }
                    }
                    yOffset++;
                    for (TileEntityElevatorBase base : multiElevators) {
                        if (base.level.getBlockState(base.getBlockPos().above(yOffset)).getBlock() != ModBlocks.ELEVATOR_FRAME.get()) {
                            break scanLoop;
                        }
                    }
                }

                for (TileEntityElevatorBase base : multiElevators) {
                    base.floorHeights = floorList.stream().mapToInt(Integer::intValue).toArray();
                }
            }
            lastFloorUpdate = level.getGameTime();
        }

        ElevatorButton[] elevatorButtons = new ElevatorButton[floorHeights.length];
        int columns = (elevatorButtons.length - 1) / 12 + 1;
        for (int j = 0; j < columns; j++) {
            for (int i = j * 12; i < floorHeights.length && i < j * 12 + 12; i++) {
                elevatorButtons[i] = new ElevatorButton(0.2F + 0.6F / columns * j, 0.5F + (Math.min(floorHeights.length, 12) - 2) * (BUTTON_SPACING + BUTTON_HEIGHT) / 2 - i % 12 * (BUTTON_HEIGHT + BUTTON_SPACING), 0.58F / columns, BUTTON_HEIGHT, i, floorHeights[i]);
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
            for (TileEntityElevatorBase base : multiElevators) {
                base.floorNames = new HashMap<>(floorNames);
            }
        }

        for (BlockPos p : callerList) {
            TileEntity te = getLevel().getBlockEntity(p);
            if (te instanceof TileEntityElevatorCaller) {
                TileEntityElevatorCaller caller = (TileEntityElevatorCaller) te;
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
            for (TileEntityElevatorBase base : multiElevators) {
                base.targetExtension = height;
            }
        }
    }

    public double getTargetExtension() {
        return targetExtension;
    }

    private void sendDescPacketFromAllElevators() {
        if (multiElevators != null) {
            for (TileEntityElevatorBase base : multiElevators) {
                base.sendDescriptionPacket();
            }
        } else {
            sendDescriptionPacket();
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), getBlockPos().getX() + 1, getBlockPos().getY() + 1 + extension, getBlockPos().getZ() + 1);
    }

    @Override
    public double getViewDistance() {
        return 65536D;
    }

    private TileEntityElevatorBase getCoreElevator() {
        if (coreElevator == null || (getLevel().isClientSide && (getLevel().getGameTime() & 0x3f) == 0)) {
            // bit of a hack; force a recalc every 64 ticks on the client
            coreElevator = BlockElevatorBase.getCoreTileEntity(getLevel(), getBlockPos());
        }
        return coreElevator;
    }

    public boolean isCoreElevator() {
        return getCoreElevator() == this;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        // only connect to other elevator bases on the UP face
        return side != Direction.UP || level.getBlockState(worldPosition.relative(side)).getBlock() == ModBlocks.ELEVATOR_BASE.get();
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
    public boolean isGuiUseableByPlayer(PlayerEntity par1EntityPlayer) {
        return getLevel().getBlockEntity(getBlockPos()) == this;
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
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Override
    public RedstoneController<TileEntityElevatorBase> getRedstoneController() {
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
        ICamouflageableTE.syncToClient(this);
    }

    @Override
    public IFormattableTextComponent getRedstoneTabTitle() {
        return xlate("pneumaticcraft.gui.tab.redstoneBehaviour.elevator.controlBy");
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerElevator(i, playerInventory, getBlockPos());
    }
}
