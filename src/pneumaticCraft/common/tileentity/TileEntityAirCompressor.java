package pneumaticCraft.common.tileentity;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityAirCompressor extends TileEntityPneumaticBase implements ISidedInventory, IRedstoneControlled{

    private ItemStack[] inventory;

    private final int INVENTORY_SIZE = 5;

    public static final int FUEL_INVENTORY_INDEX = 0;
    public static final int UPGRADE_SLOT_START = 1;
    public static final int UPGRADE_SLOT_END = 4;

    @GuiSynced
    public int burnTime;
    @GuiSynced
    public int maxBurnTime; // in here the total burn time of the current
                            // burning item is stored.
    @GuiSynced
    public int redstoneMode = 0; // determines how the compressor responds to
                                 // redstone.
    @DescSynced
    public boolean isActive;
    @GuiSynced
    public int curFuelUsage;

    public TileEntityAirCompressor(){
        this(PneumaticValues.DANGER_PRESSURE_AIR_COMPRESSOR, PneumaticValues.MAX_PRESSURE_AIR_COMPRESSOR, PneumaticValues.VOLUME_AIR_COMPRESSOR);
    }

    public TileEntityAirCompressor(float dangerPressure, float criticalPressure, int volume){
        super(dangerPressure, criticalPressure, volume);
        inventory = new ItemStack[INVENTORY_SIZE];
        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 2, 3, UPGRADE_SLOT_END});
    }

    @Override
    public void updateEntity(){
        if(!worldObj.isRemote) {
            if(burnTime < curFuelUsage && inventory[0] != null && TileEntityFurnace.isItemFuel(inventory[0]) && redstoneAllows()) {
                burnTime += TileEntityFurnace.getItemBurnTime(inventory[0]);
                maxBurnTime = burnTime;

                inventory[0].stackSize--;
                if(inventory[0].stackSize == 0) {
                    inventory[0] = inventory[0].getItem().getContainerItem(inventory[0]);
                }

            }

            curFuelUsage = (int)(getBaseProduction() * getSpeedUsageMultiplierFromUpgrades(getUpgradeSlots()) / 10);
            if(burnTime >= curFuelUsage) {
                burnTime -= curFuelUsage;
                if(!worldObj.isRemote) {
                    addAir((int)(getBaseProduction() * getSpeedMultiplierFromUpgrades(getUpgradeSlots()) * getEfficiency() / 100D), ForgeDirection.UNKNOWN);
                    onFuelBurn(curFuelUsage);
                }
            }
            isActive = burnTime > curFuelUsage;
        } else if(isActive) spawnBurningParticle();

        super.updateEntity();

    }

    protected void onFuelBurn(int burnedFuel){}

    public int getEfficiency(){
        return 100;
    }

    public int getBaseProduction(){
        return PneumaticValues.PRODUCTION_COMPRESSOR;
    }

    @Override
    public boolean redstoneAllows(){
        switch(redstoneMode){
            case 0:
                return true;
            case 1:
                return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
            case 2:
                return !worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
        }
        return false;
    }

    private void spawnBurningParticle(){
        Random rand = new Random();
        if(rand.nextInt(3) != 0) return;
        float f = xCoord + 0.5F;
        float f1 = yCoord + 0.0F + rand.nextFloat() * 6.0F / 16.0F;
        float f2 = zCoord + 0.5F;
        float f3 = 0.5F;
        float f4 = rand.nextFloat() * 0.4F - 0.2F;
        switch(ForgeDirection.getOrientation(getBlockMetadata())){
            case EAST:
                worldObj.spawnParticle("smoke", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                worldObj.spawnParticle("flame", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                break;
            case WEST:
                worldObj.spawnParticle("smoke", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                worldObj.spawnParticle("flame", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                break;
            case SOUTH:
                worldObj.spawnParticle("smoke", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
                worldObj.spawnParticle("flame", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
                break;
            case NORTH:
                worldObj.spawnParticle("smoke", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
                worldObj.spawnParticle("flame", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
                break;
        }
    }

    @Override
    protected void disperseAir(){
        super.disperseAir();
        List<Pair<ForgeDirection, IAirHandler>> teList = getConnectedPneumatics();
        if(teList.size() == 0) airLeak(ForgeDirection.getOrientation(getBlockMetadata()));
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return ForgeDirection.getOrientation(getBlockMetadata()) == side;
    }

    public int getBurnTimeRemainingScaled(int parts){
        if(maxBurnTime == 0 || burnTime < curFuelUsage) return 0;
        return parts * burnTime / maxBurnTime;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
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

        return Blockss.airCompressor.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
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
    public void readFromNBT(NBTTagCompound nbtTagCompound){

        super.readFromNBT(nbtTagCompound);
        burnTime = nbtTagCompound.getInteger("burnTime");
        maxBurnTime = nbtTagCompound.getInteger("maxBurn");
        redstoneMode = nbtTagCompound.getInteger("redstoneMode");
        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = nbtTagCompound.getTagList("Items", 10);
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
    public void writeToNBT(NBTTagCompound nbtTagCompound){

        super.writeToNBT(nbtTagCompound);

        nbtTagCompound.setInteger("burnTime", burnTime);
        nbtTagCompound.setInteger("maxBurn", maxBurnTime);
        nbtTagCompound.setInteger("redstoneMode", redstoneMode);
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
        nbtTagCompound.setTag("Items", tagList);
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
    public boolean hasCustomInventoryName(){
        return false;
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }
}
