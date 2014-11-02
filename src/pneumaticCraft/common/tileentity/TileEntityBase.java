package pneumaticCraft.common.tileentity;

import ic2.api.item.IC2Items;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeModContainer;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSendNBTPacket;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class TileEntityBase extends TileEntity implements IGUIButtonSensitive{
    /**
     * True only the first time updateEntity invokes in a session.
     */
    protected boolean firstRun = true;
    private int firstRunTicks = 100;
    public int numUsingPlayers;
    private int[] upgradeSlots;
    private boolean descriptionPacketScheduled;
    protected boolean isRedstonePowered;

    public TileEntityBase(){}

    public TileEntityBase(int... upgradeSlots){
        this.upgradeSlots = upgradeSlots;
    }

    @Override
    public Packet getDescriptionPacket(){
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
        if(ForgeModContainer.removeErroringTileEntities) {//Added protection, as apparently sometimes the NBTTagCompound is null...
            try {
                readFromNBT(pkt.func_148857_g());
            } catch(Throwable e) {
                Log.error("Removing erroring TileEntity at " + xCoord + ", " + yCoord + ", " + zCoord + ".");
                e.printStackTrace();
                invalidate();
                worldObj.setBlockToAir(xCoord, yCoord, zCoord);
            }
        } else {
            readFromNBT(pkt.func_148857_g());
        }
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

    public void sendNBTPacket(double maxPacketDistance){
        NetworkHandler.sendToAllAround(new PacketSendNBTPacket(this), new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, maxPacketDistance));
    }

    @Override
    public void updateEntity(){
        if(firstRun && !worldObj.isRemote) {
            //firstRun = false;
            onFirstServerUpdate();
            onNeighborTileUpdate();
            firstRunTicks--;
            if(needsFirstRunUpdate() && firstRunTicks == 0) {
                sendDescriptionPacket();
            }
        }
        firstRun = false;
        if(!worldObj.isRemote && descriptionPacketScheduled) {
            descriptionPacketScheduled = false;
            sendDescriptionPacket();
        }
    }

    protected void onFirstServerUpdate(){}

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

    public void onNeighborBlockUpdate(){}

    public void onNeighborTileUpdate(){}

    /**
     * When returned true, the serversided TE will send an update (description) packet the first time updateEntity() invokes.
     * @return
     */
    protected boolean needsFirstRunUpdate(){
        return false;
    }

    // similar to openChest() from IInventory
    public void openGUI(){
        if(numUsingPlayers < 0) {
            numUsingPlayers = 0;
        }
        ++numUsingPlayers;
        if(!worldObj.isRemote) sendDescriptionPacket();
    }

    // similair to closeChest() from IInventory
    public void closeGUI(){
        --numUsingPlayers;
    }

    public int getUpgrades(int upgradeDamage){
        return getUpgrades(upgradeDamage, ((IPneumaticMachine)this).getAirHandler().getUpgradeSlots());
    }

    protected int getUpgrades(int upgradeDamage, int[] upgradeSlots){
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

    protected void addLuaMethods(){}

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

    public void onNeighborTileUpdate(){}

    public void onNeighborBlockUpdate(){
        isRedstonePowered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
    }

    public boolean redstoneAllows(){
        switch(((IRedstoneControl)this).getRedstoneMode()){
            case 0:
                return true;
            case 1:
                return isRedstonePowered;
            case 2:
                return !isRedstonePowered;
        }
        return false;
    }

}
