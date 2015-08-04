package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.api.tileentity.ISidedPneumaticMachine;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityVacuumPump extends TileEntityPneumaticBase implements IInventory, IRedstoneControlled{
    @GuiSynced
    private final TileEntityPneumaticBase vacuumHandler = new TileEntityPneumaticBase(5, 7, PneumaticValues.VOLUME_VACUUM_PUMP){
        @Override
        public List<Pair<ForgeDirection, IAirHandler>> getConnectedPneumatics(){
            List<Pair<ForgeDirection, IAirHandler>> teList = new ArrayList<Pair<ForgeDirection, IAirHandler>>();
            ForgeDirection direction = getVacuumSide();
            TileEntity te = getTileCache()[direction.ordinal()].getTileEntity();
            IPneumaticMachine machine = ModInteractionUtils.getInstance().getMachine(te);
            if(machine != null && isConnectedTo(direction) && machine.isConnectedTo(direction.getOpposite())) {
                teList.add(new ImmutablePair(direction, machine.getAirHandler()));
            } else if(te instanceof ISidedPneumaticMachine) {
                IAirHandler handler = ((ISidedPneumaticMachine)te).getAirHandler(direction);
                if(handler != null) {
                    teList.add(new ImmutablePair(direction, handler));
                }
            }
            return teList;
        }

        @Override
        protected boolean saveTeInternals(){
            return false;
        }
    };
    public int rotation;
    public int oldRotation;
    public int turnTimer = -1;
    @DescSynced
    public boolean turning = false;
    public int rotationSpeed;
    @GuiSynced
    public int redstoneMode;

    private ItemStack[] inventory = new ItemStack[4];
    public static final int UPGRADE_SLOT_1 = 0;
    public static final int UPGRADE_SLOT_4 = 3;

    public static final int INVENTORY_SIZE = 4;

    public TileEntityVacuumPump(){
        super(PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP, PneumaticValues.VOLUME_VACUUM_PUMP);
        setUpgradeSlots(new int[]{UPGRADE_SLOT_1, 1, 2, UPGRADE_SLOT_4});
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        switch(ForgeDirection.getOrientation(meta)){
            case NORTH:
            case SOUTH:
                return side == ForgeDirection.NORTH || side == ForgeDirection.SOUTH;
            case EAST:
            case WEST:
                return side == ForgeDirection.EAST || side == ForgeDirection.WEST;
        }
        return false;
    }

    @Override
    public List<Pair<ForgeDirection, IAirHandler>> getConnectedPneumatics(){
        List<Pair<ForgeDirection, IAirHandler>> teList = new ArrayList<Pair<ForgeDirection, IAirHandler>>();
        ForgeDirection direction = getInputSide();
        TileEntity te = getTileCache()[direction.ordinal()].getTileEntity();
        IPneumaticMachine machine = ModInteractionUtils.getInstance().getMachine(te);
        if(machine != null && isConnectedTo(direction) && machine.isConnectedTo(direction.getOpposite())) {
            teList.add(new ImmutablePair(direction, machine.getAirHandler()));
        } else if(te instanceof ISidedPneumaticMachine) {
            IAirHandler handler = ((ISidedPneumaticMachine)te).getAirHandler(direction);
            if(handler != null) {
                teList.add(new ImmutablePair(direction, handler));
            }
        }
        return teList;
    }

    @Override
    public void validate(){
        super.validate();
        vacuumHandler.validateI(this);
    }

    @Override
    public void onNeighborTileUpdate(){
        super.onNeighborTileUpdate();
        vacuumHandler.onNeighborTileUpdate();
    }

    @Override
    public void setVolume(int newVolume){
        vacuumHandler.setVolume(newVolume);
        super.setVolume(newVolume);
    }

    public ForgeDirection getInputSide(){
        return getVacuumSide().getOpposite();
    }

    public ForgeDirection getVacuumSide(){
        return ForgeDirection.getOrientation(getBlockMetadata());
    }

    @Override
    public float getPressure(ForgeDirection sideRequested){
        if(sideRequested == getVacuumSide()) {
            return vacuumHandler.getPressure(sideRequested);
        } else {
            return super.getPressure(sideRequested);
        }
    }

    @Override
    public int getCurrentAir(ForgeDirection sideRequested){
        return sideRequested == getInputSide() ? currentAir : vacuumHandler.getCurrentAir(sideRequested);
    }

    @Override
    public void updateEntity(){
        if(!worldObj.isRemote && turnTimer >= 0) turnTimer--;
        if(!worldObj.isRemote && getPressure(getInputSide()) > PneumaticValues.MIN_PRESSURE_VACUUM_PUMP && getPressure(getVacuumSide()) > -1F && redstoneAllows()) {
            if(!worldObj.isRemote && turnTimer == -1) {
                turning = true;
            }
            addAir((int)(-PneumaticValues.PRODUCTION_VACUUM_PUMP * getSpeedMultiplierFromUpgrades(getUpgradeSlots())), getVacuumSide()); // negative because it's pulling a vacuum.
            addAir((int)(-PneumaticValues.USAGE_VACUUM_PUMP * getSpeedUsageMultiplierFromUpgrades(getUpgradeSlots())), getInputSide());
            turnTimer = 40;
        }
        if(turnTimer == 0) {
            turning = false;
        }
        oldRotation = rotation;
        if(worldObj.isRemote) {
            if(turning) {
                rotationSpeed = Math.min(rotationSpeed + 1, 20);
            } else {
                rotationSpeed = Math.max(rotationSpeed - 1, 0);
            }
            rotation += rotationSpeed;
        }

        super.updateEntity();
        vacuumHandler.updateEntityI();
        List<Pair<ForgeDirection, IAirHandler>> teList = getConnectedPneumatics();
        if(teList.size() == 0) airLeak(getInputSide());
        teList = vacuumHandler.getConnectedPneumatics();
        if(teList.size() == 0) vacuumHandler.airLeak(getVacuumSide());

    }

    @Override
    public void addAir(int amount, ForgeDirection side){
        if(side == getInputSide()) {
            currentAir += amount;
        } else {
            vacuumHandler.addAir(amount, side);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        NBTTagCompound vacuum = new NBTTagCompound();
        vacuumHandler.writeToNBTI(vacuum);
        tag.setTag("vacuum", vacuum);
        tag.setBoolean("turning", turning);
        tag.setInteger("redstoneMode", redstoneMode);
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
        tag.setTag("Items", tagList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        vacuumHandler.readFromNBTI(tag.getCompoundTag("vacuum"));
        turning = tag.getBoolean("turning");
        redstoneMode = tag.getInteger("redstoneMode");
        // Read in the ItemStacks in the inventory from NBT
        NBTTagList tagList = tag.getTagList("Items", 10);
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
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 2) redstoneMode = 0;
        }
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
    public void printManometerMessage(EntityPlayer player, List<String> curInfo){
        curInfo.add(EnumChatFormatting.GREEN + "Input pressure: " + PneumaticCraftUtils.roundNumberTo(getPressure(getInputSide()), 1) + " bar. Vacuum pressure: " + PneumaticCraftUtils.roundNumberTo(getPressure(getVacuumSide()), 1) + " bar.");
    }

    // INVENTORY METHODS- && NBT
    // ------------------------------------------------------------

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
        // super.setInventorySlotContents(slot, itemStack);
        inventory[slot] = itemStack;
        if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
    }

    @Override
    public String getInventoryName(){

        return Blockss.vacuumPump.getUnlocalizedName();
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        return itemstack.getItem() == Itemss.machineUpgrade;
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
