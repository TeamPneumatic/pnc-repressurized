package pneumaticCraft.common.thirdparty.buildcraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.Config;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.IRedstoneControlled;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.lib.PneumaticValues;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

public class TileEntityKineticCompressor extends TileEntityPneumaticBase implements IPowerReceptor, IInventory,
        IRedstoneControlled{

    private ItemStack[] inventory;

    private final int INVENTORY_SIZE = 4;

    public static final int UPGRADE_SLOT_START = 0;
    public static final int UPGRADE_SLOT_END = 3;
    public int outputTimer;//set to 20 when receiving energy, and decreased to 0 when not. Acts as a buffer before sending packets to update the client's rotation logic.

    public int redstoneMode = 0;
    private int curEnergyProduction;
    public int lastEnergyProduction;

    public float turbineRotation;
    public float oldTurbineRotation;
    public float turbineSpeed;
    public double energyUsed;
    private final PowerHandler powerHandler;

    public TileEntityKineticCompressor(){
        super(PneumaticValues.DANGER_PRESSURE_KINETIC_COMPRESSOR, PneumaticValues.MAX_PRESSURE_KINETIC_COMPRESSOR, PneumaticValues.VOLUME_KINETIC_COMPRESSOR);
        inventory = new ItemStack[INVENTORY_SIZE];

        powerHandler = new PowerHandler(this, Type.MACHINE);
        powerHandler.configure(1.5F, 300, 10, 1000);
        powerHandler.configurePowerPerdition(1, 1);

        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 1, 2, UPGRADE_SLOT_END});
    }

    @Override
    public void updateEntity(){
        oldTurbineRotation = turbineRotation;
        if(outputTimer > 0) {
            turbineSpeed = Math.min(turbineSpeed + 0.2F, 10);
        } else {
            turbineSpeed = Math.max(turbineSpeed - 0.2F, 0);
        }
        turbineRotation += turbineSpeed;

        if(!worldObj.isRemote) {
            lastEnergyProduction = curEnergyProduction;
            curEnergyProduction = 0;
        }
        super.updateEntity();
        if(!worldObj.isRemote) {
            if(redstoneAllows()) createAir();
            else energyUsed = 0;
            outputTimer--;
            if(outputTimer == 0) {
                sendDescriptionPacket();
            }

        }

    }

    private void createAir(){
        //600MJ == 16000mL
        energyUsed = powerHandler.useEnergy(0, Math.min(10, getPowerNetEnergy() / 20), true);
        int efficiency = Config.kineticCompressorEfficiency;
        int airProduction = (int)(energyUsed * 16F / 100F * efficiency);
        addAir(airProduction, ForgeDirection.UNKNOWN);
        curEnergyProduction += airProduction;
        if(airProduction > 1F) {
            boolean clientNeedsUpdate = outputTimer <= 0;
            outputTimer = 20;
            if(clientNeedsUpdate) sendDescriptionPacket();
        }
    }

    public double getPowerNetEnergy(){
        return powerHandler.getEnergyStored();
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
        /* Map<ForgeDirection, IPneumaticMachine> teList = getConnectedPneumatics();
         if(teList.size() == 0) airLeak(ForgeDirection.getOrientation(getBlockMetadata()));*/
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        ForgeDirection orientation = ForgeDirection.getOrientation(getBlockMetadata());
        return orientation == side || orientation == side.getOpposite();
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 2) redstoneMode = 0;
            sendDescriptionPacket();
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

        return BuildCraft.kineticCompressor.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound){

        super.readFromNBT(nbtTagCompound);

        redstoneMode = nbtTagCompound.getInteger("redstoneMode");
        outputTimer = nbtTagCompound.getBoolean("outputTimer") ? 20 : 0;
        turbineSpeed = nbtTagCompound.getFloat("turbineSpeed");
        energyUsed = nbtTagCompound.getDouble("energyUsed");
        lastEnergyProduction = nbtTagCompound.getInteger("energyProduction");
        powerHandler.readFromNBT(nbtTagCompound);
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
        nbtTagCompound.setBoolean("outputTimer", outputTimer > 0);
        nbtTagCompound.setFloat("turbineSpeed", turbineSpeed);
        nbtTagCompound.setDouble("energyUsed", energyUsed);
        nbtTagCompound.setInteger("energyProduction", lastEnergyProduction);
        powerHandler.writeToNBT(nbtTagCompound);
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

    @Override
    public void doWork(PowerHandler workProvider){}

    @Override
    public World getWorld(){
        return worldObj;
    }

    @Override
    public PowerReceiver getPowerReceiver(ForgeDirection side){
        return /*ForgeDirection.getOrientation(getBlockMetadata()).getOpposite()*/ForgeDirection.UP == side ? powerHandler.getPowerReceiver() : null;
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
}
