package pneumaticCraft.common.tileentity;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.util.IOHelper;

public class TileEntityOmnidirectionalHopper extends TileEntity implements IGUIButtonSensitive, ISidedInventory{
    private ForgeDirection inputDir = ForgeDirection.UNKNOWN;
    private ItemStack[] inventory = new ItemStack[9];
    public int redstoneMode;
    private int cooldown;

    @Override
    public void updateEntity(){
        if(!getWorldObj().isRemote && --cooldown <= 0 && redstoneAllows()) {
            int maxItems = getMaxItems();
            boolean success = suckInItem(maxItems);
            success |= exportItem(maxItems);

            if(!success) {
                cooldown = 8;//When insertion failed, do a long cooldown as penalty for performance.
            } else {
                cooldown = getItemTransferInterval();
            }
        }
    }

    private boolean exportItem(int maxItems){
        ForgeDirection dir = ForgeDirection.getOrientation(getBlockMetadata());
        TileEntity neighbor = IOHelper.getNeighbor(this, dir);
        for(int i = 0; i < 5; i++) {
            ItemStack stack = inventory[i];
            if(stack != null) {
                ItemStack exportedStack = stack.copy();
                if(exportedStack.stackSize > maxItems) exportedStack.stackSize = maxItems;
                int count = exportedStack.stackSize;

                ItemStack remainder = IOHelper.insert(neighbor, exportedStack, dir.getOpposite(), false);
                int exportedItems = count - (remainder == null ? 0 : remainder.stackSize);

                stack.stackSize -= exportedItems;
                if(stack.stackSize <= 0) {
                    setInventorySlotContents(i, null);
                }
                maxItems -= exportedItems;
                if(maxItems <= 0) return true;
            }
        }
        return false;
    }

    private boolean suckInItem(int maxItems){
        TileEntity inputInv = IOHelper.getNeighbor(this, inputDir);
        boolean success = false;

        //Suck from input inventory.
        for(int i = 0; i < maxItems; i++) {
            ItemStack extracted = IOHelper.extractOneItem(inputInv, inputDir.getOpposite(), true);//simulate extraction from the neighbor.
            if(extracted != null) {
                ItemStack inserted = IOHelper.insert(this, extracted, ForgeDirection.UNKNOWN, false);//if we can insert the item in this hopper.
                if(inserted == null) {
                    IOHelper.extractOneItem(inputInv, inputDir.getOpposite(), false); //actually retrieve it from the neighbor.
                    success = true;
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(xCoord + inputDir.offsetX, yCoord + inputDir.offsetY, zCoord + inputDir.offsetZ, xCoord + inputDir.offsetX + 1, yCoord + inputDir.offsetY + 1, zCoord + inputDir.offsetZ + 1);
        for(EntityItem entity : (List<EntityItem>)worldObj.getEntitiesWithinAABB(EntityItem.class, box)) {
            if(!entity.isDead) {
                ItemStack remainder = IOHelper.insert(this, entity.getEntityItem(), ForgeDirection.UNKNOWN, false);
                if(remainder == null) {
                    entity.setDead();
                    success = true;//Don't set true when the stack could not be fully consumes, as that means next insertion there won't be any room.
                }
            }
        }

        return success;
    }

    public int getMaxItems(){
        int upgrades = getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE, 5, 8);
        if(upgrades > 5) {
            return Math.min((int)Math.pow(2, upgrades - 5), 256);
        } else {
            return 1;
        }
    }

    public int getItemTransferInterval(){
        return 8 / (1 + getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE, 5, 8));
    }

    protected int getUpgrades(int upgradeDamage, int startSlot, int endSlot){
        int upgrades = 0;
        for(int i = startSlot; i <= endSlot; i++) {
            if(getStackInSlot(i) != null && getStackInSlot(i).getItem() == Itemss.machineUpgrade && getStackInSlot(i).getItemDamage() == upgradeDamage) {
                upgrades += getStackInSlot(i).stackSize;
            }
        }
        return upgrades;
    }

    public void setDirection(ForgeDirection dir){
        inputDir = dir;
    }

    public ForgeDirection getDirection(){
        return inputDir;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setInteger("inputDir", inputDir.ordinal());
        tag.setInteger("redstoneMode", redstoneMode);

        NBTTagList tagList = new NBTTagList();
        for(int currentIndex = 0; currentIndex < inventory.length; ++currentIndex) {
            if(inventory[currentIndex] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)currentIndex);
                inventory[currentIndex].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        tag.setTag("Inventory", tagList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        inputDir = ForgeDirection.getOrientation(tag.getInteger("inputDir"));
        redstoneMode = tag.getInteger("redstoneMode");

        NBTTagList tagList = tag.getTagList("Inventory", 10);
        inventory = new ItemStack[9];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < 9) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }

    @Override
    public Packet getDescriptionPacket(){
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
        readFromNBT(pkt.func_148857_g());
    }

    /**
     * Returns the name of the inventory.
     */
    @Override
    public String getInventoryName(){
        return "Omnidirectional Hopper";
    }

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
    public ItemStack getStackInSlot(int par1){
        return inventory[par1];
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
        inventory[slot] = itemStack;
        if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
    }

    private boolean redstoneAllows(){
        switch(redstoneMode){
            case 0:
                return true;
            case 1:
                return !worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
            case 2:
                return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
        }
        return false;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        redstoneMode++;
        if(redstoneMode > 2) redstoneMode = 0;
        sendDescriptionPacket();
    }

    /**
     * Sends the description packet to every client within PACKET_UPDATE_DISTANCE blocks, and in the same dimension.
     */
    public void sendDescriptionPacket(){
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack){
        return par1 < 5;
    }

    private static final int[] accessibleSlots = {0, 1, 2, 3, 4};

    @Override
    public int[] getAccessibleSlotsFromSide(int var1){
        return accessibleSlots;
    }

    @Override
    public boolean canInsertItem(int var1, ItemStack var2, int var3){
        return var1 < 5;
    }

    @Override
    public boolean canExtractItem(int var1, ItemStack var2, int var3){
        return var1 < 5;
    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    @Override
    public int getInventoryStackLimit(){
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_){
        return true;
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}
}
