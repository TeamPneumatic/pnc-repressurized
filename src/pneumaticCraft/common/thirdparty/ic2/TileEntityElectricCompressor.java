package pneumaticCraft.common.thirdparty.ic2;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
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
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.tileentity.IRedstoneControlled;
import pneumaticCraft.common.tileentity.TileEntityAdvancedAirCompressor;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.PneumaticValues;

public class TileEntityElectricCompressor extends TileEntityPneumaticBase implements IEnergySink, IWrenchable,
        IInventory, IRedstoneControlled, IHeatExchanger{

    private ItemStack[] inventory;

    private final int INVENTORY_SIZE = 4;

    public static final int UPGRADE_SLOT_START = 0;
    public static final int UPGRADE_SLOT_END = 3;
    public int outputTimer;//set to 20 when receiving energy, and decreased to 0 when not. Acts as a buffer before sending packets to update the client's rotation logic.

    private boolean redstoneAllows;
    @GuiSynced
    public int redstoneMode = 0;
    private int curEnergyProduction;
    @GuiSynced
    public int lastEnergyProduction;
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatExchangerLogic();

    public float turbineRotation;
    public float oldTurbineRotation;
    public float turbineSpeed;

    public TileEntityElectricCompressor(){
        super(PneumaticValues.DANGER_PRESSURE_ELECTRIC_COMPRESSOR, PneumaticValues.MAX_PRESSURE_ELECTRIC_COMPRESSOR, PneumaticValues.VOLUME_ELECTRIC_COMPRESSOR);
        inventory = new ItemStack[INVENTORY_SIZE];
        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 1, 2, UPGRADE_SLOT_END});
        heatExchanger.setThermalCapacity(100);
    }

    public int getEfficiency(){
        return TileEntityAdvancedAirCompressor.getEfficiency(heatExchanger.getTemperature());
    }

    @Override
    public void updateEntity(){
        redstoneAllows = redstoneAllows();

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
            /*
            if(getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.MIN_PRESSURE_ELECTRIC_COMPRESSOR && redstoneAllows()) {
                int efficiency = Config.pneumaticGeneratorEfficiency;
                if(efficiency < 1) efficiency = 1;
                int energyPacketSize = getEnergyPacketSize();
                EnergyTileSourceEvent event = new EnergyTileSourceEvent(this, energyPacketSize);
                MinecraftForge.EVENT_BUS.post(event);
                outputting = event.amount == 0;
                float airUsage = (energyPacketSize - event.amount) / 0.25F * 100F / efficiency;
                addAir(-airUsage, ForgeDirection.UNKNOWN);
            } else {
                outputting = false;
            }
            */
            outputTimer--;
            if(outputTimer == 0) {
                sendDescriptionPacket();
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

        return IC2.electricCompressor.getUnlocalizedName();
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
        lastEnergyProduction = nbtTagCompound.getInteger("energyProduction");
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
        nbtTagCompound.setInteger("energyProduction", lastEnergyProduction);
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
        return itemstack != null && (itemstack.getItem() == Itemss.machineUpgrade || PneumaticCraftUtils.isIC2Upgrade(itemstack.getItem()));
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction){
        //ForgeDirection facing = ForgeDirection.getOrientation(getBlockMetadata()).getOpposite();
        return direction == ForgeDirection.UP;
    }

    @Override
    public double getDemandedEnergy(){
        return redstoneAllows ? Double.MAX_VALUE : 0;
    }

    @Override
    public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage){
        /* if(amount > getMaxSafeInput()) {
             if(!worldObj.isRemote) {
         TODO put back when IC2 is sorted out        worldObj.createExplosion(null, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 0.5F, true);
                 worldObj.setBlockToAir(xCoord, yCoord, zCoord);
             }
             return 0;
         } else {*/
        double energyUsed = amount;
        int efficiency = Config.electricCompressorEfficiency;
        int airProduction = (int)(energyUsed / 0.25F * efficiency / 100F * getEfficiency() / 100);
        heatExchanger.addHeat(energyUsed / 16);
        addAir(airProduction, ForgeDirection.UNKNOWN);
        curEnergyProduction += airProduction;
        boolean clientNeedsUpdate = outputTimer <= 0;
        outputTimer = 20;
        if(clientNeedsUpdate) sendDescriptionPacket();
        return amount - energyUsed;
        // }
    }

    public int getMaxSafeInput(){
        int upgradesInserted = getIC2Upgrades("transformerUpgrade", getUpgradeSlots());
        return 32 * (int)Math.pow(4, upgradesInserted);
    }

    @Override
    public int getSinkTier(){
        int upgradesInserted = getIC2Upgrades("transformerUpgrade", getUpgradeSlots());
        return 1 + upgradesInserted;
    }

    @Override
    public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side){
        return side != (getBlockMetadata() ^ 1) && side >= 2;
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
        return new ItemStack(IC2.electricCompressor);
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
    public IHeatExchangerLogic getHeatExchangerLogic(ForgeDirection side){
        return heatExchanger;
    }
}
