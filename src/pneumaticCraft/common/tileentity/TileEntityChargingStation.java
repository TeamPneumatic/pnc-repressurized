package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerChargingStationItemInventory;
import pneumaticCraft.common.inventory.InventoryPneumaticInventoryItem;
import pneumaticCraft.common.item.IChargingStationGUIHolderItem;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityChargingStation extends TileEntityPneumaticBase implements ISidedInventory, IRedstoneControl{
    @DescSynced
    private ItemStack[] inventory;
    private InventoryPneumaticInventoryItem chargeableInventory;

    private final int INVENTORY_SIZE = 5;

    public static final int CHARGE_INVENTORY_INDEX = 0;
    public static final int UPGRADE_SLOT_START = 1;
    public static final int UPGRADE_SLOT_END = 4;
    public static final float ANIMATION_AIR_SPEED = 0.001F;

    @GuiSynced
    public boolean charging;
    @GuiSynced
    public boolean disCharging;
    @GuiSynced
    public int redstoneMode;
    private boolean oldRedstoneStatus;
    public float renderAirProgress;
    @DescSynced
    private ItemStack camoStack;

    public TileEntityChargingStation(){
        super(PneumaticValues.DANGER_PRESSURE_CHARGING_STATION, PneumaticValues.MAX_PRESSURE_CHARGING_STATION, PneumaticValues.VOLUME_CHARGING_STATION);
        inventory = new ItemStack[INVENTORY_SIZE];
        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 2, 3, UPGRADE_SLOT_END});
    }

    public ItemStack getCamoStack(){
        if(camoStack == null || !(camoStack.getItem() instanceof ItemBlock)) {
            return new ItemStack(Blocks.cobblestone);
        }
        Block block = ((ItemBlock)camoStack.getItem()).field_150939_a;
        if(PneumaticCraftUtils.isRenderIDCamo(block.getRenderType())) {
            return camoStack;
        } else {
            return new ItemStack(Blocks.cobblestone);
        }
    }

    public void setCamoStack(ItemStack stack){
        camoStack = stack;
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
            List<Entity> entitiesPadding = worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2, zCoord + 1));
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
            boolean charged = false;
            for(int j = 0; j < chargingItems.size(); j++) {
                IPressurizable chargingItem = chargingItems.get(j);
                ItemStack chargedItem = chargedStacks.get(j);
                if(chargingItem.getPressure(chargedItem) > getPressure(ForgeDirection.UNKNOWN) + 0.01F && chargingItem.getPressure(chargedItem) > 0F) {
                    if(!worldObj.isRemote) {
                        chargingItem.addAir(chargedItem, -1);
                        addAir(1, ForgeDirection.UNKNOWN);
                    }
                    disCharging = true;
                    renderAirProgress -= ANIMATION_AIR_SPEED;
                    if(renderAirProgress < 0.0F) {
                        renderAirProgress += 1F;
                    }
                    charged = true;
                } else if(chargingItem.getPressure(chargedItem) < getPressure(ForgeDirection.UNKNOWN) - 0.01F && chargingItem.getPressure(chargedItem) < chargingItem.maxPressure(chargedItem)) {// if there is pressure, and the item isn't fully charged yet..
                    if(!worldObj.isRemote) {
                        chargingItem.addAir(chargedItem, 1);
                        addAir(-1, ForgeDirection.UNKNOWN);
                    }
                    charging = true;
                    renderAirProgress += ANIMATION_AIR_SPEED;
                    if(renderAirProgress > 1.0F) {
                        renderAirProgress -= 1F;
                    }
                    charged = true;
                }
            }
            if(!charged) break;
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
        List<Pair<ForgeDirection, IAirHandler>> teList = getConnectedPneumatics();
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
        } else if((buttonID == 1 || buttonID == 2) && inventory[CHARGE_INVENTORY_INDEX] != null && inventory[CHARGE_INVENTORY_INDEX].getItem() instanceof IChargingStationGUIHolderItem) {
            player.openGui(PneumaticCraft.instance, buttonID == 1 ? ((IChargingStationGUIHolderItem)inventory[CHARGE_INVENTORY_INDEX].getItem()).getGuiID().ordinal() : EnumGuiId.CHARGING_STATION.ordinal(), worldObj, xCoord, yCoord, zCoord);
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

    public InventoryPneumaticInventoryItem getChargeableInventory(){
        return worldObj.isRemote ? new InventoryPneumaticInventoryItem(this) : chargeableInventory;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){

        return inventory.length + (chargeableInventory != null ? chargeableInventory.getSizeInventory() : 0);
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int slot){
        if(slot < inventory.length) {
            return inventory[slot];
        } else {
            return chargeableInventory != null ? chargeableInventory.getStackInSlot(slot - inventory.length) : null;
        }
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
        if(!worldObj.isRemote && chargeableInventory != null && slot == CHARGE_INVENTORY_INDEX) {
            chargeableInventory.writeToNBT();
            chargeableInventory = null;
        }

        if(slot < inventory.length) {
            inventory[slot] = itemStack;
        } else if(chargeableInventory != null) {
            chargeableInventory.setInventorySlotContents(slot - inventory.length, itemStack);
        }

        if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
        if(slot == CHARGE_INVENTORY_INDEX && !worldObj.isRemote) {
            if(itemStack != null && itemStack.getItem() instanceof IChargingStationGUIHolderItem) {
                chargeableInventory = new InventoryPneumaticInventoryItem(this);
            }
            List<EntityPlayer> players = worldObj.playerEntities;
            for(EntityPlayer player : players) {
                if(player.openContainer instanceof ContainerChargingStationItemInventory && ((ContainerChargingStationItemInventory)player.openContainer).te == this) {
                    if(itemStack != null && itemStack.getItem() instanceof IChargingStationGUIHolderItem) {
                        // player.openGui(PneumaticCraft.instance, CommonProxy.GUI_ID_PNEUMATIC_ARMOR, worldObj, xCoord, yCoord, zCoord);
                    } else {
                        player.openGui(PneumaticCraft.instance, EnumGuiId.CHARGING_STATION.ordinal(), worldObj, xCoord, yCoord, zCoord);
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
    public void readFromNBT(NBTTagCompound tag){

        super.readFromNBT(tag);
        // Read in the ItemStacks in the inventory from NBT

        redstoneMode = tag.getInteger("redstoneMode");
        NBTTagList tagList = tag.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
        ItemStack chargeSlot = inventory[CHARGE_INVENTORY_INDEX];
        if(chargeSlot != null && chargeSlot.getItem() instanceof IChargingStationGUIHolderItem) {
            chargeableInventory = new InventoryPneumaticInventoryItem(this);
        }

        if(tag.hasKey("camoStack")) {
            camoStack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("camoStack"));
        } else {
            camoStack = null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){

        super.writeToNBT(tag);

        if(chargeableInventory != null) chargeableInventory.writeToNBT();

        // Write the ItemStacks in the inventory to NBT
        tag.setInteger("redstoneMode", redstoneMode);
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

        if(camoStack != null) {
            NBTTagCompound subTag = new NBTTagCompound();
            camoStack.writeToNBT(subTag);
            tag.setTag("camoStack", subTag);
        }

    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemstack){
        if(itemstack == null) return true;
        if(slot < inventory.length) {
            if(slot == 0) {
                return itemstack.getItem() instanceof IPressurizable;
            } else {
                return itemstack.getItem() == Itemss.machineUpgrade;
            }
        } else {
            return chargeableInventory.isItemValidForSlot(slot - inventory.length, itemstack);
        }
    }

    @Override
    // upgrades in bottom, fuel in the rest.
    public int[] getAccessibleSlotsFromSide(int var1){
        if(chargeableInventory != null) {
            int[] slots = new int[chargeableInventory.getSizeInventory() + 1];
            slots[0] = 0;
            for(int i = 0; i < chargeableInventory.getSizeInventory(); i++) {
                slots[i + 1] = i + inventory.length;
            }
            return slots;
        } else {
            return new int[]{0};
        }
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
    public void markDirty(){
        super.markDirty();
        if(chargeableInventory != null) chargeableInventory.markDirty();
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }

    @Override
    protected boolean shouldRerenderChunkOnDescUpdate(){
        return true;
    }
}
