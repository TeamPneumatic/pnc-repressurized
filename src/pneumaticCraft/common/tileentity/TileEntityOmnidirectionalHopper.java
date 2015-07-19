package pneumaticCraft.common.tileentity;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.util.IOHelper;

public class TileEntityOmnidirectionalHopper extends TileEntityBase implements ISidedInventory, IRedstoneControlled{
    @DescSynced
    protected ForgeDirection inputDir = ForgeDirection.UNKNOWN;
    private ItemStack[] inventory = new ItemStack[getInvSize()];
    @GuiSynced
    public int redstoneMode;
    private int cooldown;
    @GuiSynced
    protected boolean leaveMaterial;//leave items/liquids (used as filter)

    public TileEntityOmnidirectionalHopper(){
        setUpgradeSlots(5, 6, 7, 8);
    }

    protected int getInvSize(){
        return 9;
    }

    @Override
    public void updateEntity(){
        super.updateEntity();
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

    protected boolean exportItem(int maxItems){
        ForgeDirection dir = ForgeDirection.getOrientation(getBlockMetadata());
        TileEntity neighbor = IOHelper.getNeighbor(this, dir);
        for(int i = 0; i < 5; i++) {
            ItemStack stack = inventory[i];
            if(stack != null && (!leaveMaterial || stack.stackSize > 1)) {
                ItemStack exportedStack = stack.copy();
                if(leaveMaterial) exportedStack.stackSize--;
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

    protected boolean suckInItem(int maxItems){
        TileEntity inputInv = IOHelper.getNeighbor(this, inputDir);
        boolean success = false;

        //Suck from input inventory.
        for(int i = 0; i < maxItems; i++) {
            if(hasEmptySlot()) {
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
            } else {
                for(int slot = 0; slot < 5; slot++) {
                    ItemStack stack = inventory[slot];
                    stack = stack.copy();
                    stack.stackSize = 1;
                    ItemStack extracted = IOHelper.extract(inputInv, inputDir.getOpposite(), stack, true, true);//simulate extraction from the neighbor.
                    if(extracted != null) {
                        ItemStack inserted = IOHelper.insert(this, extracted, ForgeDirection.UNKNOWN, false);//if we can insert the item in this hopper.
                        if(inserted == null) {
                            IOHelper.extract(inputInv, inputDir.getOpposite(), stack, true, false); //actually retrieve it from the neighbor.
                            success = true;
                            break;
                        }
                    }
                }
                if(!success) break;
            }
        }

        for(EntityItem entity : getNeighborItems(this, inputDir)) {
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

    private boolean hasEmptySlot(){
        for(int i = 0; i < 5; i++) {
            if(inventory[i] == null) return true;
        }
        return false;
    }

    public static List<EntityItem> getNeighborItems(TileEntity te, ForgeDirection inputDir){
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(te.xCoord + inputDir.offsetX, te.yCoord + inputDir.offsetY, te.zCoord + inputDir.offsetZ, te.xCoord + inputDir.offsetX + 1, te.yCoord + inputDir.offsetY + 1, te.zCoord + inputDir.offsetZ + 1);
        return te.getWorldObj().getEntitiesWithinAABB(EntityItem.class, box);
    }

    public int getMaxItems(){
        int upgrades = getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE, getUpgradeSlots());
        if(upgrades > 3) {
            return Math.min((int)Math.pow(2, upgrades - 3), 256);
        } else {
            return 1;
        }
    }

    public int getItemTransferInterval(){
        return 8 / (int)Math.pow(2, getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE, getUpgradeSlots()));
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
        tag.setBoolean("leaveMaterial", leaveMaterial);

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
        leaveMaterial = tag.getBoolean("leaveMaterial");

        NBTTagList tagList = tag.getTagList("Inventory", 10);
        inventory = new ItemStack[inventory.length];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }

    /**
     * Returns the name of the inventory.
     */
    @Override
    public String getInventoryName(){
        return Blockss.omnidirectionalHopper.getUnlocalizedName();
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

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 2) redstoneMode = 0;
        } else if(buttonID == 1) {
            leaveMaterial = false;
        } else if(buttonID == 2) {
            leaveMaterial = true;
        }
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
        return isGuiUseableByPlayer(p_70300_1_);
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }

    public boolean doesLeaveMaterial(){
        return leaveMaterial;
    }
}
