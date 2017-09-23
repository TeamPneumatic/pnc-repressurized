package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirListener;
import me.desht.pneumaticcraft.common.block.BlockElevatorBase;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.network.PacketServerTickTime;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaConstant;
import me.desht.pneumaticcraft.common.thirdparty.computercraft.LuaMethod;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class TileEntityElevatorBase extends TileEntityPneumaticBase
        implements IGUITextFieldSensitive, IRedstoneControlled, IMinWorkingPressure, IAirListener {
    private static final int INVENTORY_SIZE = 2;

    @DescSynced
    public boolean[] sidesConnected = new boolean[6];
    public float oldExtension;
    @DescSynced
    @LazySynced
    public float extension;
    @DescSynced
    public float targetExtension;
    private int soundCounter;
    private boolean isStopped; //used for sounds
    private TileEntityElevatorBase coreElevator;
    private List<TileEntityElevatorBase> multiElevators;//initialized when multiple elevators are connected in a multiblock manner.
    @GuiSynced
    public int redstoneMode;
    public int[] floorHeights = new int[0];//list of every floor of Elevator Callers.
    private HashMap<Integer, String> floorNames = new HashMap<Integer, String>();
    @GuiSynced
    private int maxFloorHeight;
    private int redstoneInputLevel;//current redstone input level
    @DescSynced
    private ItemStackHandler inventory = new FilteredItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean test(Integer integer, ItemStack itemStack) {
            return itemStack.getItem() instanceof ItemBlock;
        }
    };
    private Block baseCamo, frameCamo;

    public TileEntityElevatorBase() {
        super(PneumaticValues.DANGER_PRESSURE_ELEVATOR, PneumaticValues.MAX_PRESSURE_ELEVATOR, PneumaticValues.VOLUME_ELEVATOR, 4);
        addApplicableUpgrade(EnumUpgrade.SPEED);
    }

    @Override
    public void update() {
        oldExtension = extension;
        if (getWorld().isRemote && getWorld().getTotalWorldTime() % 60 == 0)
            coreElevator = null;//reset this because the client doesn't get notified of neighbor block updates.
        if (isCoreElevator()) {
            super.update();
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
                    sendDescPacket(256D);
                }
                soundName = Sounds.ELEVATOR_MOVING;

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
                    soundName = Sounds.ELEVATOR_START;
                    isStopped = false;
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
                soundName = Sounds.ELEVATOR_MOVING;
                if (extension > targetExtension + TileEntityConstants.ELEVATOR_SLOW_EXTENSION) {
                    extension -= TileEntityConstants.ELEVATOR_SPEED_FAST * speedMultiplier;
                } else {
                    extension -= TileEntityConstants.ELEVATOR_SPEED_SLOW * speedMultiplier;
                }
                if (extension < targetExtension) {
                    extension = targetExtension;
                    if (!getWorld().isRemote) updateFloors();
                }
                if (isStopped) {
                    soundName = Sounds.ELEVATOR_START;
                    isStopped = false;
                }
                //  movePlayerDown();
            }
            if (oldExtension == extension && !isStopped) {
                soundName = Sounds.ELEVATOR_STOP;
                isStopped = true;
                soundCounter = 0;
            }

            if (soundCounter > 0) soundCounter--;
            if (soundName != null && getWorld().isRemote && soundCounter == 0) {
                getWorld().playSound(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, soundName, SoundCategory.BLOCKS, 0.1F, 1.0F, true);
                soundCounter = 10;
            }

        } else {
            extension = 0;
        }
        if (!getWorld().isRemote && oldExtension != extension) {
            sendDescPacket(256);
        }
    }

    private void movePlayerDown() {
        if (!getWorld().isRemote) return;

        AxisAlignedBB aabb = new AxisAlignedBB(getPos().getX(), getPos().getY() + 1, getPos().getZ(), getPos().getX() + 1, getPos().getY() + oldExtension + 1.05F, getPos().getZ() + 1);
        List<Entity> entityList = getWorld().getEntitiesWithinAABBExcludingEntity(null, aabb);
        for (Entity entity : entityList) {
            if (entity instanceof EntityPlayer) {
                //   moveEntityToCenter(entity);
                double posX = entity.posX;
                double posZ = entity.posZ;
                if (posX >= getPos().getX() && posX < getPos().getX() + 1 && posZ >= getPos().getZ() && posZ < getPos().getZ() + 1) {
                    entity.motionX *= 0.6;
                    entity.motionZ *= 0.6;
                    entity.move(MoverType.SELF, 0, extension - oldExtension + 0.001F, 0);
                }
            }
        }
    }

    private void moveEntities(float moveBy) {
        AxisAlignedBB aabb = new AxisAlignedBB(getPos().getX(), getPos().getY() + 1, getPos().getZ(), getPos().getX() + 1, getPos().getY() + extension + 1, getPos().getZ() + 1);
        List<Entity> entityList = getWorld().getEntitiesWithinAABBExcludingEntity(null, aabb);
        for (Entity entity : entityList) {
            if (entity instanceof EntityPlayer) {

            } else entity.move(MoverType.SELF, 0, moveBy + 0.05F, 0);
        }
    }

    private void moveEntityToCenter(Entity entity) {
        /*
         * this is for your own protection. you may hurt yourself if you're not standing right, especially
         * on multiblock-elevators (entity will be found by multiple bases, causing problems)
         */
        entity.setPosition(getPos().getX() + 0.5F, entity.posY, getPos().getZ() + 0.5F);
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 1) redstoneMode = 0;

            if (multiElevators != null) {
                for (TileEntityElevatorBase base : multiElevators) {
                    while (base.redstoneMode != redstoneMode) {
                        base.handleGUIButtonPress(buttonID, player);
                    }
                }
            }

            int i = -1;
            TileEntity te = getWorld().getTileEntity(getPos().offset(EnumFacing.DOWN));
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

    public void updateRedstoneInputLevel() {
        if (multiElevators == null) return;

        int maxRedstone = 0;
        for (TileEntityElevatorBase base : multiElevators) {
            int i = 0;
            while (getWorld().getBlockState(base.getPos().add(0, i, 0)).getBlock() == Blockss.ELEVATOR_BASE) {
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
        } while (getWorld().getBlockState(getPos().add(0, i + 1, 0)).getBlock() == Blockss.ELEVATOR_FRAME);
        int elevatorBases = 0;
        do {
            elevatorBases++;
        } while (getWorld().getBlockState(getPos().add(0, -elevatorBases, 0)).getBlock() == Blockss.ELEVATOR_BASE);

        maxFloorHeight = Math.min(i, elevatorBases * ConfigHandler.machineProperties.elevatorBaseBlocksPerBase);
    }

    // NBT methods-----------------------------------------------
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        extension = tag.getFloat("extension");
        targetExtension = tag.getFloat("targetExtension");
        redstoneMode = tag.getInteger("redstoneMode");
        if (!tag.hasKey("maxFloorHeight")) {//backwards compatibility implementation.
            updateMaxElevatorHeight();
        } else {
            maxFloorHeight = tag.getInteger("maxFloorHeight");
        }
        for (int i = 0; i < 6; i++) {
            sidesConnected[i] = tag.getBoolean("sideConnected" + i);
        }
        inventory = new ItemStackHandler(INVENTORY_SIZE);
        inventory.deserializeNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setFloat("extension", extension);
        tag.setFloat("targetExtension", targetExtension);
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setInteger("maxFloorHeight", maxFloorHeight);
        for (int i = 0; i < 6; i++) {
            tag.setBoolean("sideConnected" + i, sidesConnected[i]);
        }
        tag.setTag("Items", inventory.serializeNBT());
        return tag;
    }

    @Override
    public void readFromPacket(NBTTagCompound tag) {
        super.readFromPacket(tag);
        floorHeights = tag.getIntArray("floorHeights");

        floorNames.clear();
        NBTTagList floorNameList = tag.getTagList("floorNames", 10);
        for (int i = 0; i < floorNameList.tagCount(); i++) {
            NBTTagCompound floorName = floorNameList.getCompoundTagAt(i);
            floorNames.put(floorName.getInteger("floorHeight"), floorName.getString("floorName"));
        }
    }

    @Override
    public void writeToPacket(NBTTagCompound tag) {
        super.writeToPacket(tag);
        tag.setIntArray("floorHeights", floorHeights);

        NBTTagList floorNameList = new NBTTagList();
        for (int key : floorNames.keySet()) {
            NBTTagCompound floorNameTag = new NBTTagCompound();
            floorNameTag.setInteger("floorHeight", key);
            floorNameTag.setString("floorName", floorNames.get(key));
            floorNameList.appendTag(floorNameTag);
        }
        tag.setTag("floorNames", floorNameList);
    }

    @Override
    public void onNeighborTileUpdate() {
        super.onNeighborTileUpdate();
        updateConnections();
        connectAsMultiblock();
    }

    private void connectAsMultiblock() {
        multiElevators = null;
        if (isCoreElevator()) {
            multiElevators = new ArrayList<>();
            Stack<TileEntityElevatorBase> todo = new Stack<>();
            todo.add(this);
            while (!todo.isEmpty()) {
                TileEntityElevatorBase curElevator = todo.pop();
                if (curElevator.isCoreElevator()) {
                    multiElevators.add(curElevator);
                    curElevator.multiElevators = multiElevators;
                    for (int i = 2; i < 6; i++) {
                        TileEntity te = curElevator.getTileCache()[i].getTileEntity();
                        if (!multiElevators.contains(te) && te instanceof TileEntityElevatorBase) {
                            todo.push((TileEntityElevatorBase) te);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        getCoreElevator().updateRedstoneInputLevel();
    }

    public void updateConnections() {
        List<Pair<EnumFacing, IAirHandler>> connections = getAirHandler(null).getConnectedPneumatics();
        Arrays.fill(sidesConnected, false);
        for (Pair<EnumFacing, IAirHandler> entry : connections) {
            sidesConnected[entry.getKey().ordinal()] = true;
        }

        if (getWorld().getBlockState(getPos().offset(EnumFacing.UP)) != Blockss.ELEVATOR_BASE) {
            coreElevator = this;
            int i = -1;
            TileEntity te = getWorld().getTileEntity(getPos().offset(EnumFacing.DOWN));
            while (te instanceof TileEntityElevatorBase) {
                ((TileEntityElevatorBase) te).coreElevator = this;
                i--;
                te = getWorld().getTileEntity(getPos().add(0, i, 0));
            }
        }
    }

    public void moveInventoryToThis() {
        TileEntity te = getWorld().getTileEntity(getPos().offset(EnumFacing.UP));
        if (te instanceof TileEntityElevatorBase) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                inventory.setStackInSlot(i, ((TileEntityElevatorBase) te).inventory.getStackInSlot(i));
                ((TileEntityElevatorBase) te).inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public void updateFloors() {
        List<Integer> floorList = new ArrayList<>();
        List<BlockPos> callerList = new ArrayList<>();

        if (multiElevators != null) {
            int i = 0;
            boolean shouldBreak = false;
            while (!shouldBreak) {
                boolean registeredThisFloor = false;
                for (TileEntityElevatorBase base : multiElevators) {
                    for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                        if (base.world.getBlockState(base.getPos().offset(dir).add(0, i + 2, 0)).getBlock() == Blockss.ELEVATOR_CALLER) {
                            callerList.add(new BlockPos(base.getPos().getX() + dir.getFrontOffsetX(), base.getPos().getY() + i + 2, base.getPos().getZ() + dir.getFrontOffsetZ()));
                            if (!registeredThisFloor) floorList.add(i);
                            registeredThisFloor = true;
                        }
                    }
                }

                i++;
                for (TileEntityElevatorBase base : multiElevators) {
                    if (base.world.getBlockState(base.getPos().add(0, i, 0)).getBlock() != Blockss.ELEVATOR_FRAME) {
                        shouldBreak = true;
                        break;
                    }
                }
            }

            for (TileEntityElevatorBase base : multiElevators) {
                base.floorHeights = new int[floorList.size()];
                for (i = 0; i < base.floorHeights.length; i++) {
                    base.floorHeights[i] = floorList.get(i);
                }
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
        if (getCoreElevator().isControlledByRedstone()) getCoreElevator().handleGUIButtonPress(0, null);
        if (floor >= 0 && floor < floorHeights.length) setTargetHeight(floorHeights[floor]);
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

    @Override
    public void onDescUpdate() {
        baseCamo = inventory.getStackInSlot(0).getItem() instanceof ItemBlock ? ((ItemBlock) inventory.getStackInSlot(0).getItem()).getBlock() : null;
        Block newFrameCamo = inventory.getStackInSlot(1).getItem() instanceof ItemBlock ? ((ItemBlock) inventory.getStackInSlot(1).getItem()).getBlock() : null;

        if (newFrameCamo != frameCamo) {
            frameCamo = newFrameCamo;
            rerenderChunk();
        }
    }

    private void sendDescPacketFromAllElevators() {
        if (multiElevators != null) {
            for (TileEntityElevatorBase base : multiElevators) {
                base.sendDescPacket(256);
            }
        } else {
            sendDescPacket(256);
        }
    }

    @Override
    public String getName() {
        return Blockss.ELEVATOR_BASE.getUnlocalizedName();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 1 + extension, getPos().getZ() + 1);
    }

    @Override
    protected double getPacketDistance() {
        return 256;
    }

    @Override
    @SideOnly(Side.CLIENT)
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
    public boolean isConnectedTo(EnumFacing side) {
        return side != EnumFacing.UP && side != EnumFacing.DOWN || getWorld().getBlockState(getPos().offset(side)).getBlock() != Blockss.ELEVATOR_BASE;
    }

    @Override
    public IAirHandler getAirHandler(EnumFacing sideRequested) {
        if (isCoreElevator()) {
            return super.getAirHandler(sideRequested);
        } else {
            return getCoreElevator().getAirHandler(sideRequested);
        }
    }

    @Override
    public void addConnectedPneumatics(List<Pair<EnumFacing, IAirHandler>> connectedMachines) {
        TileEntity te = getTileCache()[EnumFacing.DOWN.ordinal()].getTileEntity();
        if (te instanceof TileEntityElevatorBase) {
            connectedMachines.addAll(((TileEntityElevatorBase) te).airHandler.getConnectedPneumatics());
        }
    }

    @Override
    public void onAirDispersion(IAirHandler handler, EnumFacing dir, int airAdded) {
    }

    @Override
    public int getMaxDispersion(IAirHandler handler, EnumFacing dir) {
        return Integer.MAX_VALUE;
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
    public boolean isGuiUseableByPlayer(EntityPlayer par1EntityPlayer) {
        return getWorld().getTileEntity(getPos()) == this;
    }

    /*
     * COMPUTERCRAFT API
     */

    @Override
    public String getType() {
        return "elevator";
    }

    @Override
    protected void addLuaMethods() {
        super.addLuaMethods();
        luaMethods.add(new LuaConstant("getMinWorkingPressure", PneumaticValues.MIN_PRESSURE_ELEVATOR));
        luaMethods.add(new LuaMethod("setHeight") {
            @Override
            public Object[] call(Object[] args) throws Exception {
                if (args.length == 1) {
                    setTargetHeight(((Double) args[0]).floatValue());
                    if (getCoreElevator().isControlledByRedstone()) getCoreElevator().handleGUIButtonPress(0, null);
                    getCoreElevator().sendDescPacketFromAllElevators();
                    return null;
                } else {
                    throw new IllegalArgumentException("setHeight does take one argument (height)");
                }
            }
        });

        luaMethods.add(new LuaMethod("setExternalControl") {
            @Override
            public Object[] call(Object[] args) throws Exception {
                if (args.length == 1) {
                    if ((Boolean) args[0] && getCoreElevator().isControlledByRedstone() || !(Boolean) args[0] && !getCoreElevator().isControlledByRedstone()) {
                        getCoreElevator().handleGUIButtonPress(0, null);
                    }
                    return null;
                } else {
                    throw new IllegalArgumentException("setExternalControl does take one argument! (bool)");
                }
            }
        });
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return inventory;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_ELEVATOR;
    }
}
