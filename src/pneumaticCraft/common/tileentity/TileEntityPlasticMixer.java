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
import pneumaticCraft.api.tileentity.IHeatAcceptor;
import pneumaticCraft.common.fluid.FluidPlastic;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Names;
import pneumaticCraft.lib.PneumaticValues;

public class TileEntityPlasticMixer extends TileEntityBase implements IFluidHandler, IInventory, IHeatAcceptor{
    private final FluidTank tank = new FluidTank(PneumaticValues.PLASTIC_MIXER_TANK_CAPACITY);
    private final ItemStack[] inventory = new ItemStack[6];
    private int lastTickInventoryStacksize;
    private int itemTemperature = BASE_TEMPERATURE;
    private static int BASE_TEMPERATURE = Fluids.plastic.getTemperature();

    public TileEntityPlasticMixer(){
        super(0, 1, 2, 3);
    }

    @Override
    public void addHeat(int amount){
        if(tank.getFluid() != null) {
            tank.getFluid().tag.setInteger("temperature", Fluids.plastic.getTemperature(tank.getFluid()) + amount * PneumaticValues.PLASTIC_MIXER_HEAT_RATIO / tank.getFluidAmount());
        }
    }

    @Override
    public void updateEntity(){
        if(!worldObj.isRemote && worldObj.getWorldTime() % 20 == 0) {//We don't need to run _that_ often.
            if(inventory[4] != null && inventory[4].stackSize > lastTickInventoryStacksize) {
                int stackIncrease = inventory[4].stackSize - lastTickInventoryStacksize;
                double ratio = (double)inventory[4].stackSize / (inventory[4].stackSize + stackIncrease);
                itemTemperature = (int)(ratio * itemTemperature + (1 - ratio) * BASE_TEMPERATURE);
                itemTemperature = 1000;
            } else if(inventory[4] == null) {
                itemTemperature = BASE_TEMPERATURE;
            }

            if(itemTemperature >= PneumaticValues.PLASTIC_MIXER_MELTING_TEMP) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("temperature", itemTemperature);
                tag.setInteger("color", ItemDye.field_150922_c[inventory[4].getItemDamage()]);
                FluidStack moltenPlastic = new FluidStack(Fluids.plastic.getID(), inventory[4].stackSize * 1000, tag);
                int maxFill = fill(ForgeDirection.UNKNOWN, moltenPlastic, false) / 1000;
                if(maxFill > 0) {
                    inventory[4].stackSize -= maxFill;
                    if(inventory[4].stackSize <= 0) inventory[4] = null;
                    fill(ForgeDirection.UNKNOWN, new FluidStack(moltenPlastic, maxFill * 1000), true);
                }
            }
            if(tank.getFluid() != null && Fluids.plastic.getTemperature(tank.getFluid()) < PneumaticValues.PLASTIC_MIXER_MELTING_TEMP) {
                ItemStack solidifiedStack = new ItemStack(Itemss.plastic, tank.getFluid().amount / 1000, FluidPlastic.getPlasticMeta(tank.getFluid()));
                if(inventory[4] == null) {
                    inventory[4] = solidifiedStack;
                    itemTemperature = Fluids.plastic.getTemperature(tank.getFluid());
                    drain(ForgeDirection.UNKNOWN, inventory[4].stackSize * 1000, true);
                } else if(solidifiedStack.isItemEqual(inventory[4])) {
                    int solidifiedItems = Math.min(64 - inventory[4].stackSize, solidifiedStack.stackSize);
                    double ratio = (double)inventory[4].stackSize / (inventory[4].stackSize + solidifiedItems);
                    itemTemperature = (int)(ratio * itemTemperature + (1 - ratio) * FluidPlastic.getTemperatureS(tank.getFluid()));
                    inventory[4].stackSize += solidifiedItems;
                    drain(ForgeDirection.UNKNOWN, solidifiedItems * 1000, true);
                }
            }
            lastTickInventoryStacksize = inventory[4] != null ? inventory[4].stackSize : 0;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        itemTemperature = tag.getInteger("itemTemperature");
        loadInventory(inventory, tag, "Items");
        tank.readFromNBT(tag.getCompoundTag("fluid"));
        lastTickInventoryStacksize = tag.getInteger("lastTickInventoryStacksize");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setInteger("itemTemperature", itemTemperature);
        saveInventory(inventory, tag, "Items");
        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        tag.setTag("fluid", tankTag);
        tag.setInteger("lastTickInventoryStacksize", lastTickInventoryStacksize);
    }

    /******************* Tank methods *******************/

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill){
        if(resource == null || resource.getFluid() != Fluids.plastic) return 0;
        int fillingAmount = Math.min(tank.getCapacity() - tank.getFluidAmount(), resource.amount);
        if(doFill) {
            tank.setFluid(FluidPlastic.mixFluid(tank.getFluid(), new FluidStack(resource, fillingAmount)));
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
        return Names.PLASTIC_MIXER;
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
        return true;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1){
        return isGuiUseableByPlayer(var1);
    }
}
