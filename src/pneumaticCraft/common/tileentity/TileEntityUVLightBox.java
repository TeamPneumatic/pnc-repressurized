package pneumaticCraft.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.TileEntityConstants;

public class TileEntityUVLightBox extends TileEntityPneumaticBase implements ISidedInventory, IMinWorkingPressure,
        IRedstoneControl{
    @DescSynced
    public boolean leftConnected;
    @DescSynced
    public boolean rightConnected;
    @DescSynced
    public boolean areLightsOn;
    @GuiSynced
    public int redstoneMode;
    @DescSynced
    public ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
    public int ticksExisted;
    public static final int INVENTORY_SIZE = 5;
    public static final int PCB_INDEX = 0;
    public static final int UPGRADE_SLOT_START = 1;
    public static final int UPGRADE_SLOT_END = 4;
    private boolean oldRedstoneStatus;

    public TileEntityUVLightBox(){
        super(PneumaticValues.DANGER_PRESSURE_UV_LIGHTBOX, PneumaticValues.MAX_PRESSURE_UV_LIGHTBOX, PneumaticValues.VOLUME_UV_LIGHTBOX);
        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 2, 3, UPGRADE_SLOT_END});
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt){
        super.readFromNBT(nbt);
        redstoneMode = nbt.getInteger("redstoneMode");

        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = nbt.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
        //  if(worldObj.isRemote) System.out.println("reading from NBT, got a packet?");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt){
        super.writeToNBT(nbt);
        nbt.setInteger("redstoneMode", redstoneMode);

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
        nbt.setTag("Items", tagList);
    }

    @Override
    public void updateEntity(){
        super.updateEntity();
        if(!worldObj.isRemote) {
            ticksExisted++;
            if(getPressure(ForgeDirection.UNKNOWN) >= PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX && inventory[0] != null && inventory[0].getItem() == Itemss.emptyPCB && inventory[0].getItemDamage() > 0) {

                addAir((int)(-PneumaticValues.USAGE_UV_LIGHTBOX * getSpeedUsageMultiplierFromUpgrades(getUpgradeSlots())), ForgeDirection.UNKNOWN);
                if(ticksExisted % Math.max(1, (int)(TileEntityConstants.LIGHT_BOX_0_100_TIME / (5 * getSpeedMultiplierFromUpgrades(getUpgradeSlots())))) == 0) {
                    if(!areLightsOn) {
                        areLightsOn = true;
                        updateNeighbours();
                    }
                    inventory[0].setItemDamage(Math.max(0, inventory[0].getItemDamage() - 1));
                }
            } else if(areLightsOn) {
                areLightsOn = false;
                updateNeighbours();
            }
            if(oldRedstoneStatus != shouldEmitRedstone()) {
                oldRedstoneStatus = !oldRedstoneStatus;
                updateNeighbours();
            }
        }
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate(){
        return true;
    }

    @Override
    public void onNeighborTileUpdate(){
        super.onNeighborTileUpdate();
        updateConnections();
    }

    public int getLightLevel(){
        return areLightsOn ? Math.min(5, getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE) * 2) + 10 : 0;
    }

    // used in the air dispersion methods.
    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return side != ForgeDirection.UP && side != ForgeDirection.getOrientation(getBlockMetadata()) && side != ForgeDirection.getOrientation(getBlockMetadata()).getOpposite();
    }

    public void updateConnections(){
        for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            if(isConnectedTo(direction)) {
                boolean checkingLeft = ForgeDirection.getOrientation(getBlockMetadata()).getRotation(ForgeDirection.UP) == direction;
                TileEntity te = worldObj.getTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
                if(te instanceof IPneumaticMachine) {
                    /*sidesConnected[direction.ordinal()]*/
                    boolean isConnected = ((IPneumaticMachine)te).isConnectedTo(direction.getOpposite());
                    if(checkingLeft) leftConnected = isConnected;
                    else rightConnected = isConnected;
                } else {
                    /*sidesConnected[direction.ordinal()]*/
                    if(checkingLeft) leftConnected = false;
                    else rightConnected = false;
                }
            }
        }
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
        inventory[slot] = itemStack;
        if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName(){
        return Blockss.uvLightBox.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        return i == 0 || itemstack != null && itemstack.getItem() == Itemss.machineUpgrade;
    }

    @Override
    // upgrades in bottom, fuel in the rest.
    public int[] getAccessibleSlotsFromSide(int var1){
        return new int[]{0};
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, int j){
        return true;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, int j){
        return true;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 4) redstoneMode = 0;
            updateNeighbours();
        }
    }

    public boolean shouldEmitRedstone(){
        if(redstoneMode == 0 || inventory[0] == null || inventory[0].getItem() != Itemss.emptyPCB) return false;
        switch(redstoneMode){
            case 1:
                return inventory[0].getItemDamage() < 30;
            case 2:
                return inventory[0].getItemDamage() < 20;
            case 3:
                return inventory[0].getItemDamage() < 10;
            case 4:
                return inventory[0].getItemDamage() == 0;
        }
        return false;
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
    public void openInventory(){}

    @Override
    public void closeInventory(){}

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }

    @Override
    public float getMinWorkingPressure(){
        return PneumaticValues.MIN_PRESSURE_UV_LIGHTBOX;
    }
}
