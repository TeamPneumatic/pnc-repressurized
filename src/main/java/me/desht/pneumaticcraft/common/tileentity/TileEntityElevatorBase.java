package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.common.block.BlockElevatorBase;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerElevator;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethod;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethodRegistry;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class TileEntityElevatorBase extends TileEntityPneumaticBase
        implements IGUITextFieldSensitive, IRedstoneControlled, IMinWorkingPressure, IAirListener, ICamouflageableTE, INamedContainerProvider {

    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.elevator.button.redstone",
            "gui.tab.redstoneBehaviour.elevator.button.elevatorCallers"
    );

    public float oldExtension;
    @DescSynced
    @LazySynced
    public float extension;
    @DescSynced
    private float targetExtension;
    private boolean isStopped = true;  //used for sounds
    private TileEntityElevatorBase coreElevator;
    private List<TileEntityElevatorBase> multiElevators; //initialized when multiple elevators are connected in a multiblock manner.
    @DescSynced
    public int multiElevatorCount;
    @GuiSynced
    private int redstoneMode;
    public int[] floorHeights = new int[0]; //list of every floor of Elevator Callers.
    private HashMap<Integer, String> floorNames = new HashMap<>();
    @GuiSynced
    private int maxFloorHeight;
    private int redstoneInputLevel; // current redstone input level
    @DescSynced
    private ItemStack camoStack = ItemStack.EMPTY;
    private BlockState camoState;

    public TileEntityElevatorBase() {
        super(ModTileEntities.ELEVATOR_BASE.get(), PneumaticValues.DANGER_PRESSURE_ELEVATOR, PneumaticValues.MAX_PRESSURE_ELEVATOR, PneumaticValues.VOLUME_ELEVATOR, 4);
    }

    @Override
    public void tick() {
        oldExtension = extension;
        if (getWorld().isRemote && (getWorld().getGameTime() & 0x3f) == 0)
            coreElevator = null;//reset this because the client doesn't get notified of neighbor block updates.
        if (isCoreElevator()) {
            super.tick();
            if (!getWorld().isRemote && isControlledByRedstone()) {
                float oldTargetExtension = targetExtension;
                float maxExtension = getMaxElevatorHeight();

                int redstoneInput = redstoneInputLevel;
                if (multiElevators != null) {
                    for (TileEntityElevatorBase base : multiElevators) {
                        redstoneInput = Math.max(redstoneInputLevel, base.redstoneInputLevel);
                    }
                }

                targetExtension = redstoneInput * maxExtension / 15;
                if (targetExtension > oldExtension && getPressure() < PneumaticValues.MIN_PRESSURE_ELEVATOR)
                    targetExtension = oldExtension; // only ascent when there's enough pressure
                if (oldTargetExtension != targetExtension) sendDescPacketFromAllElevators();
            }
            float speedMultiplier = getSpeedMultiplierFromUpgrades();
            if (getWorld().isRemote) {
                speedMultiplier = (float) (speedMultiplier * PacketServerTickTime.tickTimeMultiplier);
            }

            SoundEvent soundName = null;
            if (extension < targetExtension) {
                if (!getWorld().isRemote && getPressure() < PneumaticValues.MIN_PRESSURE_ELEVATOR) {
                    targetExtension = extension;
                    sendDescriptionPacket(256D);
                }

                float moveBy;
                if (extension < targetExtension - TileEntityConstants.ELEVATOR_SLOW_EXTENSION) {
                    moveBy = TileEntityConstants.ELEVATOR_SPEED_FAST * speedMultiplier;
                } else {
                    moveBy = TileEntityConstants.ELEVATOR_SPEED_SLOW * speedMultiplier;
                }
                if (extension + moveBy > targetExtension) {
                    extension = targetExtension;
                    if (!getWorld().isRemote) updateFloors();
                }
                if (isStopped) {
                    soundName = ModSounds.ELEVATOR_RISING_START.get();
                    isStopped = false;
                    if (!world.isRemote) {
                        PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 1024, world.getDimension().getType());
                        NetworkHandler.sendToAllAround(new PacketPlayMovingSound(MovingSounds.Sound.ELEVATOR, getCoreElevator()), tp);
                    }
                }
                float startingExtension = extension;

                while (extension < startingExtension + moveBy) {
                    extension += TileEntityConstants.ELEVATOR_SPEED_SLOW;
                    /*
                    if(extension > startingExtension + moveBy) {
                        extension = startingExtension + moveBy;
                    }
                    */
                    // moveEntities(TileEntityConstants.ELEVATOR_SPEED_SLOW);
                }
                addAir((int) ((oldExtension - extension) * PneumaticValues.USAGE_ELEVATOR * (getSpeedUsageMultiplierFromUpgrades() / speedMultiplier)));// substract the ascended distance from the air reservoir.
            }
            if (extension > targetExtension) {
                float chargingSlowdown = 1.0f - Math.min(4, getUpgrades(EnumUpgrade.CHARGING)) * 0.1f;
                if (extension > targetExtension + TileEntityConstants.ELEVATOR_SLOW_EXTENSION) {
                    extension -= TileEntityConstants.ELEVATOR_SPEED_FAST * speedMultiplier * chargingSlowdown;
                } else {
                    extension -= TileEntityConstants.ELEVATOR_SPEED_SLOW * speedMultiplier * chargingSlowdown;
                }
                if (extension < targetExtension) {
                    extension = targetExtension;
                    if (!getWorld().isRemote) updateFloors();
                }
                if (isStopped) {
                    soundName = ModSounds.ELEVATOR_RISING_START.get();
                    isStopped = false;
                    if (!world.isRemote) {
                        PacketDistributor.TargetPoint tp = new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 1024, world.getDimension().getType());
                        NetworkHandler.sendToAllAround(new PacketPlayMovingSound(MovingSounds.Sound.ELEVATOR, getCoreElevator()), tp);
                    }
                }
                if (getUpgrades(EnumUpgrade.CHARGING) > 0) {
                    float mul = 0.15f * Math.min(4, getUpgrades(EnumUpgrade.CHARGING));
                    addAir((int) ((oldExtension - extension) * PneumaticValues.USAGE_ELEVATOR * mul * (getSpeedUsageMultiplierFromUpgrades() / speedMultiplier)));
                }
                //  movePlayerDown();
            }
            if (oldExtension == extension && !isStopped) {
                soundName = ModSounds.ELEVATOR_RISING_STOP.get();
                isStopped = true;
            }

            if (soundName != null && getWorld().isRemote) {
                getWorld().playSound(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, soundName, SoundCategory.BLOCKS, 0.5F, 1.0F, true);
            }

        } else {
            extension = 0;
        }
        if (!getWorld().isRemote && oldExtension != extension) {
            sendDescriptionPacket(256);
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 1) redstoneMode = 0;

            if (multiElevators != null) {
                for (TileEntityElevatorBase base : multiElevators) {
                    while (base.redstoneMode != redstoneMode) {
                        base.handleGUIButtonPress(tag, player);
                    }
                }
            }

            int i = -1;
            TileEntity te = getWorld().getTileEntity(getPos().offset(Direction.DOWN));
            while (te instanceof TileEntityElevatorBase) {
                ((TileEntityElevatorBase) te).redstoneMode = redstoneMode;
                i--;
                te = getWorld().getTileEntity(getPos().add(0, i, 0));
            }
        }
    }

    private boolean isControlledByRedstone() {
        return redstoneMode == 0;
    }

    @Override
    public boolean redstoneAllows() {
        return true;
    }

    private void updateRedstoneInputLevel() {
        if (multiElevators == null) return;

        int maxRedstone = 0;
        for (TileEntityElevatorBase base : multiElevators) {
            int i = 0;
            while (getWorld().getBlockState(base.getPos().add(0, i, 0)).getBlock() == ModBlocks.ELEVATOR_BASE.get()) {
                maxRedstone = Math.max(maxRedstone, PneumaticCraftUtils.getRedstoneLevel(getWorld(), base.getPos().add(0, i, 0)));
                i--;
            }
        }
        for (TileEntityElevatorBase base : multiElevators) {
            base.redstoneInputLevel = maxRedstone;
        }
    }

    public float getMaxElevatorHeight() {
        int max = maxFloorHeight;
        if (multiElevators != null) {
            for (TileEntityElevatorBase base : multiElevators) {
                max = Math.max(max, base.maxFloorHeight);
            }
        }
        return max;
    }

    public void updateMaxElevatorHeight() {
        int i = -1;
        do {
            i++;
        } while (getWorld().getBlockState(getPos().add(0, i + 1, 0)).getBlock() == ModBlocks.ELEVATOR_FRAME.get());
        int elevatorBases = 0;
        do {
            elevatorBases++;
        } while (getWorld().getBlockState(getPos().add(0, -elevatorBases, 0)).getBlock() == ModBlocks.ELEVATOR_BASE.get());

        maxFloorHeight = Math.min(i, elevatorBases * PNCConfig.Common.Machines.elevatorBaseBlocksPerBase);
    }

    // NBT methods-----------------------------------------------
    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        extension = tag.getFloat("extension");
        targetExtension = tag.getFloat("targetExtension");
        redstoneMode = tag.getInt("redstoneMode");
        if (!tag.contains("maxFloorHeight")) {//backwards compatibility implementation.
            updateMaxElevatorHeight();
        } else {
            maxFloorHeight = tag.getInt("maxFloorHeight");
        }
        camoStack = ICamouflageableTE.readCamoStackFromNBT(tag);
        camoState = ICamouflageableTE.getStateForStack(camoStack);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putFloat("extension", extension);
        tag.putFloat("targetExtension", targetExtension);
        tag.putInt("redstoneMode", redstoneMode);
        tag.putInt("maxFloorHeight", maxFloorHeight);
        ICamouflageableTE.writeCamoStackToNBT(camoStack, tag);
        return tag;
    }

    @Override
    public void readFromPacket(CompoundNBT tag) {
        super.readFromPacket(tag);
        floorHeights = tag.getIntArray("floorHeights");

        floorNames.clear();
        ListNBT floorNameList = tag.getList("floorNames", 10);
        for (int i = 0; i < floorNameList.size(); i++) {
            CompoundNBT floorName = floorNameList.getCompound(i);
            floorNames.put(floorName.getInt("floorHeight"), floorName.getString("floorName"));
        }
    }

    @Override
    public void writeToPacket(CompoundNBT tag) {
        super.writeToPacket(tag);
        tag.putIntArray("floorHeights", floorHeights);

        ListNBT floorNameList = new ListNBT();
        for (int key : floorNames.keySet()) {
            CompoundNBT floorNameTag = new CompoundNBT();
            floorNameTag.putInt("floorHeight", key);
            floorNameTag.putString("floorName", floorNames.get(key));
            floorNameList.add(floorNameTag);
        }
        tag.put("floorNames", floorNameList);
    }

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        updateConnections();
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
                    for (Direction face : PneumaticCraftUtils.HORIZONTALS) {
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
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        getCoreElevator().updateRedstoneInputLevel();
        connectAsMultiblock();
        updateConnections();
    }

    @Override
    public void onDescUpdate() {
        camoState = ICamouflageableTE.getStateForStack(camoStack);

        super.onDescUpdate();
    }

    private void updateConnections() {
        if (getWorld().getBlockState(getPos().offset(Direction.UP)).getBlock() != ModBlocks.ELEVATOR_BASE.get()) {
            coreElevator = this;
            int i = -1;
            TileEntity te = getWorld().getTileEntity(getPos().offset(Direction.DOWN));
            while (te instanceof TileEntityElevatorBase) {
                ((TileEntityElevatorBase) te).coreElevator = this;
                i--;
                te = getWorld().getTileEntity(getPos().add(0, i, 0));
            }
        }
    }

    /**
     * Elevator above us has just been broken; move its upgrades & camo to this elevator base, if possible.
     */
    public void moveUpgradesFromAbove() {
        TileEntity brokenTE = getWorld().getTileEntity(getPos().offset(Direction.UP));
        if (brokenTE instanceof TileEntityElevatorBase) {
            camoStack = ((TileEntityElevatorBase) brokenTE).camoStack;
            sendDescriptionPacket();

            for (int i = 0; i < getUpgradeHandler().getSlots(); i++) {
                ItemStack stack = ((TileEntityElevatorBase) brokenTE).getUpgradeHandler().getStackInSlot(i);
                ItemStack excess = ItemHandlerHelper.insertItem(getUpgradeHandler(), stack, false);
                if (!excess.isEmpty()) PneumaticCraftUtils.dropItemOnGround(excess, world, getPos());
                ((TileEntityElevatorBase) brokenTE).getUpgradeHandler().setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public void updateFloors() {
        List<Integer> floorList = new ArrayList<>();
        List<BlockPos> callerList = new ArrayList<>();

        if (multiElevators != null) {
            int yOffset = 0;
            boolean shouldBreak = false;
            while (!shouldBreak) {
                boolean registeredThisFloor = false;
                for (TileEntityElevatorBase base : multiElevators) {
                    for (Direction dir : PneumaticCraftUtils.HORIZONTALS) {
                        BlockPos checkPos = base.getPos().offset(dir).up(yOffset + 2);
                        if (base.world.getBlockState(checkPos).getBlock() == ModBlocks.ELEVATOR_CALLER.get()) {
                            callerList.add(checkPos);
                            if (!registeredThisFloor) floorList.add(yOffset);
                            registeredThisFloor = true;
                        }
                    }
                }

                yOffset++;
                for (TileEntityElevatorBase base : multiElevators) {
                    if (base.world.getBlockState(base.getPos().up(yOffset)).getBlock() != ModBlocks.ELEVATOR_FRAME.get()) {
                        shouldBreak = true;
                        break;
                    }
                }
            }

            for (TileEntityElevatorBase base : multiElevators) {
                base.floorHeights = floorList.stream().mapToInt(Integer::intValue).toArray();
            }
        }

        double buttonHeight = 0.06D;
        double buttonSpacing = 0.02D;
        TileEntityElevatorCaller.ElevatorButton[] elevatorButtons = new TileEntityElevatorCaller.ElevatorButton[floorHeights.length];
        int columns = (elevatorButtons.length - 1) / 12 + 1;
        for (int j = 0; j < columns; j++) {
            for (int i = j * 12; i < floorHeights.length && i < j * 12 + 12; i++) {
                elevatorButtons[i] = new TileEntityElevatorCaller.ElevatorButton(0.2D + 0.6D / columns * j, 0.5D + (Math.min(floorHeights.length, 12) - 2) * (buttonSpacing + buttonHeight) / 2 - i % 12 * (buttonHeight + buttonSpacing), 0.58D / columns, buttonHeight, i, floorHeights[i]);
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
            TileEntity te = getWorld().getTileEntity(p);
            if (te instanceof TileEntityElevatorCaller) {
                int callerFloorHeight = p.getY() - getPos().getY() - 2;
                int callerFloor = -1;
                for (TileEntityElevatorCaller.ElevatorButton floor : elevatorButtons) {
                    if (floor.floorHeight == callerFloorHeight) {
                        callerFloor = floor.floorNumber;
                        break;
                    }
                }
                if (callerFloor == -1) {
                    Log.error("Error while updating elevator floors! This will cause a indexOutOfBoundsException, index = -1");
                }
                ((TileEntityElevatorCaller) te).setEmittingRedstone(PneumaticCraftUtils.areFloatsEqual(targetExtension, extension, 0.1F) && PneumaticCraftUtils.areFloatsEqual(extension, callerFloorHeight, 0.1F));
                ((TileEntityElevatorCaller) te).setFloors(elevatorButtons, callerFloor);
            }
        }
    }

    public void goToFloor(int floor) {
        if (getCoreElevator().isControlledByRedstone()) {
            getCoreElevator().handleGUIButtonPress(IGUIButtonSensitive.REDSTONE_TAG, null);
        }
        if (floor >= 0 && floor < floorHeights.length) {
            setTargetHeight(floorHeights[floor]);
        }
        updateFloors();
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

    public float getTargetExtension() {
        return targetExtension;
    }

    private void sendDescPacketFromAllElevators() {
        if (multiElevators != null) {
            for (TileEntityElevatorBase base : multiElevators) {
                base.sendDescriptionPacket(256);
            }
        } else {
            sendDescriptionPacket(256);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1 + extension, getPos().getZ() + 1);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 65536D;
    }

    private TileEntityElevatorBase getCoreElevator() {
        if (coreElevator == null) {
            coreElevator = BlockElevatorBase.getCoreTileEntity(getWorld(), getPos());
        }
        return coreElevator;
    }

    public boolean isCoreElevator() {
        return getCoreElevator() == this;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != Direction.UP && side != Direction.DOWN
                || getWorld().getBlockState(getPos().offset(side)).getBlock() != ModBlocks.ELEVATOR_BASE.get();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY && !isCoreElevator()) {
            return getCoreElevator().getCapability(cap, side);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public List<IAirHandlerMachine> addConnectedPneumatics(List<IAirHandlerMachine> airHandlers) {
        TileEntity te = getTileCache()[Direction.DOWN.ordinal()].getTileEntity();
        if (te instanceof TileEntityElevatorBase) {
            TileEntityElevatorBase te1 = (TileEntityElevatorBase) te;
            List<IAirHandlerMachine.Connection> l = te1.airHandler.getConnectedAirHandlers(te1);
            airHandlers.addAll(l.stream().map(IAirHandlerMachine.Connection::getAirHandler).collect(Collectors.toList()));
        }
        return airHandlers;
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
            updateFloors();
        }
    }

    @Override
    public boolean isGuiUseableByPlayer(PlayerEntity par1EntityPlayer) {
        return getWorld().getTileEntity(getPos()) == this;
    }

    /*
     * COMPUTERCRAFT API
     */

//    @Override
//    public String getType() {
//        return "elevator";
//    }

    @Override
    protected void addLuaMethods(LuaMethodRegistry registry) {
        super.addLuaMethods(registry);

        registry.registerLuaMethod(new LuaMethod("setHeight") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "height (in blocks)");
                setTargetHeight(((Double) args[0]).floatValue());
                if (getCoreElevator().isControlledByRedstone()) {
                    getCoreElevator().handleGUIButtonPress(IGUIButtonSensitive.REDSTONE_TAG, null);
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

        registry.registerLuaMethod(new LuaMethod("setExternalControl") {
            @Override
            public Object[] call(Object[] args) {
                requireArgs(args, 1, "true/false");
                if ((Boolean) args[0] && getCoreElevator().isControlledByRedstone()
                        || !(Boolean) args[0] && !getCoreElevator().isControlledByRedstone()) {
                    getCoreElevator().handleGUIButtonPress(IGUIButtonSensitive.REDSTONE_TAG, null);
                }
                return null;
            }
        });
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
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
        camoStack = ICamouflageableTE.getStackForState(state);
        sendDescriptionPacket();
        markDirty();
    }

    @Override
    public String getRedstoneTabTitle() {
        return "gui.tab.redstoneBehaviour.elevator.controlBy";
    }

    @Override
    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerElevator(i, playerInventory, getPos());
    }
}
