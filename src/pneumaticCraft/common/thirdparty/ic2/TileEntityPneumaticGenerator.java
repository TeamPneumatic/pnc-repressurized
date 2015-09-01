package pneumaticCraft.common.thirdparty.ic2;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.tile.IWrenchable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.tileentity.IMinWorkingPressure;
import pneumaticCraft.common.tileentity.IRedstoneControlled;
import pneumaticCraft.common.tileentity.TileEntityAdvancedAirCompressor;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.lib.PneumaticValues;

public class TileEntityPneumaticGenerator extends TileEntityPneumaticBase implements IEnergySource, IWrenchable,
        IInventory, IRedstoneControlled, IMinWorkingPressure, IHeatExchanger{

    private ItemStack[] inventory;

    private final int INVENTORY_SIZE = 4;

    public static final int UPGRADE_SLOT_START = 0;
    public static final int UPGRADE_SLOT_END = 3;
    public boolean outputting;//true when fully dispersed all the EU's it can possibly do.
    @GuiSynced
    public int curEnergyProduction;
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatExchangerLogic();

    @GuiSynced
    public int redstoneMode = 0;

    public TileEntityPneumaticGenerator(){
        super(PneumaticValues.DANGER_PRESSURE_PNEUMATIC_GENERATOR, PneumaticValues.MAX_PRESSURE_PNEUMATIC_GENERATOR, PneumaticValues.VOLUME_PNEUMATIC_GENERATOR);
        inventory = new ItemStack[INVENTORY_SIZE];
        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 1, 2, UPGRADE_SLOT_END});
        heatExchanger.setThermalCapacity(100);
    }

    public int getEfficiency(){
        return TileEntityAdvancedAirCompressor.getEfficiency(heatExchanger.getTemperature());
    }

    @Override
    public void updateEntity(){
        super.updateEntity();
        if(!worldObj.isRemote) {
            if(outputting) {
                outputting = false;
            } else {
                curEnergyProduction = 0;
            }
        }

    }

    @Override
    protected void onFirstServerUpdate(){
        MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
    }

    @Override
    public void invalidate(){
        if(worldObj != null && !worldObj.isRemote) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload(){
        if(worldObj != null && !worldObj.isRemote) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
        }
        super.onChunkUnload();
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

    @Override
    protected void disperseAir(){
        super.disperseAir();
        /*  Map<ForgeDirection, IPneumaticMachine> teList = getConnectedPneumatics();
          if(teList.size() == 0) airLeak(ForgeDirection.getOrientation(getBlockMetadata()));*/
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return ForgeDirection.getOrientation(getBlockMetadata()) == side;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 2) redstoneMode = 0;
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

        return IC2.pneumaticGenerator.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound){

        super.readFromNBT(nbtTagCompound);

        redstoneMode = nbtTagCompound.getInteger("redstoneMode");
        outputting = nbtTagCompound.getBoolean("outputting");
        curEnergyProduction = nbtTagCompound.getInteger("energyProduction");
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
        nbtTagCompound.setInteger("redstoneMode", redstoneMode);
        nbtTagCompound.setBoolean("outputting", outputting);
        nbtTagCompound.setInteger("energyProduction", curEnergyProduction);

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
        return itemstack != null && itemstack.getItem() == Itemss.machineUpgrade;
    }

    /*
     * IC2 Methods
     */

    @Override
    public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction){
        ForgeDirection facing = ForgeDirection.getOrientation(getBlockMetadata()).getOpposite();
        return direction == facing;
    }

    @Override
    public double getOfferedEnergy(){
        return getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.MIN_PRESSURE_PNEUMATIC_GENERATOR && redstoneAllows() ? getEnergyPacketSize() : 0;
    }

    @Override
    public void drawEnergy(double amount){
        int efficiency = Config.pneumaticGeneratorEfficiency;
        if(efficiency < 1) efficiency = 1;
        int airUsage = (int)(amount / 0.25F * 100F / efficiency);
        addAir(-airUsage, ForgeDirection.UNKNOWN);
        heatExchanger.addHeat(airUsage / 40);
        outputting = true;
        curEnergyProduction = (int)amount;
    }

    /**
     * Returns 32, 128 or 512 dependant on the upgrades inserted in the machine.
     * @return
     */
    public int getEnergyPacketSize(){
        int upgradesInserted = getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE, getUpgradeSlots());
        int energyAmount = 32 * (int)Math.pow(4, Math.min(3, upgradesInserted));
        return energyAmount * getEfficiency() / 100;
    }

    @Override
    public int getSourceTier(){
        return 1 + getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE, getUpgradeSlots());
    }

    @Override
    public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side){
        return side != (getBlockMetadata() ^ 1);
    }

    @Override
    public short getFacing(){
        return (short)(getBlockMetadata() ^ 1);
    }

    @Override
    public void setFacing(short facing){
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, facing ^ 1, 3);
    }

    @Override
    public boolean wrenchCanRemove(EntityPlayer entityPlayer){
        return true;
    }

    @Override
    public float getWrenchDropRate(){
        return 1;
    }

    @Override
    public ItemStack getWrenchDrop(EntityPlayer entityPlayer){
        return new ItemStack(IC2.pneumaticGenerator);
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
        return PneumaticValues.MIN_PRESSURE_PNEUMATIC_GENERATOR;
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(ForgeDirection side){
        return heatExchanger;
    }
}
