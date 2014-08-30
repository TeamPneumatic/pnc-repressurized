package pneumaticCraft.common.tileentity;

import net.minecraft.block.BlockHopper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.Facing;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;

public class TileEntityOmnidirectionalHopper extends TileEntityHopper implements IGUIButtonSensitive, ISidedInventory{
    private ForgeDirection inputDir = ForgeDirection.UNKNOWN;
    private ItemStack[] upgradeInventory = new ItemStack[4];
    public int redstoneMode;

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
        for(int currentIndex = 0; currentIndex < upgradeInventory.length; ++currentIndex) {
            if(upgradeInventory[currentIndex] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)currentIndex);
                upgradeInventory[currentIndex].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        tag.setTag("Upgrades", tagList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        inputDir = ForgeDirection.getOrientation(tag.getInteger("inputDir"));
        redstoneMode = tag.getInteger("redstoneMode");

        NBTTagList tagList = tag.getTagList("Upgrades", 10);
        upgradeInventory = new ItemStack[4];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < 4) {
                upgradeInventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
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
        return super.getSizeInventory() + 4;
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int par1){
        return par1 < super.getSizeInventory() ? super.getStackInSlot(par1) : upgradeInventory[par1 - super.getSizeInventory()];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount){
        int invSize = super.getSizeInventory();
        if(slot < invSize) {
            return super.decrStackSize(slot, amount);
        } else {
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
        if(slot < super.getSizeInventory()) {
            super.setInventorySlotContents(slot, itemStack);
        } else {
            upgradeInventory[slot - super.getSizeInventory()] = itemStack;
            if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
                itemStack.stackSize = getInventoryStackLimit();
            }
        }
    }

    @Override
    public boolean func_145887_i(){//updateHopper
        if(worldObj != null && !worldObj.isRemote) {
            if(!func_145888_j() && redstoneAllows()) {//isCoolingDown
                boolean flag = insertItemToInventory();
                flag = suckItemIntoHopper(this) || flag;

                if(flag) {
                    func_145896_c(8);//setTransferCooldown
                    markDirty();
                    return true;
                }
            }

            return false;
        } else {
            return false;
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
    public void func_145896_c(int par1){//setTransferCooldown
        super.func_145896_c(par1 > 0 ? getItemTransferInterval(par1) : 0);
    }

    public int getItemTransferInterval(int baseValue){
        return baseValue / (1 + getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE, 5, 8));
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

    /**
     * Inserts one item from the hopper into the inventory the hopper is pointing at.
     */
    private boolean insertItemToInventory(){
        IInventory iinventory = getOutputInventory();
        TileEntity te = null;
        ForgeDirection side = ForgeDirection.getOrientation(BlockHopper.getDirectionFromMetadata(getBlockMetadata()));
        if(iinventory == null) {
            te = worldObj.getTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);
        }
        side = side.getOpposite();
        if(iinventory != null || te != null) {
            for(int i = 0; i < 5; ++i) {
                if(getStackInSlot(i) != null) {
                    ItemStack itemstack = getStackInSlot(i).copy();
                    itemstack.stackSize = 1;

                    ItemStack itemstack1 = null;
                    if(iinventory != null) {
                        itemstack1 = func_145889_a(iinventory, itemstack, side.ordinal());//insertStack
                    } else {
                        itemstack1 = PneumaticCraftUtils.exportStackToInventory(te, itemstack, side);
                    }

                    if(itemstack1 == null || itemstack1.stackSize == 0) {
                        decrStackSize(i, 1);
                        if(iinventory != null) {
                            iinventory.markDirty();
                        } else {
                            te.markDirty();
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets the inventory the hopper is pointing at.
     */
    private IInventory getOutputInventory(){
        int i = BlockHopper.getDirectionFromMetadata(getBlockMetadata());
        return func_145893_b(getWorldObj(), xCoord + Facing.offsetsXForSide[i], yCoord + Facing.offsetsYForSide[i], zCoord + Facing.offsetsZForSide[i]);//getInventoryAtLocation
    }

    public static boolean suckItemsIntoHopper(IHopper hopper){
        return ((TileEntityOmnidirectionalHopper)hopper).suckItemIntoHopper(hopper);
    }

    /**
     * Sucks one item into the given hopper from an inventory or EntityItem above it.
     */
    public boolean suckItemIntoHopper(IHopper par0Hopper){
        IInventory iinventory = getInventory(par0Hopper);

        if(iinventory != null) {
            byte b0 = (byte)inputDir.getOpposite().ordinal();

            if(iinventory instanceof ISidedInventory && b0 > -1) {
                ISidedInventory isidedinventory = (ISidedInventory)iinventory;
                int[] aint = isidedinventory.getAccessibleSlotsFromSide(b0);

                for(int element : aint) {
                    if(insertStackFromInventory(par0Hopper, iinventory, element, b0)) {
                        return true;
                    }
                }
            } else {
                int j = iinventory.getSizeInventory();

                for(int k = 0; k < j; ++k) {
                    if(insertStackFromInventory(par0Hopper, iinventory, k, b0)) {
                        return true;
                    }
                }
            }
        } else {
            EntityItem entityitem = func_145897_a(par0Hopper.getWorldObj(), par0Hopper.getXPos() + inputDir.offsetX, par0Hopper.getYPos() + inputDir.offsetY, par0Hopper.getZPos() + inputDir.offsetZ);

            if(entityitem != null) {
                return func_145898_a(par0Hopper, entityitem);
            }
        }

        return false;
    }

    /**
    * Looks for anything, that can hold items (like chests, furnaces, etc.) one block above the given hopper.
    */
    public IInventory getInventory(IHopper par0Hopper){
        return func_145893_b(par0Hopper.getWorldObj(), par0Hopper.getXPos() + inputDir.offsetX, par0Hopper.getYPos() + inputDir.offsetY, par0Hopper.getZPos() + inputDir.offsetZ);
    }

    private static boolean insertStackFromInventory(IHopper par0Hopper, IInventory par1IInventory, int par2, int par3){
        ItemStack itemstack = par1IInventory.getStackInSlot(par2);

        if(itemstack != null && canExtractItemFromInventory(par1IInventory, itemstack, par2, par3)) {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = func_145889_a(par0Hopper, par1IInventory.decrStackSize(par2, 1), -1);

            if(itemstack2 == null || itemstack2.stackSize == 0) {
                par1IInventory.markDirty();
                return true;
            }

            par1IInventory.setInventorySlotContents(par2, itemstack1);
        }

        return false;
    }

    private static boolean canExtractItemFromInventory(IInventory par0IInventory, ItemStack par1ItemStack, int par2, int par3){
        return !(par0IInventory instanceof ISidedInventory) || ((ISidedInventory)par0IInventory).canExtractItem(par2, par1ItemStack, par3);
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
}
