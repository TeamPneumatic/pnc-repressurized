package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerChargingStationItemInventory;
import pneumaticCraft.common.item.IChargingStationGUIHolderItem;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.proxy.CommonProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityChargingStation extends TileEntityPneumaticBase implements ISidedInventory, IRedstoneControl{
    private ItemStack[] inventory;

    private final int INVENTORY_SIZE = 5;

    public static final int CHARGE_INVENTORY_INDEX = 0;
    public static final int UPGRADE_SLOT_START = 1;
    public static final int UPGRADE_SLOT_END = 4;
    public static final float ANIMATION_AIR_SPEED = 0.001F;

    public boolean charging;
    public boolean disCharging;
    public int redstoneMode;
    private boolean oldRedstoneStatus;
    public float renderAirProgress;

    public TileEntityChargingStation(){
        super(PneumaticValues.DANGER_PRESSURE_CHARGING_STATION, PneumaticValues.MAX_PRESSURE_CHARGING_STATION, PneumaticValues.VOLUME_CHARGING_STATION);
        inventory = new ItemStack[INVENTORY_SIZE];
        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 2, 3, UPGRADE_SLOT_END});
    }

    @Override
    public void updateEntity(){
        disCharging = false;
        charging = false;
        List<IPressurizable> chargingItems = new ArrayList<IPressurizable>();
        List<ItemStack> chargedStacks = new ArrayList<ItemStack>();
        if(inventory[CHARGE_INVENTORY_INDEX] != null && inventory[CHARGE_INVENTORY_INDEX].getItem() instanceof IPressurizable) {
            chargingItems.add((IPressurizable)inventory[CHARGE_INVENTORY_INDEX].getItem());
            chargedStacks.add(inventory[CHARGE_INVENTORY_INDEX]);
        }
        if(this.getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) > 0) {
            //creating a new word, 'entities padding'.
            List<Entity> entitiesPadding = worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 2, zCoord + 1));
            for(Entity entity : entitiesPadding) {
                if(entity instanceof IPressurizable) {
                    chargingItems.add((IPressurizable)entity);
                    chargedStacks.add(null);
                } else if(entity instanceof EntityItem) {
                    ItemStack entityStack = ((EntityItem)entity).getEntityItem();
                    if(entityStack != null && entityStack.getItem() instanceof IPressurizable) {
                        chargingItems.add((IPressurizable)entityStack.getItem());
                        chargedStacks.add(entityStack);
                    }
                } else if(entity instanceof EntityPlayer) {
                    InventoryPlayer inv = ((EntityPlayer)entity).inventory;
                    for(int i = 0; i < inv.getSizeInventory(); i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if(stack != null && stack.getItem() instanceof IPressurizable) {
                            chargingItems.add((IPressurizable)stack.getItem());
                            chargedStacks.add(stack);
                        }
                    }
                }
            }
        }
        int speedMultiplier = (int)getSpeedMultiplierFromUpgrades(getUpgradeSlots());
        for(int i = 0; i < PneumaticValues.CHARGING_STATION_CHARGE_RATE * speedMultiplier; i++) {
            for(int j = 0; j < chargingItems.size(); j++) {
                IPressurizable chargingItem = chargingItems.get(j);
                ItemStack chargedItem = chargedStacks.get(j);
                if(chargingItem.getPressure(chargedItem) > getPressure(ForgeDirection.UNKNOWN) + 0.01F && chargingItem.getPressure(chargedItem) > 0F) {
                    chargingItem.addAir(chargedItem, -1);
                    addAir(1, ForgeDirection.UNKNOWN);
                    disCharging = true;
                    renderAirProgress -= ANIMATION_AIR_SPEED;
                    if(renderAirProgress < 0.0F) {
                        renderAirProgress += 1F;
                    }
                } else if(chargingItem.getPressure(chargedItem) < getPressure(ForgeDirection.UNKNOWN) - 0.01F && chargingItem.getPressure(chargedItem) < chargingItem.maxPressure(chargedItem)) {// if there is pressure, and the item isn't fully charged yet..
                    chargingItem.addAir(chargedItem, 1);
                    addAir(-1, ForgeDirection.UNKNOWN);
                    charging = true;
                    renderAirProgress += ANIMATION_AIR_SPEED;
                    if(renderAirProgress > 1.0F) {
                        renderAirProgress -= 1F;
                    }
                }
            }
        }

        if(!worldObj.isRemote && oldRedstoneStatus != shouldEmitRedstone()) {
            oldRedstoneStatus = shouldEmitRedstone();
            updateNeighbours();
        }

        super.updateEntity();

    }

    @Override
    protected void disperseAir(){
        super.disperseAir();
        List<Pair<ForgeDirection, IPneumaticMachine>> teList = getConnectedPneumatics();
        if(teList.size() == 0) airLeak(ForgeDirection.getOrientation(getBlockMetadata()));
    }

    @Override
    public boolean isConnectedTo(ForgeDirection side){
        return ForgeDirection.getOrientation(worldObj.getBlockMetadata(xCoord, yCoord, zCoord)) == side;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 3) redstoneMode = 0;
            updateNeighbours();
            sendDescriptionPacket();
        } else if((buttonID == 1 || buttonID == 2) && inventory[CHARGE_INVENTORY_INDEX] != null && inventory[CHARGE_INVENTORY_INDEX].getItem() instanceof IChargingStationGUIHolderItem) {
            player.openGui(PneumaticCraft.instance, buttonID == 1 ? ((IChargingStationGUIHolderItem)inventory[CHARGE_INVENTORY_INDEX].getItem()).getGuiID() : CommonProxy.GUI_ID_CHARGING_STATION, worldObj, xCoord, yCoord, zCoord);
        }
    }

    public boolean shouldEmitRedstone(){
        // if(!worldObj.isRemote) System.out.println("redstone mode: " +
        // redstoneMode + ", charging: " + charging + ",discharging: " +
        // disCharging);

        switch(redstoneMode){
            case 0:
                return false;
            case 1:
                return !charging && !disCharging && inventory[CHARGE_INVENTORY_INDEX] != null && inventory[CHARGE_INVENTORY_INDEX].getItem() instanceof IPressurizable;
            case 2:
                return charging;
            case 3:
                return disCharging;

        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
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
        if(slot == CHARGE_INVENTORY_INDEX && !worldObj.isRemote) {
            List<EntityPlayer> players = worldObj.playerEntities;
            for(EntityPlayer player : players) {
                if(player.openContainer instanceof ContainerChargingStationItemInventory && ((ContainerChargingStationItemInventory)player.openContainer).te == this) {
                    if(itemStack != null && itemStack.getItem() instanceof IChargingStationGUIHolderItem) {
                        // player.openGui(PneumaticCraft.instance, CommonProxy.GUI_ID_PNEUMATIC_ARMOR, worldObj, xCoord, yCoord, zCoord);
                    } else {
                        player.openGui(PneumaticCraft.instance, CommonProxy.GUI_ID_CHARGING_STATION, worldObj, xCoord, yCoord, zCoord);
                    }
                }
            }
        }
    }

    @Override
    public String getInventoryName(){

        return Blockss.chargingStation.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
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
    public void readFromNBT(NBTTagCompound nbtTagCompound){

        super.readFromNBT(nbtTagCompound);
        // Read in the ItemStacks in the inventory from NBT

        redstoneMode = nbtTagCompound.getInteger("redstoneMode");
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
        // Write the ItemStacks in the inventory to NBT
        nbtTagCompound.setInteger("redstoneMode", redstoneMode);
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
        return i == 0 || itemstack != null && itemstack.getItem() == Itemss.machineUpgrade;
    }

    @Override
    // upgrades in bottom, fuel in the rest.
    public int[] getAccessibleSlotsFromSide(int var1){
        if(var1 == 0) return new int[]{1, 2, 3, 4};
        return new int[]{0};
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, int j){
        return true;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, int j){
        return true;
    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }
}
