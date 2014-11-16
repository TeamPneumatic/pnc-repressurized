package pneumaticCraft.common.thirdparty.buildcraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.Config;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.network.LazySynced;
import pneumaticCraft.common.tileentity.IMinWorkingPressure;
import pneumaticCraft.common.tileentity.IRedstoneControlled;
import pneumaticCraft.common.tileentity.TileEntityPneumaticBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.PneumaticValues;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

public class TileEntityPneumaticEngine extends TileEntityPneumaticBase implements IPowerReceptor, IPowerEmitter,
        IInventory, IRedstoneControlled, IMinWorkingPressure{
    public enum EnergyStage{
        BLUE, GREEN, YELLOW, RED, OVERHEAT
    }

    public static final float MIN_HEAT = 20;
    public static final float IDEAL_HEAT = 100;
    public static final float DANGER_HEAT = 175;
    public static final float MAX_HEAT = 250;
    public static final float CRITICAL_HEAT = 300;

    @GuiSynced
    public float energy;
    @DescSynced
    public EnergyStage lastEnergyStage = EnergyStage.BLUE;

    private ItemStack[] inventory;

    private final int INVENTORY_SIZE = 4;

    public static final int UPGRADE_SLOT_START = 0;
    public static final int UPGRADE_SLOT_END = 3;

    @GuiSynced
    public int redstoneMode = 0;

    public float oldCilinderProgress;
    @DescSynced
    @LazySynced
    public float cilinderProgress = PneumaticCraftUtils.sin.length * 3 / 4;
    @DescSynced
    private boolean isPumping;
    private float cilinderSpeed;

    private final PowerHandler powerHandler;

    public TileEntityPneumaticEngine(){
        super(PneumaticValues.DANGER_PRESSURE_PNEUMATIC_ENGINE, PneumaticValues.MAX_PRESSURE_PNEUMATIC_ENGINE, PneumaticValues.VOLUME_PNEUMATIC_ENGINE);
        inventory = new ItemStack[INVENTORY_SIZE];

        powerHandler = new PowerHandler(this, Type.ENGINE);
        powerHandler.configure(1.5F, 300, 10, 1000);
        powerHandler.configurePowerPerdition(1, 100);

        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 1, 2, UPGRADE_SLOT_END});
    }

    @Override
    public void updateEntity(){
        oldCilinderProgress = cilinderProgress;
        if(!worldObj.isRemote && !isPumping && redstoneAllows() && getPressure(ForgeDirection.UNKNOWN) >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_ENGINE) {
            isPumping = true;
        }
        if(isPumping) {
            if(cilinderSpeed < getCilinderSpeed()) {
                cilinderSpeed = Math.min(cilinderSpeed + 0.2F, getCilinderSpeed());
            } else {
                cilinderSpeed = Math.max(cilinderSpeed - 0.2F, getCilinderSpeed());
            }
            cilinderProgress += cilinderSpeed;
            if(cilinderProgress >= PneumaticCraftUtils.sin.length) {
                if(!worldObj.isRemote) createPower();
                cilinderProgress -= PneumaticCraftUtils.sin.length;
                oldCilinderProgress -= PneumaticCraftUtils.sin.length;
            }
            if(!worldObj.isRemote && (int)cilinderProgress == PneumaticCraftUtils.sin.length * 3 / 4 && cilinderSpeed < 2 && (!redstoneAllows() || getPressure(ForgeDirection.UNKNOWN) < PneumaticValues.MIN_PRESSURE_PNEUMATIC_ENGINE)) {
                isPumping = false;
                cilinderSpeed = 0;
            }
        }
        if(!worldObj.isRemote) {
            sendPower();
            lastEnergyStage = getEnergyStage();
        }

        super.updateEntity();

    }

    private void createPower(){
        energy += PneumaticValues.PRODUCTION_PNEUMATIC_ENGINE;
        int efficiency = Config.pneumaticEngineEfficiency;
        int airUsage = (int)(PneumaticValues.PRODUCTION_PNEUMATIC_ENGINE * 11.291F / (efficiency / 100F));
        addAir(-airUsage, ForgeDirection.UNKNOWN);
    }

    private float getCilinderSpeed(){
        if(!redstoneAllows() || getPressure(ForgeDirection.UNKNOWN) < PneumaticValues.MIN_PRESSURE_PNEUMATIC_ENGINE) return 1;
        switch(getEnergyStage()){
            case BLUE:
                return 20;
            case GREEN:
                return 30;
            case YELLOW:
                return 40;
            default:
                return 60;
        }
    }

    public float getCurrentMJProduction(){
        if(cilinderSpeed == 0) return 0;
        float burstFrequency = PneumaticCraftUtils.sin.length / cilinderSpeed;
        return 1 / burstFrequency * PneumaticValues.PRODUCTION_PNEUMATIC_ENGINE;
    }

    public float getCurrentAirUsage(){
        if(cilinderSpeed == 0) return 0;
        int efficiency = Config.pneumaticEngineEfficiency;
        float burstFrequency = PneumaticCraftUtils.sin.length / cilinderSpeed;
        return 1 / burstFrequency * PneumaticValues.PRODUCTION_PNEUMATIC_ENGINE * 11.291F / (efficiency / 100F);
    }

    public double getPowerNetEnergy(){
        return powerHandler.getEnergyStored();
    }

    private ForgeDirection getOrientation(){
        return ForgeDirection.getOrientation(getBlockMetadata());
    }

    private double getPowerToExtract(){
        ForgeDirection o = getOrientation().getOpposite();
        TileEntity tile = worldObj.getTileEntity(xCoord + o.offsetX, yCoord + o.offsetY, zCoord + o.offsetZ);
        PowerReceiver receptor = ((IPowerReceptor)tile).getPowerReceiver(o.getOpposite());
        return extractEnergy(receptor.getMinEnergyReceived(), receptor.getMaxEnergyReceived(), false);
    }

    private void sendPower(){
        ForgeDirection o = getOrientation().getOpposite();
        TileEntity tile = worldObj.getTileEntity(xCoord + o.offsetX, yCoord + o.offsetY, zCoord + o.offsetZ);
        if(isPoweredTile(tile, o)) {
            PowerReceiver receptor = ((IPowerReceptor)tile).getPowerReceiver(o.getOpposite());

            double extracted = getPowerToExtract();
            if(extracted > 0) {
                double needed = receptor.receiveEnergy(PowerHandler.Type.ENGINE, extracted, o.getOpposite());
                extractEnergy(receptor.getMinEnergyReceived(), needed, true);
            }
        }
    }

    public double extractEnergy(double d, double e, boolean doExtract){
        if(energy < d) return 0;

        double actualMax;

        if(e > maxEnergyExtracted()) actualMax = maxEnergyExtracted();
        else actualMax = e;

        if(actualMax < d) return 0;

        double extracted;

        if(energy >= actualMax) {
            extracted = actualMax;
            if(doExtract) energy -= actualMax;
        } else {
            extracted = energy;
            if(doExtract) energy = 0;
        }

        return extracted;
    }

    private float maxEnergyExtracted(){
        return 10;
    }

    public boolean isPoweredTile(TileEntity tile, ForgeDirection side){
        if(tile instanceof IPowerReceptor) return ((IPowerReceptor)tile).getPowerReceiver(side.getOpposite()) != null;

        return false;
    }

    @Override
    public boolean canEmitPowerFrom(ForgeDirection side){
        return side == getOrientation().getOpposite();
    }

    public EnergyStage getEnergyStage(){
        float pressure = getPressure(ForgeDirection.UNKNOWN);
        if(pressure < 10) return EnergyStage.BLUE;
        else if(pressure < 15) return EnergyStage.GREEN;
        else if(pressure < DANGER_PRESSURE) return EnergyStage.YELLOW;
        else if(pressure < CRITICAL_PRESSURE) return EnergyStage.RED;
        else return EnergyStage.OVERHEAT;
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
    public boolean isConnectedTo(ForgeDirection side){
        return getOrientation() == side;
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

        return BuildCraft.pneumaticEngine.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound){

        super.readFromNBT(nbtTagCompound);

        redstoneMode = nbtTagCompound.getInteger("redstoneMode");
        cilinderProgress = nbtTagCompound.getFloat("cilinderProgress");
        cilinderSpeed = nbtTagCompound.getFloat("cilinderSpeed");
        energy = nbtTagCompound.getFloat("energy");
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
        nbtTagCompound.setFloat("cilinderProgress", cilinderProgress);
        nbtTagCompound.setFloat("cilinderSpeed", cilinderSpeed);
        nbtTagCompound.setFloat("energy", energy);
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
        return ForgeDirection.getOrientation(getBlockMetadata()).getOpposite() == side ? powerHandler.getPowerReceiver() : null;
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
        return PneumaticValues.MIN_PRESSURE_PNEUMATIC_ENGINE;
    }
}
