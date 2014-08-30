package pneumaticCraft.common.tileentity;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.Names;
import pneumaticCraft.lib.PneumaticValues;

import com.mojang.authlib.GameProfile;

public class TileEntityAerialInterface extends TileEntityPneumaticBase implements ISidedInventory{
    private ItemStack[] inventory;

    private final int INVENTORY_SIZE = 4;

    private static final int UPGRADE_SLOT_START = 0;
    private static final int UPGRADE_SLOT_END = 3;

    public String playerName = "";
    public String playerUUID = "";

    public int redstoneMode;
    private boolean oldRedstoneStatus;
    private boolean updateNeighbours;
    public boolean isConnectedToPlayer;

    public TileEntityAerialInterface(){
        super(PneumaticValues.DANGER_PRESSURE_AERIAL_INTERFACE, PneumaticValues.MAX_PRESSURE_AERIAL_INTERFACE, PneumaticValues.VOLUME_AERIAL_INTERFACE);
        inventory = new ItemStack[INVENTORY_SIZE];
        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 1, 2, UPGRADE_SLOT_END});
    }

    public void setPlayer(GameProfile gameProfile){
        setPlayer(gameProfile.getName(), gameProfile.getId() != null ? gameProfile.getId().toString() : "");
    }

    public void setPlayer(String username, String uuid){
        playerName = username;
        playerUUID = uuid;
        updateNeighbours = true;
    }

    @Override
    public void updateEntity(){
        if(!worldObj.isRemote && updateNeighbours) {
            updateNeighbours = false;
            //System.out.println("UPDATING NEIGHBOURS");
            updateNeighbours();
        }
        if(!worldObj.isRemote) {
            if(getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE && isConnectedToPlayer) {
                addAir(-PneumaticValues.USAGE_AERIAL_INTERFACE, ForgeDirection.UNKNOWN);
            }
            if(numUsingPlayers > 0) {
                boolean wasConnected = isConnectedToPlayer;
                getPlayerInventory();
                if(wasConnected != isConnectedToPlayer) {
                    sendDescriptionPacket();
                }
            }
        }

        if(oldRedstoneStatus != shouldEmitRedstone()) {
            oldRedstoneStatus = shouldEmitRedstone();
            updateNeighbours = true;
        }

        // if(!worldObj.isRemote) System.out.println("player: " + playerName);
        super.updateEntity();

    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 1) redstoneMode = 0;
            // updateNeighbours();
            sendDescriptionPacket();
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
                return isConnectedToPlayer;

        }
        return false;
    }

    private InventoryPlayer getPlayerInventory(){
        if(worldObj != null && !worldObj.isRemote) {
            EntityPlayer player = null;
            for(EntityPlayer checkingPlayer : (List<EntityPlayer>)MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
                if(playerUUID.equals("")) {//TODO remove legacy code.
                    if(checkingPlayer.getCommandSenderName().equals(playerName)) {
                        player = checkingPlayer;
                        break;
                    }
                } else {
                    if(checkingPlayer.getGameProfile().getId().toString().equals(playerUUID)) {
                        player = checkingPlayer;
                        break;
                    }
                }
            }
            isConnectedToPlayer = player != null;
            if(isConnectedToPlayer) {
                if(playerUUID.equals("")) {
                    playerUUID = player.getGameProfile().getId().toString();
                    Log.info("Legacy conversion: Aerial Interface username '" + player.getCommandSenderName() + "' is now using UUID '" + playerUUID + "'.");
                }
                return player.inventory;
            }
        }
        return null;
    }

    @Override
    public boolean hasCustomInventoryName(){
        return true;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){
        InventoryPlayer inventoryPlayer = getPlayerInventory();
        return inventory.length + (inventoryPlayer != null ? inventoryPlayer.getSizeInventory() : 0);
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int slot){
        if(slot < 4) {
            return inventory[slot];
        } else {
            InventoryPlayer inventoryPlayer = getPlayerInventory();
            return inventoryPlayer != null ? inventoryPlayer.getStackInSlot(slot - 4) : null;
        }
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount){
        if(slot < 4) {
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
        } else {
            InventoryPlayer inventoryPlayer = getPlayerInventory();
            return inventoryPlayer != null ? inventoryPlayer.decrStackSize(slot - 4, amount) : null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot){
        if(slot < 4) {
            ItemStack itemStack = getStackInSlot(slot);
            if(itemStack != null) {
                setInventorySlotContents(slot, null);
            }
            return itemStack;
        } else {
            InventoryPlayer inventoryPlayer = getPlayerInventory();
            return inventoryPlayer != null ? inventoryPlayer.getStackInSlotOnClosing(slot - 4) : null;
        }
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack){
        if(slot < 4) {
            inventory[slot] = itemStack;
            if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
                itemStack.stackSize = getInventoryStackLimit();
            }
        } else {
            InventoryPlayer inventoryPlayer = getPlayerInventory();
            if(inventoryPlayer != null) {
                inventoryPlayer.setInventorySlotContents(slot - 4, itemStack);
            } else if(worldObj != null && !worldObj.isRemote) {
                EntityItem item = new EntityItem(worldObj, xCoord, yCoord, zCoord, itemStack);
                worldObj.spawnEntityInWorld(item);
            }
        }

    }

    @Override
    public String getInventoryName(){

        return Names.AERIAL_INTERFACE;
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound){

        super.readFromNBT(nbtTagCompound);
        // Read in the ItemStacks in the inventory from NBT

        redstoneMode = nbtTagCompound.getInteger("redstoneMode");
        setPlayer(nbtTagCompound.getString("playerName"), nbtTagCompound.getString("playerUUID"));
        isConnectedToPlayer = nbtTagCompound.getBoolean("connected");
        NBTTagList tagList = nbtTagCompound.getTagList("Items", 10);
        inventory = new ItemStack[4];
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
        nbtTagCompound.setString("playerName", playerName);
        nbtTagCompound.setString("playerUUID", playerUUID);

        nbtTagCompound.setBoolean("connected", isConnectedToPlayer);
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
        return i >= 4 || itemstack != null && itemstack.getItem() == Itemss.machineUpgrade;
    }

    @Override
    // upgrades in bottom, fuel in the rest.
    public int[] getAccessibleSlotsFromSide(int var1){
        if(ForgeDirection.getOrientation(var1) == ForgeDirection.UP) {
            return new int[]{0, 1, 2, 3};
        } else if(getPlayerInventory() == null) {
            return new int[0];
        } else if(ForgeDirection.getOrientation(var1) == ForgeDirection.DOWN) {
            return new int[]{40, 41, 42, 43};
        } else {
            int[] mainInv = new int[36];
            for(int i = 0; i < 36; i++) {
                mainInv[i] = i + 4;
            }
            return mainInv;
        }

    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, int j){
        if(i < 4) return true;
        if(getPlayerInventory() == null) return false;
        if(getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE) {
            return i < 40 || itemstack != null && itemstack.getItem() instanceof ItemArmor && ((ItemArmor)itemstack.getItem()).armorType == 43 - i;
        } else {
            return false;
        }
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, int j){
        return i < 4 || getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1){
        return isGuiUseableByPlayer(var1);
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){}
}
