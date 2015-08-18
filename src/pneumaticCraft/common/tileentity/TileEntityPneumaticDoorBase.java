package pneumaticCraft.common.tileentity;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.block.BlockPneumaticDoor;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.network.LazySynced;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.TileEntityConstants;

public class TileEntityPneumaticDoorBase extends TileEntityPneumaticBase implements IInventory, IRedstoneControl,
        IMinWorkingPressure{
    private TileEntityPneumaticDoor door;
    private TileEntityPneumaticDoorBase doubleDoor;
    @DescSynced
    public boolean rightGoing;
    public float oldProgress;
    @DescSynced
    @LazySynced
    public float progress;
    @DescSynced
    private boolean opening;
    public boolean wasPowered;
    @DescSynced
    private ItemStack[] inventory = new ItemStack[5];
    @GuiSynced
    public int redstoneMode;
    @DescSynced
    public ForgeDirection orientation = ForgeDirection.UNKNOWN;
    public static final int UPGRADE_SLOT_1 = 0;
    public static final int UPGRADE_SLOT_4 = 3;
    public static final int CAMO_SLOT = 4;
    private ItemStack oldCamo;

    public TileEntityPneumaticDoorBase(){
        super(PneumaticValues.DANGER_PRESSURE_PNEUMATIC_DOOR, PneumaticValues.MAX_PRESSURE_PNEUMATIC_DOOR, PneumaticValues.VOLUME_PNEUMATIC_DOOR);
        setUpgradeSlots(new int[]{UPGRADE_SLOT_1, 1, 2, UPGRADE_SLOT_4});
    }

    @Override
    public void updateEntity(){
        super.updateEntity();
        oldProgress = progress;
        if(!worldObj.isRemote) {
            if(getPressure(ForgeDirection.UNKNOWN) >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
                if(worldObj.getTotalWorldTime() % 60 == 0) {
                    TileEntity te = worldObj.getTileEntity(orientation.offsetX * 3 + xCoord, yCoord, orientation.offsetZ * 3 + zCoord);
                    if(te instanceof TileEntityPneumaticDoorBase) {
                        doubleDoor = (TileEntityPneumaticDoorBase)te;
                    } else {
                        doubleDoor = null;
                    }
                }
                setOpening(shouldOpen() || isNeighborOpening());
                setNeighborOpening(isOpening());
            } else {
                setOpening(true);
            }
        }
        float targetProgress = opening ? 1F : 0F;
        float speedMultiplier = getSpeedMultiplierFromUpgrades(getUpgradeSlots());
        if(progress < targetProgress) {
            if(progress < targetProgress - TileEntityConstants.PNEUMATIC_DOOR_EXTENSION) {
                progress += TileEntityConstants.PNEUMATIC_DOOR_SPEED_FAST * speedMultiplier;
            } else {
                progress += TileEntityConstants.PNEUMATIC_DOOR_SPEED_SLOW * speedMultiplier;
            }
            if(progress > targetProgress) progress = targetProgress;
        }
        if(progress > targetProgress) {
            if(progress > targetProgress + TileEntityConstants.PNEUMATIC_DOOR_EXTENSION) {
                progress -= TileEntityConstants.PNEUMATIC_DOOR_SPEED_FAST * speedMultiplier;
            } else {
                progress -= TileEntityConstants.PNEUMATIC_DOOR_SPEED_SLOW * speedMultiplier;
            }
            if(progress < targetProgress) progress = targetProgress;
        }
        if(!worldObj.isRemote) addAir((int)(-Math.abs(oldProgress - progress) * PneumaticValues.USAGE_PNEUMATIC_DOOR * (getSpeedUsageMultiplierFromUpgrades(getUpgradeSlots()) / speedMultiplier)), ForgeDirection.UNKNOWN);

        // if(worldObj.isRemote) System.out.println("progress: " + progress);
        door = getDoor();
        if(door != null) {
            door.setRotation(progress * 90);
            if(!worldObj.isRemote) rightGoing = door.rightGoing;
        }

        if(oldCamo != inventory[CAMO_SLOT]) {
            oldCamo = inventory[CAMO_SLOT];
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, inventory[CAMO_SLOT] != null ? inventory[CAMO_SLOT].getItemDamage() % 16 : 0, 2);
            worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
        }
    }

    private boolean shouldOpen(){
        switch(redstoneMode){
            case 0:
            case 1:
                int range = TileEntityConstants.RANGE_PNEUMATIC_DOOR_BASE + this.getUpgrades(ItemMachineUpgrade.UPGRADE_RANGE);
                AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(xCoord - range, yCoord - range, zCoord - range, xCoord + range + 1, yCoord + range + 1, zCoord + range + 1);
                List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, aabb);
                for(EntityPlayer player : players) {
                    if(PneumaticCraftUtils.getProtectingSecurityStations(worldObj, xCoord, yCoord, zCoord, player, false, false) == 0) {
                        if(redstoneMode == 0) {
                            return true;
                        } else {
                            ((BlockPneumaticDoor)Blockss.pneumaticDoor).isTrackingPlayerEye = true;
                            ChunkPosition lookedPosition = PneumaticCraftUtils.getEntityLookedBlock(player, range * 1.41F); //max range = range * sqrt(2).
                            ((BlockPneumaticDoor)Blockss.pneumaticDoor).isTrackingPlayerEye = false;
                            if(lookedPosition != null) {
                                if(lookedPosition.equals(new ChunkPosition(xCoord, yCoord, zCoord))) {
                                    return true;
                                } else {
                                    if(door != null) {
                                        if(lookedPosition.equals(new ChunkPosition(door.xCoord, door.yCoord, door.zCoord))) return true;
                                        if(lookedPosition.equals(new ChunkPosition(door.xCoord, door.yCoord + (door.getBlockMetadata() < 6 ? 1 : -1), door.zCoord))) return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            case 2:
                return opening;
        }
        return false;
    }

    public void setOpening(boolean opening){
        this.opening = opening;
    }

    public boolean isOpening(){
        return opening;
    }

    private boolean isNeighborOpening(){
        return doubleDoor != null ? doubleDoor.shouldOpen() : false;
    }

    public void setNeighborOpening(boolean opening){
        if(doubleDoor != null && doubleDoor.getPressure(ForgeDirection.UNKNOWN) >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
            doubleDoor.setOpening(opening);
        }
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return side != ForgeDirection.UP;
    }

    private TileEntityPneumaticDoor getDoor(){
        TileEntity te = worldObj.getTileEntity(orientation.offsetX + xCoord, yCoord - 1, orientation.offsetZ + zCoord);
        if(te instanceof TileEntityPneumaticDoor) {
            if(orientation.getRotation(ForgeDirection.UP) == ForgeDirection.getOrientation(te.getBlockMetadata()) && !((TileEntityPneumaticDoor)te).rightGoing) {
                return (TileEntityPneumaticDoor)te;
            } else if(orientation.getRotation(ForgeDirection.DOWN) == ForgeDirection.getOrientation(te.getBlockMetadata()) && ((TileEntityPneumaticDoor)te).rightGoing) {
                return (TileEntityPneumaticDoor)te;
            }
        }
        return null;
    }

    // NBT methods-----------------------------------------------
    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        progress = tag.getFloat("extension");
        opening = tag.getBoolean("opening");
        redstoneMode = tag.getInteger("redstoneMode");
        orientation = ForgeDirection.getOrientation(tag.getInteger("orientation"));
        rightGoing = tag.getBoolean("rightGoing");
        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = tag.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];
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
        tag.setFloat("extension", progress);
        tag.setBoolean("opening", opening);
        tag.setInteger("orientation", orientation.ordinal());
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setBoolean("rightGoing", rightGoing);
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
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 2) redstoneMode = 0;
        }
    }

    // INVENTORY METHODS-
    // ------------------------------------------------------------

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){

        return inventory.length;
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int slot){

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
        inventory[slot] = itemStack;
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
        return Blockss.pneumaticDoorBase.getUnlocalizedName();
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        return i == CAMO_SLOT || itemstack.getItem() == Itemss.machineUpgrade;
    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1){
        return isGuiUseableByPlayer(var1);
    }

    @Override
    public float getMinWorkingPressure(){
        return PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR;
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }
}
