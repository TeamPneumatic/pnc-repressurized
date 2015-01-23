package pneumaticCraft.common.tileentity;

import ic2.api.item.IC2Items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.inventory.SyncedField;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.DescPacketHandler;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.NetworkUtils;
import pneumaticCraft.common.network.PacketDescription;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class TileEntityBase extends TileEntity implements IGUIButtonSensitive{
    /**
     * True only the first time updateEntity invokes in a session.
     */
    protected boolean firstRun = true;
    private int[] upgradeSlots;
    private boolean descriptionPacketScheduled;
    private List<SyncedField> descriptionFields;
    protected int poweredRedstone; //The redstone strength currently applied to the block.

    public TileEntityBase(){

    }

    public TileEntityBase(int... upgradeSlots){
        this();
        this.upgradeSlots = upgradeSlots;
    }

    @Override
    public Packet getDescriptionPacket(){
        return DescPacketHandler.getPacket(new PacketDescription(this));
    }

    protected double getPacketDistance(){
        return 64;
    }

    public List<SyncedField> getDescriptionFields(){
        if(descriptionFields == null) {
            descriptionFields = NetworkUtils.getSyncedFields(this, DescSynced.class);
            for(SyncedField field : descriptionFields) {
                field.update();
            }
        }
        return descriptionFields;
    }

    /**
     * Sends the description packet to every client within PACKET_UPDATE_DISTANCE blocks, and in the same dimension.
     */
    public void sendDescriptionPacket(){
        //PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, TileEntityConstants.PACKET_UPDATE_DISTANCE, worldObj.provider.dimensionId, getDescriptionPacket());
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    /**
     * A way to safely mark a block for an update from another thread (like the CC Lua thread).
     */
    protected void scheduleDescriptionPacket(){
        descriptionPacketScheduled = true;
    }

    public void sendDescPacket(double maxPacketDistance){
        NetworkHandler.sendToAllAround(new PacketDescription(this), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, maxPacketDistance));
    }

    @Override
    public void updateEntity(){
        if(firstRun && !worldObj.isRemote) {
            //firstRun = false;
            onFirstServerUpdate();
            onNeighborTileUpdate();
            onNeighborBlockUpdate();
        }
        firstRun = false;

        if(!worldObj.isRemote) {
            if(this instanceof IHeatExchanger) {
                ((IHeatExchanger)this).getHeatExchangerLogic(ForgeDirection.UNKNOWN).update();
            }

            if(descriptionFields == null) descriptionPacketScheduled = true;
            for(SyncedField field : getDescriptionFields()) {
                if(field.update()) {
                    descriptionPacketScheduled = true;
                }
            }

            if(descriptionPacketScheduled) {
                descriptionPacketScheduled = false;
                sendDescriptionPacket();
            }
        }
    }

    protected void onFirstServerUpdate(){
        initializeIfHeatExchanger();
    }

    protected void updateNeighbours(){
        int oldMeta = getBlockMetadata();
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 15, 3);
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, oldMeta, 3);
        /*
         * worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord - 1, zCoord,
         * PneumaticCraft.BlockChargingStation.blockID);
         * worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord + 1, zCoord,
         * PneumaticCraft.BlockChargingStation.blockID);
         * worldObj.notifyBlocksOfNeighborChange(xCoord - 1, yCoord, zCoord,
         * PneumaticCraft.BlockChargingStation.blockID);
         * worldObj.notifyBlocksOfNeighborChange(xCoord + 1, yCoord, zCoord,
         * PneumaticCraft.BlockChargingStation.blockID);
         * worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord - 1,
         * PneumaticCraft.BlockChargingStation.blockID);
         * worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord + 1,
         * PneumaticCraft.BlockChargingStation.blockID);
         */
    }

    protected void rerenderChunk(){
        worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
    }

    protected boolean shouldRerenderChunkOnDescUpdate(){
        return false;
    }

    /**
     * Encoded into the description packet. Also is included in the world save.
     * Used as last resort, using @DescSynced is preferred.
     * @param tag
     */
    public void writeToPacket(NBTTagCompound tag){}

    /**
     * Encoded into the description packet. Also is included in the world save.
     * Used as last resort, using @DescSynced is preferred.
     * @param tag
     */
    public void readFromPacket(NBTTagCompound tag){}

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        writeToPacket(tag);
        if(this instanceof IHeatExchanger) {
            ((IHeatExchanger)this).getHeatExchangerLogic(ForgeDirection.UNKNOWN).writeToNBT(tag);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        readFromPacket(tag);
        if(this instanceof IHeatExchanger) {
            ((IHeatExchanger)this).getHeatExchangerLogic(ForgeDirection.UNKNOWN).readFromNBT(tag);
        }
    }

    @Override
    public void validate(){
        super.validate();
        scheduleDescriptionPacket();
    }

    public void onDescUpdate(){
        if(shouldRerenderChunkOnDescUpdate()) rerenderChunk();
    }

    public ForgeDirection getRotation(){
        return ForgeDirection.getOrientation(getBlockMetadata());
    }

    public int getUpgrades(int upgradeDamage){
        return getUpgrades(upgradeDamage, this instanceof IPneumaticMachine ? ((IPneumaticMachine)this).getAirHandler().getUpgradeSlots() : getUpgradeSlots());
    }

    protected int getUpgrades(int upgradeDamage, int... upgradeSlots){
        int upgrades = 0;
        IInventory inv = null;
        if(this instanceof IInventory) inv = (IInventory)this;
        if(inv == null && this instanceof TileEntityPneumaticBase && ((TileEntityPneumaticBase)this).parentTile instanceof IInventory) inv = (IInventory)((TileEntityPneumaticBase)this).parentTile;
        if(inv != null) {
            for(int i : upgradeSlots) {
                if(inv.getStackInSlot(i) != null && inv.getStackInSlot(i).getItem() == Itemss.machineUpgrade && inv.getStackInSlot(i).getItemDamage() == upgradeDamage) {
                    upgrades += inv.getStackInSlot(i).stackSize;
                }
            }
        }
        return upgrades;
    }

    protected float getSpeedMultiplierFromUpgrades(){
        return getSpeedMultiplierFromUpgrades(getUpgradeSlots());
    }

    protected float getSpeedUsageMultiplierFromUpgrades(){
        return getSpeedUsageMultiplierFromUpgrades(getUpgradeSlots());
    }

    public float getSpeedMultiplierFromUpgrades(int[] upgradeSlots){
        return (float)Math.pow(PneumaticValues.SPEED_UPGRADE_MULTIPLIER, Math.min(10, getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE, upgradeSlots)));
    }

    protected float getSpeedUsageMultiplierFromUpgrades(int[] upgradeSlots){
        return (float)Math.pow(PneumaticValues.SPEED_UPGRADE_USAGE_MULTIPLIER, Math.min(10, getUpgrades(ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE, upgradeSlots)));
    }

    @Optional.Method(modid = ModIds.INDUSTRIALCRAFT)
    protected int getIC2Upgrades(String ic2ItemKey, int[] upgradeSlots){
        ItemStack itemStack = IC2Items.getItem(ic2ItemKey);
        if(itemStack == null) return 0;
        int upgrades = 0;
        if(this instanceof IInventory) {// this always should be true.
            IInventory inv = (IInventory)this;
            for(int i : upgradeSlots) {
                if(inv.getStackInSlot(i) != null && inv.getStackInSlot(i).getItem() == itemStack.getItem()) {
                    upgrades += inv.getStackInSlot(i).stackSize;
                }
            }
        }
        return upgrades;
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player){}

    public boolean isGuiUseableByPlayer(EntityPlayer par1EntityPlayer){
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) != this ? false : par1EntityPlayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    public void setUpgradeSlots(int... upgradeSlots){
        this.upgradeSlots = upgradeSlots;
    }

    public int[] getUpgradeSlots(){
        return upgradeSlots;
    }

    protected void writeInventoryToNBT(NBTTagCompound tag, ItemStack[] stacks){
        writeInventoryToNBT(tag, stacks, "Items");
    }

    protected void writeInventoryToNBT(NBTTagCompound tag, IInventory inventory, String tagName){
        ItemStack[] stacks = new ItemStack[inventory.getSizeInventory()];
        for(int i = 0; i < stacks.length; i++) {
            stacks[i] = inventory.getStackInSlot(i);
        }
        writeInventoryToNBT(tag, stacks, tagName);
    }

    protected void writeInventoryToNBT(NBTTagCompound tag, ItemStack[] stacks, String tagName){
        NBTTagList tagList = new NBTTagList();
        for(int i = 0; i < stacks.length; i++) {
            if(stacks[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                stacks[i].writeToNBT(itemTag);
                itemTag.setByte("Slot", (byte)i);
                tagList.appendTag(itemTag);
            }
        }
        tag.setTag(tagName, tagList);
    }

    protected void readInventoryFromNBT(NBTTagCompound tag, ItemStack[] stacks){
        readInventoryFromNBT(tag, stacks, "Items");
    }

    protected void readInventoryFromNBT(NBTTagCompound tag, IInventory inventory, String tagName){
        ItemStack[] stacks = new ItemStack[inventory.getSizeInventory()];
        readInventoryFromNBT(tag, stacks, tagName);
        for(int i = 0; i < stacks.length; i++) {
            inventory.setInventorySlotContents(i, stacks[i]);
        }
    }

    protected void readInventoryFromNBT(NBTTagCompound tag, ItemStack[] stacks, String tagName){
        for(int i = 0; i < stacks.length; i++) {
            stacks[i] = null;
        }
        NBTTagList tagList = tag.getTagList(tagName, 10);
        for(int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound itemTag = tagList.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot");
            if(slot >= 0 && slot < stacks.length) {
                stacks[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
    }

    public void onNeighborTileUpdate(){
        initializeIfHeatExchanger();
    }

    public void onNeighborBlockUpdate(){
        poweredRedstone = PneumaticCraftUtils.getRedstoneLevel(worldObj, xCoord, yCoord, zCoord);
        initializeIfHeatExchanger();
    }

    public boolean redstoneAllows(){
        switch(((IRedstoneControl)this).getRedstoneMode()){
            case 0:
                return true;
            case 1:
                return poweredRedstone > 0;
            case 2:
                return poweredRedstone == 0;
        }
        return false;
    }

    private void initializeIfHeatExchanger(){
        if(this instanceof IHeatExchanger) {
            ((IHeatExchanger)this).getHeatExchangerLogic(ForgeDirection.UNKNOWN).initializeAsHull(worldObj, xCoord, yCoord, zCoord);
        }
    }
}
