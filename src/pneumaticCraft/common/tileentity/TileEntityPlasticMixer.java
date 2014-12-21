package pneumaticCraft.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.oredict.OreDictionary;
import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.fluid.FluidPlastic;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityPlasticMixer extends TileEntityBase implements IFluidHandler, IInventory, IHeatExchanger{
    private final FluidTank tank = new FluidTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    private final ItemStack[] inventory = new ItemStack[6];
    private int lastTickInventoryStacksize;
    private static int BASE_TEMPERATURE = FluidRegistry.WATER.getTemperature();
    @GuiSynced
    private final IHeatExchangerLogic hullLogic = PneumaticRegistry.getInstance().getHeatExchangerLogic();
    @GuiSynced
    private final IHeatExchangerLogic itemLogic = PneumaticRegistry.getInstance().getHeatExchangerLogic();
    @GuiSynced
    private final IHeatExchangerLogic liquidLogic = PneumaticRegistry.getInstance().getHeatExchangerLogic();

    private static final String[] DYES = {"dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite"};

    private static final int INV_ITEM = 4, INV_DYE = 5;

    public TileEntityPlasticMixer(){
        super(0, 1, 2, 3);
        hullLogic.addConnectedExchanger(itemLogic);
        hullLogic.addConnectedExchanger(liquidLogic);
        itemLogic.addConnectedExchanger(liquidLogic);

        hullLogic.setThermalCapacity(100);
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

            itemLogic.update();
            liquidLogic.update();
            if(tank.getFluid() != null) {
                //  tank.getFluid().tag.setInteger("temperature", 500/*(int)liquidLogic.getTemperature()*/);
            }

            if(worldObj.getWorldTime() % 20 == 0) {//We don't need to run _that_ often.
                if(inventory[INV_ITEM] != null && inventory[INV_ITEM].stackSize > lastTickInventoryStacksize) {
                    int stackIncrease = inventory[INV_ITEM].stackSize - lastTickInventoryStacksize;
                    double ratio = (double)inventory[INV_ITEM].stackSize / (inventory[INV_ITEM].stackSize + stackIncrease);
                    itemLogic.setTemperature((int)(ratio * itemLogic.getTemperature() + (1 - ratio) * BASE_TEMPERATURE));
                } else if(inventory[INV_ITEM] == null) {
                    itemLogic.setTemperature(BASE_TEMPERATURE);
                }

                if(itemLogic.getTemperature() >= PneumaticValues.PLASTIC_MIXER_MELTING_TEMP) {
                    NBTTagCompound tag = new NBTTagCompound();
                    //tag.setInteger("temperature", (int)itemLogic.getTemperature());
                    tag.setInteger("color", ItemDye.field_150922_c[inventory[INV_ITEM].getItemDamage()]);
                    FluidStack moltenPlastic = new FluidStack(Fluids.plastic.getID(), inventory[INV_ITEM].stackSize * 1000, tag);
                    int maxFill = fill(ForgeDirection.UNKNOWN, moltenPlastic, false) / 1000;
                    if(maxFill > 0) {
                        inventory[INV_ITEM].stackSize -= maxFill;
                        if(inventory[INV_ITEM].stackSize <= 0) inventory[INV_ITEM] = null;
                        int oldAmount = tank.getFluidAmount();
                        fill(ForgeDirection.UNKNOWN, new FluidStack(moltenPlastic, maxFill * 1000), true);
                        double ratio = (double)oldAmount / (oldAmount + maxFill * 1000);

                        liquidLogic.setTemperature(ratio * liquidLogic.getTemperature() + (1 - ratio) * FluidPlastic.getTemperatureS(tank.getFluid()));
                    }
                }
                if(tank.getFluid() != null && liquidLogic.getTemperature() < PneumaticValues.PLASTIC_MIXER_MELTING_TEMP) {
                    ItemStack solidifiedStack = new ItemStack(Itemss.plastic, tank.getFluid().amount / 1000, FluidPlastic.getPlasticMeta(tank.getFluid()));
                    if(solidifiedStack.stackSize > 0) {
                        if(inventory[INV_ITEM] == null) {
                            inventory[INV_ITEM] = solidifiedStack;
                            itemLogic.setTemperature(liquidLogic.getTemperature());
                            tank.drain(inventory[INV_ITEM].stackSize * 1000, true);
                            sendDescriptionPacket();
                        } else if(solidifiedStack.isItemEqual(inventory[INV_ITEM])) {
                            int solidifiedItems = Math.min(64 - inventory[INV_ITEM].stackSize, solidifiedStack.stackSize);
                            double ratio = (double)inventory[INV_ITEM].stackSize / (inventory[INV_ITEM].stackSize + solidifiedItems);
                            itemLogic.setTemperature((int)(ratio * itemLogic.getTemperature() + (1 - ratio) * liquidLogic.getTemperature()));
                            inventory[INV_ITEM].stackSize += solidifiedItems;
                            tank.drain(solidifiedItems * 1000, true);
                            sendDescriptionPacket();
                        }
                    }
                } else if(tank.getFluid() != null && inventory[INV_DYE] != null) {
                    while(inventory[INV_DYE] != null) {
                        FluidPlastic.addDye(tank.getFluid(), getDyeIndex());
                        inventory[INV_DYE].stackSize--;
                        if(inventory[INV_DYE].stackSize <= 0) inventory[INV_DYE] = null;
                    }
                    sendDescriptionPacket();
                }
                lastTickInventoryStacksize = inventory[INV_ITEM] != null ? inventory[INV_ITEM].stackSize : 0;

                itemLogic.setThermalCapacity(inventory[INV_ITEM] == null ? 0 : inventory[INV_ITEM].stackSize);
                liquidLogic.setThermalCapacity(tank.getFluid() == null ? 0 : tank.getFluid().amount / 1000D);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        readInventoryFromNBT(tag, inventory, "Items");
        lastTickInventoryStacksize = tag.getInteger("lastTickInventoryStacksize");

        itemLogic.readFromNBT(tag.getCompoundTag("itemLogic"));
        liquidLogic.readFromNBT(tag.getCompoundTag("liquidLogic"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        writeInventoryToNBT(tag, inventory, "Items");
        tag.setInteger("lastTickInventoryStacksize", lastTickInventoryStacksize);

        NBTTagCompound heatTag = new NBTTagCompound();
        itemLogic.writeToNBT(heatTag);
        tag.setTag("itemLogic", heatTag);

        heatTag = new NBTTagCompound();
        liquidLogic.writeToNBT(heatTag);
        tag.setTag("liquidLogic", heatTag);
    }

    @Override
    public void readFromPacket(NBTTagCompound tag){
        super.readFromPacket(tag);
        tank.setFluid(null);
        tank.readFromNBT(tag.getCompoundTag("fluid"));
    }

    @Override
    public void writeToPacket(NBTTagCompound tag){
        super.writeToPacket(tag);
        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        tag.setTag("fluid", tankTag);
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
        if(resource == null || resource.getFluid() == Fluids.plastic) return drain(from, PneumaticValues.MAX_DRAIN, doDrain);
        else return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain){
        FluidStack drained = tank.drain(Math.min(PneumaticValues.MAX_DRAIN, maxDrain), doDrain);
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
        return Blockss.plasticMixer.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        return itemstack != null && (i < 4 && itemstack.getItem() == Itemss.machineUpgrade || i == INV_ITEM && itemstack.getItem() == Itemss.plastic || i == INV_DYE && getDyeIndex(itemstack) >= 0);
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

    private int getDyeIndex(){
        return getDyeIndex(inventory[INV_DYE]);
    }

    public static int getDyeIndex(ItemStack stack){
        int[] ids = OreDictionary.getOreIDs(stack);
        for(int id : ids) {
            String name = OreDictionary.getOreName(id);
            for(int i = 0; i < DYES.length; i++) {
                if(DYES[i].equals(name)) return i;
            }
        }
        return -1;
    }
}
