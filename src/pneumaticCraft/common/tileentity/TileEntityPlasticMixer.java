package pneumaticCraft.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.fluid.FluidPlastic;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityPlasticMixer extends TileEntityBase implements IFluidHandler, IInventory, IHeatExchanger{
    private final FluidTank tank = new FluidTank(PneumaticValues.PLASTIC_MIXER_TANK_CAPACITY);
    private final ItemStack[] inventory = new ItemStack[6];
    private int lastTickInventoryStacksize;
    private static int BASE_TEMPERATURE = Fluids.plastic.getTemperature();
    private final IHeatExchangerLogic hullLogic = PneumaticRegistry.getInstance().getHeatExchangerLogic();
    private final IHeatExchangerLogic itemLogic = PneumaticRegistry.getInstance().getHeatExchangerLogic();
    private final IHeatExchangerLogic liquidLogic = PneumaticRegistry.getInstance().getHeatExchangerLogic();

    public TileEntityPlasticMixer(){
        super(0, 1, 2, 3);
        hullLogic.addConnectedExchanger(itemLogic);
        hullLogic.addConnectedExchanger(liquidLogic);
        itemLogic.addConnectedExchanger(liquidLogic);

        hullLogic.setThermalCapacity(100);
    }

    public int getTemperature(int index){
        switch(index){
            case 0:
                return (int)hullLogic.getTemperature();
            case 1:
                return (int)itemLogic.getTemperature();
            case 2:
                return (int)liquidLogic.getTemperature();
        }
        throw new IllegalArgumentException("Invalid index: " + index);
    }

    @SideOnly(Side.CLIENT)
    public void setTemperature(int temperature, int index){
        switch(index){
            case 0:
                hullLogic.setTemperature(temperature);
                return;
            case 1:
                itemLogic.setTemperature(temperature);
                return;
            case 2:
                liquidLogic.setTemperature(temperature);
                return;
        }
        throw new IllegalArgumentException("Invalid index: " + index);
    }

    @SideOnly(Side.CLIENT)
    public IHeatExchangerLogic getLogic(int index){
        switch(index){
            case 0:
                return hullLogic;
            case 1:
                return itemLogic;
            case 2:
                return liquidLogic;
        }
        throw new IllegalArgumentException("Invalid index: " + index);
    }

    @SideOnly(Side.CLIENT)
    public IFluidTank getFluidTank(){
        return tank;
    }

    @Override
    public void updateEntity(){
        super.updateEntity();
        if(!worldObj.isRemote) {

            hullLogic.update();
            itemLogic.update();
            liquidLogic.update();
            if(tank.getFluid() != null) {
                //  tank.getFluid().tag.setInteger("temperature", 500/*(int)liquidLogic.getTemperature()*/);
            }

            if(worldObj.getWorldTime() % 20 == 0) {//We don't need to run _that_ often.
                if(inventory[4] != null && inventory[4].stackSize > lastTickInventoryStacksize) {
                    int stackIncrease = inventory[4].stackSize - lastTickInventoryStacksize;
                    double ratio = (double)inventory[4].stackSize / (inventory[4].stackSize + stackIncrease);
                    itemLogic.setTemperature((int)(ratio * itemLogic.getTemperature() + (1 - ratio) * BASE_TEMPERATURE));
                } else if(inventory[4] == null) {
                    itemLogic.setTemperature(BASE_TEMPERATURE);
                }

                if(itemLogic.getTemperature() >= PneumaticValues.PLASTIC_MIXER_MELTING_TEMP) {
                    NBTTagCompound tag = new NBTTagCompound();
                    //tag.setInteger("temperature", (int)itemLogic.getTemperature());
                    tag.setInteger("color", ItemDye.field_150922_c[inventory[4].getItemDamage()]);
                    FluidStack moltenPlastic = new FluidStack(Fluids.plastic.getID(), inventory[4].stackSize * 1000, tag);
                    int maxFill = fill(ForgeDirection.UNKNOWN, moltenPlastic, false) / 1000;
                    if(maxFill > 0) {
                        inventory[4].stackSize -= maxFill;
                        if(inventory[4].stackSize <= 0) inventory[4] = null;
                        int oldAmount = tank.getFluidAmount();
                        fill(ForgeDirection.UNKNOWN, new FluidStack(moltenPlastic, maxFill * 1000), true);
                        double ratio = (double)oldAmount / (oldAmount + maxFill * 1000);

                        liquidLogic.setTemperature(ratio * liquidLogic.getTemperature() + (1 - ratio) * FluidPlastic.getTemperatureS(tank.getFluid()));
                    }
                }
                if(tank.getFluid() != null && liquidLogic.getTemperature() < PneumaticValues.PLASTIC_MIXER_MELTING_TEMP) {
                    ItemStack solidifiedStack = new ItemStack(Itemss.plastic, tank.getFluid().amount / 1000, FluidPlastic.getPlasticMeta(tank.getFluid()));
                    if(solidifiedStack.stackSize > 0) {
                        if(inventory[4] == null) {
                            inventory[4] = solidifiedStack;
                            itemLogic.setTemperature(liquidLogic.getTemperature());
                            tank.drain(inventory[4].stackSize * 1000, true);
                            sendDescriptionPacket();
                        } else if(solidifiedStack.isItemEqual(inventory[4])) {
                            int solidifiedItems = Math.min(64 - inventory[4].stackSize, solidifiedStack.stackSize);
                            double ratio = (double)inventory[4].stackSize / (inventory[4].stackSize + solidifiedItems);
                            itemLogic.setTemperature((int)(ratio * itemLogic.getTemperature() + (1 - ratio) * liquidLogic.getTemperature()));
                            inventory[4].stackSize += solidifiedItems;
                            tank.drain(solidifiedItems * 1000, true);
                            sendDescriptionPacket();
                        }
                    }
                } else if(tank.getFluid() != null && inventory[5] != null) {
                    while(inventory[5] != null) {
                        FluidPlastic.addDye(tank.getFluid(), inventory[5].getItemDamage());
                        inventory[5].stackSize--;
                        if(inventory[5].stackSize <= 0) inventory[5] = null;
                    }
                    sendDescriptionPacket();
                }
                lastTickInventoryStacksize = inventory[4] != null ? inventory[4].stackSize : 0;

                itemLogic.setThermalCapacity(inventory[4] == null ? 0 : inventory[4].stackSize);
                liquidLogic.setThermalCapacity(tank.getFluid() == null ? 0 : tank.getFluid().amount / 1000D);
            }
        }
    }

    @Override
    protected void onFirstServerUpdate(){
        hullLogic.initializeAsHull(worldObj, xCoord, yCoord, zCoord);
    }

    @Override
    public void onNeighborBlockUpdate(){
        onFirstServerUpdate();
    }

    @Override
    public void onNeighborTileUpdate(){
        onFirstServerUpdate();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        readInventoryFromNBT(tag, inventory, "Items");
        tank.setFluid(null);
        tank.readFromNBT(tag.getCompoundTag("fluid"));
        lastTickInventoryStacksize = tag.getInteger("lastTickInventoryStacksize");

        hullLogic.readFromNBT(tag.getCompoundTag("hullLogic"));
        itemLogic.readFromNBT(tag.getCompoundTag("itemLogic"));
        liquidLogic.readFromNBT(tag.getCompoundTag("liquidLogic"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        writeInventoryToNBT(tag, inventory, "Items");
        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        tag.setTag("fluid", tankTag);
        tag.setInteger("lastTickInventoryStacksize", lastTickInventoryStacksize);

        NBTTagCompound heatTag = new NBTTagCompound();
        hullLogic.writeToNBT(heatTag);
        tag.setTag("hullLogic", heatTag);

        heatTag = new NBTTagCompound();
        itemLogic.writeToNBT(heatTag);
        tag.setTag("itemLogic", heatTag);

        heatTag = new NBTTagCompound();
        liquidLogic.writeToNBT(heatTag);
        tag.setTag("liquidLogic", heatTag);

    }

    /******************* Tank methods *******************/

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill){
        if(resource == null || resource.getFluid() != Fluids.plastic) return 0;
        int fillingAmount = Math.min(tank.getCapacity() - tank.getFluidAmount(), resource.amount);
        if(doFill) {
            tank.setFluid(FluidPlastic.mixFluid(tank.getFluid(), new FluidStack(resource, fillingAmount)));
            liquidLogic.setTemperature(FluidPlastic.getTemperatureS(tank.getFluid()));
            sendDescriptionPacket();
        }
        return fillingAmount;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain){
        if(resource == null || resource.getFluid() == Fluids.plastic) return drain(from, PneumaticValues.PLASTIC_MIXER_MAX_DRAIN, doDrain);
        else return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain){
        FluidStack drained = tank.drain(Math.min(PneumaticValues.PLASTIC_MIXER_MAX_DRAIN, maxDrain), doDrain);
        if(doDrain && drained != null && drained.getFluid() != null) sendDescriptionPacket();
        return drained;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid){
        return fluid == Fluids.plastic;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid){
        return canFill(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from){
        return new FluidTankInfo[]{tank.getInfo()};
    }

    /****************** End Tank methods *******************/

    /****************** IInventory *********************/

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){
        return 1;
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
        return Blockss.plasticMixer.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        return itemstack != null && (i < 4 && itemstack.getItem() == Itemss.machineUpgrade || i == 4 && itemstack.getItem() == Itemss.plastic || i == 5 && itemstack.getItem() == Items.dye);
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
    public IHeatExchangerLogic getHeatExchangerLogic(ForgeDirection side){
        return hullLogic;
    }
}
