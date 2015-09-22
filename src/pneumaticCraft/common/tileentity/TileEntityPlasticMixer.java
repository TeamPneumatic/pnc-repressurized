package pneumaticCraft.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
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
import pneumaticCraft.common.AchievementHandler;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.fluid.FluidPlastic;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.thirdparty.computercraft.LuaMethod;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityPlasticMixer extends TileEntityBase implements IFluidHandler, ISidedInventory, IHeatExchanger,
        IRedstoneControlled{
    private final FluidTank tank = new FluidTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    private final ItemStack[] inventory = new ItemStack[9];
    private int lastTickInventoryStacksize;
    private static int BASE_TEMPERATURE = FluidRegistry.WATER.getTemperature();
    @GuiSynced
    private final IHeatExchangerLogic hullLogic = PneumaticRegistry.getInstance().getHeatExchangerLogic();
    @GuiSynced
    private final IHeatExchangerLogic itemLogic = PneumaticRegistry.getInstance().getHeatExchangerLogic();
    @GuiSynced
    public int selectedPlastic = -1;
    @GuiSynced
    private int redstoneMode;
    @GuiSynced
    public boolean lockSelection;
    @GuiSynced
    public int[] dyeBuffers = new int[3];
    public static final int DYE_PER_DYE = 0xFF * 10;
    public static final int DYE_BUFFER_MAX = 0xFF * 2 * PneumaticValues.NORMAL_TANK_CAPACITY / 1000;

    public static final String[] DYES = {"dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite"};
    private static final int[] SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    public static final int INV_INPUT = 4, INV_OUTPUT = 5, INV_DYE_RED = 6, INV_DYE_GREEN = 7, INV_DYE_BLUE = 8;

    public TileEntityPlasticMixer(){
        super(0, 1, 2, 3);
        hullLogic.addConnectedExchanger(itemLogic);
        hullLogic.setThermalCapacity(100);
    }

    @SideOnly(Side.CLIENT)
    public IHeatExchangerLogic getLogic(int index){
        switch(index){
            case 0:
                return hullLogic;
            case 1:
                return itemLogic;
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
            refillDyeBuffers();
            itemLogic.update();
            if(worldObj.getTotalWorldTime() % 20 == 0) {//We don't need to run _that_ often.
                if(inventory[INV_INPUT] != null && inventory[INV_INPUT].stackSize > lastTickInventoryStacksize) {
                    int stackIncrease = inventory[INV_INPUT].stackSize - lastTickInventoryStacksize;
                    double ratio = (double)inventory[INV_INPUT].stackSize / (inventory[INV_INPUT].stackSize + stackIncrease);
                    itemLogic.setTemperature((int)(ratio * itemLogic.getTemperature() + (1 - ratio) * BASE_TEMPERATURE));
                } else if(inventory[INV_INPUT] == null) {
                    itemLogic.setTemperature(BASE_TEMPERATURE);
                }

                if(itemLogic.getTemperature() >= PneumaticValues.PLASTIC_MIXER_MELTING_TEMP) {
                    FluidStack moltenPlastic = new FluidStack(Fluids.plastic, inventory[INV_INPUT].stackSize * 1000);
                    int maxFill = fill(ForgeDirection.UNKNOWN, moltenPlastic, false) / 1000;
                    if(maxFill > 0) {
                        inventory[INV_INPUT].stackSize -= maxFill;
                        if(inventory[INV_INPUT].stackSize <= 0) inventory[INV_INPUT] = null;
                        fill(ForgeDirection.UNKNOWN, new FluidStack(moltenPlastic, maxFill * 1000), true);
                    }
                }

                lastTickInventoryStacksize = inventory[INV_INPUT] != null ? inventory[INV_INPUT].stackSize : 0;

                itemLogic.setThermalCapacity(inventory[INV_INPUT] == null ? 0 : inventory[INV_INPUT].stackSize);
            }
            if(tank.getFluid() != null && selectedPlastic >= 0 && redstoneAllows()) {
                ItemStack solidifiedStack = new ItemStack(Itemss.plastic, tank.getFluid().amount / 1000, selectedPlastic);
                if(solidifiedStack.stackSize > 0) {
                    solidifiedStack.stackSize = 1;
                    if(inventory[INV_OUTPUT] == null) {
                        solidifiedStack.stackSize = useDye(solidifiedStack.stackSize);
                        if(solidifiedStack.stackSize > 0) {
                            inventory[INV_OUTPUT] = solidifiedStack;
                            tank.drain(inventory[INV_OUTPUT].stackSize * 1000, true);
                            sendDescriptionPacket();
                        }
                    } else if(solidifiedStack.isItemEqual(inventory[INV_OUTPUT])) {
                        int solidifiedItems = Math.min(64 - inventory[INV_OUTPUT].stackSize, solidifiedStack.stackSize);
                        solidifiedItems = useDye(solidifiedItems);
                        inventory[INV_OUTPUT].stackSize += solidifiedItems;
                        tank.drain(solidifiedItems * 1000, true);
                        sendDescriptionPacket();
                    }
                }
            }
            if(!lockSelection) selectedPlastic = -1;
            if(redstoneMode == 3) selectedPlastic = poweredRedstone;
        }
    }

    private void refillDyeBuffers(){
        for(int i = 0; i < 3; i++) {
            if(getStackInSlot(INV_DYE_RED + i) != null && dyeBuffers[i] <= DYE_BUFFER_MAX - DYE_PER_DYE) {
                decrStackSize(INV_DYE_RED + i, 1);
                dyeBuffers[i] += DYE_PER_DYE;
            }
        }
    }

    private int useDye(int maxItems){
        int desiredColor = ItemDye.field_150922_c[selectedPlastic];
        if(selectedPlastic == 15) return maxItems;//Converting to white plastic is free.
        for(int i = 0; i < 3; i++) {
            int colorComponent = desiredColor >> 8 * i & 0xFF;
            colorComponent = 0xFF - colorComponent;//Invert, because we start out with white, and we darken the plastic.
            if(colorComponent > 0) {
                maxItems = Math.min(maxItems, dyeBuffers[i] / colorComponent);
            }
        }
        for(int i = 0; i < 3; i++) {
            int colorComponent = desiredColor >> 8 * i & 0xFF;
            colorComponent = 0xFF - colorComponent;//Invert, because we start out with white, and we darken the plastic.
            dyeBuffers[i] -= colorComponent * maxItems;
        }
        return maxItems;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        readInventoryFromNBT(tag, inventory, "Items");
        lastTickInventoryStacksize = tag.getInteger("lastTickInventoryStacksize");
        selectedPlastic = tag.getInteger("selectedPlastic");
        lockSelection = tag.getBoolean("lockSelection");
        dyeBuffers[0] = tag.getInteger("dyeBuffer0");
        dyeBuffers[1] = tag.getInteger("dyeBuffer1");
        dyeBuffers[2] = tag.getInteger("dyeBuffer2");
        redstoneMode = tag.getInteger("redstoneMode");

        itemLogic.readFromNBT(tag.getCompoundTag("itemLogic"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        writeInventoryToNBT(tag, inventory, "Items");
        tag.setInteger("lastTickInventoryStacksize", lastTickInventoryStacksize);
        tag.setInteger("selectedPlastic", selectedPlastic);
        tag.setBoolean("lockSelection", lockSelection);
        tag.setInteger("dyeBuffer0", dyeBuffers[0]);
        tag.setInteger("dyeBuffer1", dyeBuffers[1]);
        tag.setInteger("dyeBuffer2", dyeBuffers[2]);
        tag.setInteger("redstoneMode", redstoneMode);

        NBTTagCompound heatTag = new NBTTagCompound();
        itemLogic.writeToNBT(heatTag);
        tag.setTag("itemLogic", heatTag);
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
        if(resource == null || !Fluids.areFluidsEqual(resource.getFluid(), Fluids.plastic)) return 0;
        int fillingAmount = Math.min(tank.getCapacity() - tank.getFluidAmount(), resource.amount);
        if(doFill && fillingAmount > 0) {
            tank.setFluid(FluidPlastic.mixFluid(tank.getFluid(), new FluidStack(resource, fillingAmount)));
            sendDescriptionPacket();
        }
        return fillingAmount;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain){
        if(resource == null || Fluids.areFluidsEqual(resource.getFluid(), Fluids.plastic)) return drain(from, PneumaticValues.MAX_DRAIN, doDrain);
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
        return Fluids.areFluidsEqual(fluid, Fluids.plastic);
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
        if(itemstack == null) return true;
        switch(i){
            case INV_INPUT:
                return itemstack.getItem() == Itemss.plastic;
            case INV_OUTPUT:
                return false;
            case INV_DYE_RED:
                return getDyeIndex(itemstack) == 1;
            case INV_DYE_GREEN:
                return getDyeIndex(itemstack) == 2;
            case INV_DYE_BLUE:
                return getDyeIndex(itemstack) == 4;
            default:
                return itemstack.getItem() == Itemss.machineUpgrade;
        }
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

    @Override
    public int[] getAccessibleSlotsFromSide(int side){
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side){
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side){
        return slot == INV_OUTPUT;
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player){
        super.handleGUIButtonPress(guiID, player);
        if(guiID == 0) {
            if(++redstoneMode > 3) {
                redstoneMode = 0;
            }
        } else if(guiID >= 1 && guiID < 17) {
            if(selectedPlastic != guiID) {
                selectedPlastic = guiID - 1;
                if(tank.getFluidAmount() >= 1000) {
                    AchievementHandler.giveAchievement(player, new ItemStack(Itemss.plastic));
                }
            } else {
                selectedPlastic = -1;
            }
        } else if(guiID == 17) {
            lockSelection = !lockSelection;
        }
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }

    @Override
    public boolean redstoneAllows(){
        return redstoneMode == 3 ? true : super.redstoneAllows();
    }

    @Override
    protected void addLuaMethods(){
        super.addLuaMethods();
        luaMethods.add(new LuaMethod("selectColor"){
            @Override
            public Object[] call(Object[] args) throws Exception{
                if(args.length == 1) {
                    int selection = ((Double)args[0]).intValue();
                    if(selection >= 0 && selection <= 16) {
                        selectedPlastic = selection - 1;
                        return null;
                    } else {
                        throw new IllegalArgumentException("selectColor method only accepts a value ranging from 0-16. The value passed was: " + selection);
                    }
                } else {
                    throw new IllegalArgumentException("selectColor method requires 1 argument (int color index, with 0 being no color");
                }
            }
        });
    }
}
