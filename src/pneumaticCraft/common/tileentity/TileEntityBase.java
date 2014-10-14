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

    public void setUpgradeSlots(int[] upgradeSlots){
        this.upgradeSlots = upgradeSlots;
    }

    public int[] getUpgradeSlots(){
        return upgradeSlots;
    }

    protected void addLuaMethods(){}

    public static void saveInventory(ItemStack[] inventory, NBTTagCompound tag, String tagName){
        NBTTagList tagList = new NBTTagList();
        for(int i = 0; i < inventory.length; ++i) {
            if(inventory[i] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)i);
                inventory[i].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        tag.setTag(tagName, tagList);
    }

    public static void loadInventory(ItemStack[] inventory, NBTTagCompound tag, String tagName){
        for(int i = 0; i < inventory.length; i++)
            inventory[i] = null;

        NBTTagList tagList = tag.getTagList(tagName, 10);
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
    }

    public static void saveInventory(IInventory inventory, NBTTagCompound tag, String tagName){
        NBTTagList tagList = new NBTTagList();
        for(int i = 0; i < inventory.getSizeInventory(); ++i) {
            if(inventory.getStackInSlot(i) != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)i);
                inventory.getStackInSlot(i).writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        tag.setTag(tagName, tagList);
    }

    public static void loadInventory(IInventory inventory, NBTTagCompound tag, String tagName){
        for(int i = 0; i < inventory.getSizeInventory(); i++)
            inventory.setInventorySlotContents(i, null);

        NBTTagList tagList = tag.getTagList(tagName, 10);
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.getSizeInventory()) {
                inventory.setInventorySlotContents(i, ItemStack.loadItemStackFromNBT(tagCompound));
            }
        }
    }
}
