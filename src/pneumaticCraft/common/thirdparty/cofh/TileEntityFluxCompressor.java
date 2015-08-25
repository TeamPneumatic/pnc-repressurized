package pneumaticCraft.common.thirdparty.cofh;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.tileentity.IRedstoneControlled;
import pneumaticCraft.common.tileentity.TileEntityAdvancedAirCompressor;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.lib.PneumaticValues;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyHandler;
import cofh.api.tileentity.IEnergyInfo;

public class TileEntityFluxCompressor extends TileEntityPneumaticBase implements IEnergyHandler, IEnergyInfo,
        IInventory, IRedstoneControlled, IRFConverter, IHeatExchanger{

    private final EnergyStorage energy = new EnergyStorage(100000);
    @GuiSynced
    private int rfPerTick;
    @GuiSynced
    private int airPerTick;
    @GuiSynced
    private int redstoneMode;
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatExchangerLogic();

    private final ItemStack[] inventory = new ItemStack[4];

    public TileEntityFluxCompressor(){
        super(PneumaticValues.DANGER_PRESSURE_FLUX_COMPRESSOR, PneumaticValues.MAX_PRESSURE_FLUX_COMPRESSOR, PneumaticValues.VOLUME_FLUX_COMPRESSOR);
        setUpgradeSlots(0, 1, 2, 3);
        heatExchanger.setThermalCapacity(100);
    }

    public int getEfficiency(){
        return TileEntityAdvancedAirCompressor.getEfficiency(heatExchanger.getTemperature());
    }

    @Override
    public void updateEntity(){
        super.updateEntity();

        if(!worldObj.isRemote) {
            if(worldObj.getTotalWorldTime() % 5 == 0) {
                airPerTick = (int)(40 * this.getSpeedUsageMultiplierFromUpgrades() * getEfficiency() * Config.fluxCompressorEfficiency / 100 / 100);
                rfPerTick = (int)(40 * this.getSpeedUsageMultiplierFromUpgrades());
            }
            if(redstoneAllows() && getEnergyStored(ForgeDirection.UNKNOWN) >= rfPerTick) {
                this.addAir(airPerTick, ForgeDirection.UNKNOWN);
                energy.extractEnergy(rfPerTick, false);
                heatExchanger.addHeat(rfPerTick / 100D);
            }
        }
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from){
        return from == ForgeDirection.UP;
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return side == getRotation() || side == getRotation().getOpposite();
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate){
        maxReceive = Math.min(maxReceive, getRFRate() * 2);
        return energy.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(ForgeDirection from){
        return energy.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from){
        return energy.getMaxEnergyStored();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        energy.writeToNBT(tag);
        writeInventoryToNBT(tag, inventory);
        tag.setByte("redstoneMode", (byte)redstoneMode);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        energy.readFromNBT(tag);
        readInventoryFromNBT(tag, inventory);
        redstoneMode = tag.getByte("redstoneMode");
    }

    @Override
    public int getInfoEnergyPerTick(){
        return rfPerTick;
    }

    @Override
    public int getInfoMaxEnergyPerTick(){
        return energy.getMaxExtract();
    }

    @Override
    public int getInfoEnergyStored(){
        return energy.getEnergyStored();
    }

    @Override
    public int getInfoMaxEnergyStored(){
        return energy.getMaxEnergyStored();
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate){
        return 0;
    }

    /*
     * ---------------------------IInventory
     */

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

        return CoFHCore.fluxCompressor.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
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
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack itemstack){
        return itemstack != null && itemstack.getItem() == Itemss.machineUpgrade;
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0 && ++redstoneMode > 2) redstoneMode = 0;
    }

    @Override
    public int getRFRate(){
        return rfPerTick;
    }

    @Override
    public int getAirRate(){
        return airPerTick;
    }

    @Override
    public EnergyStorage getEnergyStorage(){
        return energy;
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(ForgeDirection side){
        return heatExchanger;
    }
}
