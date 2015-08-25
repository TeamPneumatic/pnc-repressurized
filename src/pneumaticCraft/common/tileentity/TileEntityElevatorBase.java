package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.block.BlockElevatorBase;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.network.LazySynced;
import pneumaticCraft.common.network.PacketServerTickTime;
import pneumaticCraft.common.thirdparty.computercraft.LuaConstant;
import pneumaticCraft.common.thirdparty.computercraft.LuaMethod;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Sounds;
import pneumaticCraft.lib.TileEntityConstants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityElevatorBase extends TileEntityPneumaticBase implements IInventory, IGUITextFieldSensitive,
        IRedstoneControlled, IMinWorkingPressure{
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
    private ItemStack[] inventory = new ItemStack[6];
    public Block baseCamo, frameCamo;
    public static final int UPGRADE_SLOT_1 = 0;
    public static final int UPGRADE_SLOT_4 = 3;

    public TileEntityElevatorBase(){
        super(PneumaticValues.DANGER_PRESSURE_ELEVATOR, PneumaticValues.MAX_PRESSURE_ELEVATOR, PneumaticValues.VOLUME_ELEVATOR);
        setUpgradeSlots(new int[]{UPGRADE_SLOT_1, 1, 2, UPGRADE_SLOT_4});
    }

    @Override
    public void updateEntity(){
        oldExtension = extension;
        if(worldObj.isRemote && worldObj.getTotalWorldTime() % 60 == 0) coreElevator = null;//reset this because the client doesn't get notified of neighbor block updates.
        if(isCoreElevator()) {
            super.updateEntity();
            if(!worldObj.isRemote && isControlledByRedstone()) {
                float oldTargetExtension = targetExtension;
                float maxExtension = getMaxElevatorHeight();

                int redstoneInput = redstoneInputLevel;
                if(multiElevators != null) {
                    for(TileEntityElevatorBase base : multiElevators) {
                        redstoneInput = Math.max(redstoneInputLevel, base.redstoneInputLevel);
                    }
                }

                targetExtension = redstoneInput * maxExtension / 15;
                if(targetExtension > oldExtension && getPressure(ForgeDirection.UNKNOWN) < PneumaticValues.MIN_PRESSURE_ELEVATOR) targetExtension = oldExtension; // only ascent when there's enough pressure
                if(oldTargetExtension != targetExtension) sendDescPacketFromAllElevators();
            }
            float speedMultiplier = getSpeedMultiplierFromUpgrades(getUpgradeSlots());
            if(worldObj.isRemote) {
                speedMultiplier = (float)(speedMultiplier * PacketServerTickTime.tickTimeMultiplier);
            }

            String soundName = null;
            if(extension < targetExtension) {
                if(!worldObj.isRemote && getPressure(ForgeDirection.UNKNOWN) < PneumaticValues.MIN_PRESSURE_ELEVATOR) {
                    targetExtension = extension;
                    sendDescPacket(256D);
                }
                soundName = Sounds.ELEVATOR_MOVING;

                float moveBy;

                if(extension < targetExtension - TileEntityConstants.ELEVATOR_SLOW_EXTENSION) {
                    moveBy = TileEntityConstants.ELEVATOR_SPEED_FAST * speedMultiplier;
                } else {
                    moveBy = TileEntityConstants.ELEVATOR_SPEED_SLOW * speedMultiplier;
                }
                if(extension + moveBy > targetExtension) {
                    extension = targetExtension;
                    if(!worldObj.isRemote) updateFloors();
                }
                if(isStopped) {
                    soundName = Sounds.ELEVATOR_START;
                    isStopped = false;
                }
                float startingExtension = extension;

                while(extension < startingExtension + moveBy) {
                    extension += TileEntityConstants.ELEVATOR_SPEED_SLOW;
                    /*
                    if(extension > startingExtension + moveBy) {
                        extension = startingExtension + moveBy;
                    }
                    */
                    // moveEntities(TileEntityConstants.ELEVATOR_SPEED_SLOW);
                }
                addAir((int)((oldExtension - extension) * PneumaticValues.USAGE_ELEVATOR * (getSpeedUsageMultiplierFromUpgrades(getUpgradeSlots()) / speedMultiplier)), ForgeDirection.UNKNOWN);// substract the ascended distance from the air reservoir.
            }
            if(extension > targetExtension) {
                soundName = Sounds.ELEVATOR_MOVING;
                if(extension > targetExtension + TileEntityConstants.ELEVATOR_SLOW_EXTENSION) {
                    extension -= TileEntityConstants.ELEVATOR_SPEED_FAST * speedMultiplier;
                } else {
                    extension -= TileEntityConstants.ELEVATOR_SPEED_SLOW * speedMultiplier;
                }
                if(extension < targetExtension) {
                    extension = targetExtension;
                    if(!worldObj.isRemote) updateFloors();
                }
                if(isStopped) {
                    soundName = Sounds.ELEVATOR_START;
                    isStopped = false;
                }
                //  movePlayerDown();
            }
            if(oldExtension == extension && !isStopped) {
                soundName = Sounds.ELEVATOR_STOP;
                isStopped = true;
                soundCounter = 0;
            }

            if(soundCounter > 0) soundCounter--;
            if(soundName != null && worldObj.isRemote && soundCounter == 0) {
                worldObj.playSound(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, soundName, 0.1F, 1.0F, true);
                soundCounter = 10;
            }

        } else {
            extension = 0;
        }
        if(!worldObj.isRemote && oldExtension != extension) {
            sendDescPacket(256);
        }
    }

    private void movePlayerDown(){
        if(!worldObj.isRemote) return;

        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + oldExtension + 1.05F, zCoord + 1);
        List<Entity> entityList = worldObj.getEntitiesWithinAABBExcludingEntity(null, aabb);
        for(Entity entity : entityList) {
            if(entity instanceof EntityPlayer) {
                //   moveEntityToCenter(entity);
                double posX = entity.posX;
                double posZ = entity.posZ;
                if(posX >= xCoord && posX < xCoord + 1 && posZ >= zCoord && posZ < zCoord + 1) {
                    entity.motionX *= 0.6;
                    entity.motionZ *= 0.6;
                    entity.moveEntity(0, extension - oldExtension + 0.001F, 0);
                }
            }
        }
    }

    private void moveEntities(float moveBy){
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + extension + 1, zCoord + 1);
        List<Entity> entityList = worldObj.getEntitiesWithinAABBExcludingEntity(null, aabb);
        for(Entity entity : entityList) {
            if(entity instanceof EntityPlayer) {

            } else entity.moveEntity(0, moveBy + 0.05F, 0);
        }
    }

    private void moveEntityToCenter(Entity entity){
        /*
         * this is for your own protection. you may hurt yourself if you're not standing right, especially
         * on multiblock-elevators (entity will be found by multiple bases, causing problems)
         */
        ((EntityPlayer)entity).setPosition(xCoord + 0.5F, entity.posY, zCoord + 0.5F);
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 1) redstoneMode = 0;

            if(multiElevators != null) {
                for(TileEntityElevatorBase base : multiElevators) {
                    while(base.redstoneMode != redstoneMode) {
                        base.handleGUIButtonPress(buttonID, player);
                    }
                }
            }

            int i = -1;
            TileEntity te = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
            while(te instanceof TileEntityElevatorBase) {
                ((TileEntityElevatorBase)te).redstoneMode = redstoneMode;
                i--;
                te = worldObj.getTileEntity(xCoord, yCoord + i, zCoord);
            }
        }
    }

    private boolean isControlledByRedstone(){
        return redstoneMode == 0;
    }

    public void updateRedstoneInputLevel(){
        if(multiElevators == null) return;

        int maxRedstone = 0;
        for(TileEntityElevatorBase base : multiElevators) {
            int i = 0;
            while(worldObj.getBlock(base.xCoord, base.yCoord + i, base.zCoord) == Blockss.elevatorBase) {
                maxRedstone = Math.max(maxRedstone, PneumaticCraftUtils.getRedstoneLevel(worldObj, base.xCoord, base.yCoord + i, base.zCoord));
                i--;
            }
        }
        for(TileEntityElevatorBase base : multiElevators) {
            base.redstoneInputLevel = maxRedstone;
        }
    }

    public float getMaxElevatorHeight(){
        int max = maxFloorHeight;
        if(multiElevators != null) {
            for(TileEntityElevatorBase base : multiElevators) {
                max = Math.max(max, base.maxFloorHeight);
            }
        }
        return max;
    }

    public void updateMaxElevatorHeight(){
        int i = -1;
        do {
            i++;
        } while(worldObj.getBlock(xCoord, yCoord + i + 1, zCoord) == Blockss.elevatorFrame);
        int elevatorBases = 0;
        do {
            elevatorBases++;
        } while(worldObj.getBlock(xCoord, yCoord - elevatorBases, zCoord) == Blockss.elevatorBase);

        maxFloorHeight = Math.min(i, elevatorBases * Config.elevatorBaseBlocksPerBase);
    }

    // NBT methods-----------------------------------------------
    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        extension = tag.getFloat("extension");
        targetExtension = tag.getFloat("targetExtension");
        redstoneMode = tag.getInteger("redstoneMode");
        if(!tag.hasKey("maxFloorHeight")) {//backwards compatibility implementation.
            updateMaxElevatorHeight();
        } else {
            maxFloorHeight = tag.getInteger("maxFloorHeight");
        }
        for(int i = 0; i < 6; i++) {
            sidesConnected[i] = tag.getBoolean("sideConnected" + i);
        }

        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = tag.getTagList("Items", 10);
        inventory = new ItemStack[inventory.length];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setFloat("extension", extension);
        tag.setFloat("targetExtension", targetExtension);
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setInteger("maxFloorHeight", maxFloorHeight);
        for(int i = 0; i < 6; i++) {
            tag.setBoolean("sideConnected" + i, sidesConnected[i]);
        }

        // Write the ItemStacks in the inventory to NBT
        NBTTagList tagList = new NBTTagList();
        for(int currentIndex = 0; currentIndex < inventory.length; ++currentIndex) {
            if(inventory[currentIndex] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)currentIndex);
                inventory[currentIndex].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        tag.setTag("Items", tagList);

    }

    @Override
    public void readFromPacket(NBTTagCompound tag){
        super.readFromPacket(tag);
        floorHeights = tag.getIntArray("floorHeights");

        floorNames.clear();
        NBTTagList floorNameList = tag.getTagList("floorNames", 10);
        for(int i = 0; i < floorNameList.tagCount(); i++) {
            NBTTagCompound floorName = floorNameList.getCompoundTagAt(i);
            floorNames.put(floorName.getInteger("floorHeight"), floorName.getString("floorName"));
        }
    }

    @Override
    public void writeToPacket(NBTTagCompound tag){
        super.writeToPacket(tag);
        tag.setIntArray("floorHeights", floorHeights);

        NBTTagList floorNameList = new NBTTagList();
        for(int key : floorNames.keySet()) {
            NBTTagCompound floorNameTag = new NBTTagCompound();
            floorNameTag.setInteger("floorHeight", key);
            floorNameTag.setString("floorName", floorNames.get(key));
            floorNameList.appendTag(floorNameTag);
        }
        tag.setTag("floorNames", floorNameList);
    }

    @Override
    public void onNeighborTileUpdate(){
        super.onNeighborTileUpdate();
        updateConnections();
        connectAsMultiblock();
    }

    private void connectAsMultiblock(){
        multiElevators = null;
        if(isCoreElevator()) {
            multiElevators = new ArrayList<TileEntityElevatorBase>();
            Stack<TileEntityElevatorBase> todo = new Stack<TileEntityElevatorBase>();
            todo.add(this);
            while(!todo.isEmpty()) {
                TileEntityElevatorBase curElevator = todo.pop();
                if(curElevator.isCoreElevator()) {
                    multiElevators.add(curElevator);
                    curElevator.multiElevators = multiElevators;
                    for(int i = 2; i < 6; i++) {
                        TileEntity te = curElevator.getTileCache()[i].getTileEntity();
                        if(!multiElevators.contains(te) && te instanceof TileEntityElevatorBase) {
                            todo.push((TileEntityElevatorBase)te);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onNeighborBlockUpdate(){
        super.onNeighborBlockUpdate();
        getCoreElevator().updateRedstoneInputLevel();
    }

    public void updateConnections(){
        for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity te = worldObj.getTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
            if(te instanceof IPneumaticMachine) {
                sidesConnected[direction.ordinal()] = ((IPneumaticMachine)te).isConnectedTo(direction.getOpposite());
            } else {
                sidesConnected[direction.ordinal()] = false;
            }
        }
        if(worldObj.getBlock(xCoord, yCoord + 1, zCoord) != Blockss.elevatorBase) {
            coreElevator = this;
            int i = -1;
            TileEntity te = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
            while(te instanceof TileEntityElevatorBase) {
                ((TileEntityElevatorBase)te).coreElevator = this;
                i--;
                te = worldObj.getTileEntity(xCoord, yCoord + i, zCoord);
            }
        }
    }

    public void moveInventoryToThis(){
        TileEntity te = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
        if(te instanceof TileEntityElevatorBase) {
            for(int i = 0; i < getSizeInventory(); i++) {
                inventory[i] = ((TileEntityElevatorBase)te).inventory[i];
                ((TileEntityElevatorBase)te).inventory[i] = null;
            }
        }
    }

    public void updateFloors(){
        List<Integer> floorList = new ArrayList<Integer>();
        List<ChunkPosition> callerList = new ArrayList<ChunkPosition>();

        if(multiElevators != null) {
            int i = 0;
            boolean shouldBreak = false;
            while(!shouldBreak) {
                boolean registeredThisFloor = false;
                for(TileEntityElevatorBase base : multiElevators) {
                    for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                        if(dir != ForgeDirection.UP && dir != ForgeDirection.DOWN) {
                            if(base.worldObj.getBlock(base.xCoord + dir.offsetX, base.yCoord + i + 2, base.zCoord + dir.offsetZ) == Blockss.elevatorCaller) {
                                callerList.add(new ChunkPosition(base.xCoord + dir.offsetX, base.yCoord + i + 2, base.zCoord + dir.offsetZ));
                                if(!registeredThisFloor) floorList.add(i);
                                registeredThisFloor = true;
                            }
                        }
                    }
                }

                i++;
                for(TileEntityElevatorBase base : multiElevators) {
                    if(base.worldObj.getBlock(base.xCoord, base.yCoord + i, base.zCoord) != Blockss.elevatorFrame) {
                        shouldBreak = true;
                        break;
                    }
                }
            }

            for(TileEntityElevatorBase base : multiElevators) {
                base.floorHeights = new int[floorList.size()];
                for(i = 0; i < base.floorHeights.length; i++) {
                    base.floorHeights[i] = floorList.get(i);
                }
            }
        }

        double buttonHeight = 0.06D;
        double buttonSpacing = 0.02D;
        TileEntityElevatorCaller.ElevatorButton[] elevatorButtons = new TileEntityElevatorCaller.ElevatorButton[floorHeights.length];
        int columns = (elevatorButtons.length - 1) / 12 + 1;
        for(int j = 0; j < columns; j++) {
            for(int i = j * 12; i < floorHeights.length && i < j * 12 + 12; i++) {
                elevatorButtons[i] = new TileEntityElevatorCaller.ElevatorButton(0.2D + 0.6D / columns * j, 0.5D + (Math.min(floorHeights.length, 12) - 2) * (buttonSpacing + buttonHeight) / 2 - i % 12 * (buttonHeight + buttonSpacing), 0.58D / columns, buttonHeight, i, floorHeights[i]);
                elevatorButtons[i].setColor(floorHeights[i] == targetExtension ? 0 : 1, 1, floorHeights[i] == targetExtension ? 0 : 1);
                String floorName = floorNames.get(floorHeights[i]);
                if(floorName != null) {
                    elevatorButtons[i].buttonText = floorName;
                } else {
                    floorNames.put(floorHeights[i], elevatorButtons[i].buttonText);
                }
            }
        }

        if(multiElevators != null) {
            for(TileEntityElevatorBase base : multiElevators) {
                base.floorNames = new HashMap<Integer, String>(floorNames);
            }
        }

        for(ChunkPosition p : callerList) {
            TileEntity te = worldObj.getTileEntity(p.chunkPosX, p.chunkPosY, p.chunkPosZ);
            if(te instanceof TileEntityElevatorCaller) {
                int callerFloorHeight = p.chunkPosY - yCoord - 2;
                int callerFloor = -1;
                for(TileEntityElevatorCaller.ElevatorButton floor : elevatorButtons) {
                    if(floor.floorHeight == callerFloorHeight) {
                        callerFloor = floor.floorNumber;
                        break;
                    }
                }
                if(callerFloor == -1) {
                    Log.error("Error while updating elevator floors! This will cause a indexOutOfBoundsException, index = -1");
                }
                ((TileEntityElevatorCaller)te).setEmittingRedstone(PneumaticCraftUtils.areFloatsEqual(targetExtension, extension, 0.1F) && PneumaticCraftUtils.areFloatsEqual(extension, callerFloorHeight, 0.1F));
                ((TileEntityElevatorCaller)te).setFloors(elevatorButtons, callerFloor);
            }
        }
    }

    public void goToFloor(int floor){
        if(getCoreElevator().isControlledByRedstone()) getCoreElevator().handleGUIButtonPress(0, null);
        if(floor >= 0 && floor < floorHeights.length) setTargetHeight(floorHeights[floor]);
        updateFloors();
        sendDescPacketFromAllElevators();
    }

    private void setTargetHeight(float height){
        height = Math.min(height, getMaxElevatorHeight());
        if(multiElevators != null) {
            for(TileEntityElevatorBase base : multiElevators) {
                base.targetExtension = height;
            }
        }
    }

    @Override
    public void onDescUpdate(){
        baseCamo = inventory[4] != null && inventory[4].getItem() instanceof ItemBlock ? ((ItemBlock)inventory[4].getItem()).field_150939_a : null;
        Block newFrameCamo = inventory[5] != null && inventory[5].getItem() instanceof ItemBlock ? ((ItemBlock)inventory[5].getItem()).field_150939_a : null;

        if(newFrameCamo != frameCamo) {
            frameCamo = newFrameCamo;
            rerenderChunk();
        }
    }

    private void sendDescPacketFromAllElevators(){
        if(multiElevators != null) {
            for(TileEntityElevatorBase base : multiElevators) {
                base.sendDescPacket(256);
            }
        } else {
            sendDescPacket(256);
        }
    }

    // INVENTORY METHODS-
    // ------------------------------------------------------------

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){
        return getCoreElevator().inventory.length;
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int slot){
        return getCoreElevator().inventory[slot];
    }

    public ItemStack getRealStackInSlot(int slot){
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount){

        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            if(itemStack.stackSize <= amount) {
                setInventorySlotContents(slot, null);
            } else {
                itemStack = itemStack.splitStack(amount);
                if(itemStack.stackSize == 0) {
                    setInventorySlotContents(slot, null);
                }
            }
        }

        return itemStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot){

        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            setInventorySlotContents(slot, null);
        }
        return itemStack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack){
        // super.setInventorySlotContents(slot, itemStack);
        getCoreElevator().inventory[slot] = itemStack;
        if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
    }

    @Override
    public String getInventoryName(){
        return Blockss.elevatorBase.getUnlocalizedName();
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        return itemstack.getItem() == Itemss.machineUpgrade && i < 4 || itemstack.getItem() instanceof ItemBlock && i >= 4;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1){
        return isGuiUseableByPlayer(var1);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1 + extension, zCoord + 1);
    }

    @Override
    protected double getPacketDistance(){
        return 256;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared(){
        return 65536D;
    }

    private TileEntityElevatorBase getCoreElevator(){
        if(coreElevator == null) {
            coreElevator = BlockElevatorBase.getCoreTileEntity(worldObj, xCoord, yCoord, zCoord);
        }
        return coreElevator;
    }

    public boolean isCoreElevator(){
        return getCoreElevator() == this;
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return side != ForgeDirection.UP && side != ForgeDirection.DOWN || worldObj.getBlock(xCoord, yCoord + side.offsetY, zCoord) != Blockss.elevatorBase;
    }

    @Override
    public void addAir(int amount, ForgeDirection side){
        if(isCoreElevator()) {
            super.addAir(amount, side);
        } else {
            getCoreElevator().addAir(amount, side);
        }
    }

    @Override
    public float getPressure(ForgeDirection sideRequested){
        if(isCoreElevator()) {
            return super.getPressure(sideRequested);
        } else {
            return getCoreElevator().getPressure(sideRequested);
        }
    }

    @Override
    public int getCurrentAir(ForgeDirection sideRequested){
        if(isCoreElevator()) {
            return super.getCurrentAir(sideRequested);
        } else {
            return getCoreElevator().getCurrentAir(sideRequested);
        }
    }

    @Override
    public List<Pair<ForgeDirection, IAirHandler>> getConnectedPneumatics(){
        List<Pair<ForgeDirection, IAirHandler>> connectedMachines = super.getConnectedPneumatics();
        TileEntity te = getTileCache()[ForgeDirection.DOWN.ordinal()].getTileEntity();
        if(te instanceof TileEntityElevatorBase) {
            connectedMachines.addAll(((TileEntityElevatorBase)te).getConnectedPneumatics());
        }
        return connectedMachines;
    }

    @Override
    public void setText(int textFieldID, String text){
        setFloorName(textFieldID, text);
    }

    @Override
    public String getText(int textFieldID){
        return getFloorName(textFieldID);
    }

    public String getFloorName(int floor){
        return floor < floorHeights.length ? floorNames.get(floorHeights[floor]) : "";
    }

    public void setFloorName(int floor, String name){
        if(floor < floorHeights.length) {
            floorNames.put(floorHeights[floor], name);
            updateFloors();
        }
    }

    @Override
    public boolean isGuiUseableByPlayer(EntityPlayer par1EntityPlayer){
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    /*
     * COMPUTERCRAFT API
     */

    @Override
    public String getType(){
        return "elevator";
    }

    @Override
    protected void addLuaMethods(){
        super.addLuaMethods();
        luaMethods.add(new LuaConstant("getMinWorkingPressure", PneumaticValues.MIN_PRESSURE_ELEVATOR));
        luaMethods.add(new LuaMethod("setHeight"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    setTargetHeight(((Double)args[0]).floatValue());
                    if(getCoreElevator().isControlledByRedstone()) getCoreElevator().handleGUIButtonPress(0, null);
                    getCoreElevator().sendDescPacketFromAllElevators();
                    return null;
                } else {
                    throw new IllegalArgumentException("setHeight does take one argument (height)");
                }
            }
        });

        luaMethods.add(new LuaMethod("setExternalControl"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    if((Boolean)args[0] && getCoreElevator().isControlledByRedstone() || !(Boolean)args[0] && !getCoreElevator().isControlledByRedstone()) {
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
    public int getRedstoneMode(){
        return redstoneMode;
    }

    @Override
    public float getMinWorkingPressure(){
        return PneumaticValues.MIN_PRESSURE_ELEVATOR;
    }
}
