package pneumaticCraft.common.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.render.RenderRangeLines;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.ItemNetworkComponents;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketRenderRangeLines;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.TileEntityConstants;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntitySecurityStation extends TileEntityBase implements ISidedInventory, IGUITextFieldSensitive,
        IRangeLineShower, IRedstoneControl{
    private ItemStack[] inventory;
    private final int INVENTORY_SIZE = 39;
    public static final int UPGRADE_SLOT_START = 35;
    public static final int UPGRADE_SLOT_END = 38;

    public final List<GameProfile> hackedUsers = new ArrayList<GameProfile>(); //Stores all the users that have hacked this Security Station.
    public final List<GameProfile> sharedUsers = new ArrayList<GameProfile>(); //Stores all the users that have been allowed by the stationOwner.
    @GuiSynced
    private int rebootTimer;//When the player decides to reset the station, this variable will hold the remaining reboot time.
    @GuiSynced
    public String textFieldText = "";
    private int securityRange;
    private int oldSecurityRange; //range used by the range line renderer, to figure out if the range has been changed.
    private final RenderRangeLines rangeLineRenderer = new RenderRangeLines(0x33FF0000);

    @GuiSynced
    public int redstoneMode;
    public boolean oldRedstoneStatus;

    private boolean validNetwork;

    public TileEntitySecurityStation(){
        inventory = new ItemStack[INVENTORY_SIZE];
        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 36, 37, UPGRADE_SLOT_END});
    }

    @Override
    public void updateEntity(){
        if(rebootTimer > 0) {
            rebootTimer--;
            if(!worldObj.isRemote) {
                if(rebootTimer == 0) {
                    hackedUsers.clear();
                }
            }
        }
        if(worldObj.isRemote && !firstRun) {
            if(oldSecurityRange != getSecurityRange() || oldSecurityRange == 0) {
                rangeLineRenderer.resetRendering(getSecurityRange());
                oldSecurityRange = getSecurityRange();
            }
            rangeLineRenderer.update();
        }
        if(/* !worldObj.isRemote && */oldRedstoneStatus != shouldEmitRedstone()) {
            oldRedstoneStatus = shouldEmitRedstone();
            updateNeighbours();
        }

        securityRange = Math.min(2 + getUpgrades(ItemMachineUpgrade.UPGRADE_RANGE, getUpgradeSlots()), TileEntityConstants.SECURITY_STATION_MAX_RANGE);

        super.updateEntity();

    }

    public void rebootStation(){
        rebootTimer = TileEntityConstants.SECURITY_STATION_REBOOT_TIME;
    }

    public int getRebootTime(){
        return rebootTimer;
    }

    /**
     * Will initiate the wireframe rendering. When invoked on the server, it sends a packet to every client to render the box.
     */
    @Override
    public void showRangeLines(){
        if(worldObj.isRemote) {
            rangeLineRenderer.resetRendering(getSecurityRange());
        } else {
            NetworkHandler.sendToAllAround(new PacketRenderRangeLines(this), worldObj, TileEntityConstants.PACKET_UPDATE_DISTANCE + getSecurityRange());
        }
    }

    @SideOnly(Side.CLIENT)
    public void renderRangeLines(){
        rangeLineRenderer.render();
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 2) redstoneMode = 0;
            updateNeighbours();
        } else if(buttonID == 2) {
            rebootStation();
        } else if(buttonID == 3) {
            if(!hasValidNetwork()) {
                player.addChatComponentMessage(new ChatComponentTranslation(EnumChatFormatting.GREEN + "This Security Station is out of order: Its network hasn't been properly configured."));
            } else {
                player.openGui(PneumaticCraft.instance, EnumGuiId.HACKING.ordinal(), worldObj, xCoord, yCoord, zCoord);
            }
        } else if(buttonID > 3 && buttonID - 4 < sharedUsers.size()) {
            sharedUsers.remove(buttonID - 4);
        }
        sendDescriptionPacket();
    }

    public void addSharedUser(GameProfile user){
        for(GameProfile sharedUser : sharedUsers) {
            if(gameProfileEquals(sharedUser, user)) return;
        }
        sharedUsers.add(user);
        sendDescriptionPacket();
    }

    public void addHacker(GameProfile user){
        for(GameProfile hackedUser : hackedUsers) {
            if(gameProfileEquals(hackedUser, user)) {
                return;
            }
        }
        for(GameProfile sharedUser : sharedUsers) {
            if(gameProfileEquals(sharedUser, user)) return;
        }
        hackedUsers.add(user);
        sendDescriptionPacket();
    }

    private boolean gameProfileEquals(GameProfile profile1, GameProfile profile2){
        return profile1.getId() != null && profile2.getId() != null ? profile1.getId().equals(profile2.getId()) : profile1.getName().equals(profile2.getName());
    }

    public boolean shouldEmitRedstone(){
        switch(redstoneMode){
            case 0:
                return false;
            case 1:
                return isHacked();
            case 2:
                return getRebootTime() <= 0;
        }
        return false;
    }

    public boolean isHacked(){
        return hackedUsers.size() > 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
        if(!rangeLineRenderer.isCurrentlyRendering()) return super.getRenderBoundingBox();
        int range = getSecurityRange();
        return AxisAlignedBB.getBoundingBox(xCoord - range, yCoord - range, zCoord - range, xCoord + 1 + range, yCoord + 1 + range, zCoord + 1 + range);
    }

    public int getSecurityRange(){
        return securityRange;
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
        checkForNetworkValidity();
    }

    @Override
    public String getInventoryName(){
        return Blockss.securityStation.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){
        return 64;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){

        super.readFromNBT(tag);
        redstoneMode = tag.getInteger("redstoneMode");
        rebootTimer = tag.getInteger("startupTimer");
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
        checkForNetworkValidity();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){

        super.writeToNBT(tag);
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setInteger("startupTimer", rebootTimer);
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
    public void writeToPacket(NBTTagCompound tag){
        super.writeToPacket(tag);
        NBTTagList sharedList = new NBTTagList();
        for(int i = 0; i < sharedUsers.size(); i++) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setString("name", sharedUsers.get(i).getName());
            if(sharedUsers.get(i).getId() != null) tagCompound.setString("uuid", sharedUsers.get(i).getId().toString());
            sharedList.appendTag(tagCompound);
        }
        tag.setTag("SharedUsers", sharedList);

        NBTTagList hackedList = new NBTTagList();
        for(int i = 0; i < hackedUsers.size(); i++) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setString("name", hackedUsers.get(i).getName());
            if(hackedUsers.get(i).getId() != null) tagCompound.setString("uuid", hackedUsers.get(i).getId().toString());
            hackedList.appendTag(tagCompound);
        }
        tag.setTag("HackedUsers", hackedList);
    }

    @Override
    public void readFromPacket(NBTTagCompound tag){
        super.readFromPacket(tag);
        sharedUsers.clear();
        NBTTagList sharedList = tag.getTagList("SharedUsers", 10);
        for(int i = 0; i < sharedList.tagCount(); ++i) {
            NBTTagCompound tagCompound = sharedList.getCompoundTagAt(i);
            sharedUsers.add(new GameProfile(tagCompound.hasKey("uuid") ? UUID.fromString(tagCompound.getString("uuid")) : null, tagCompound.getString("name")));
        }

        hackedUsers.clear();
        NBTTagList hackedList = tag.getTagList("HackedUsers", 10);
        for(int i = 0; i < hackedList.tagCount(); ++i) {
            NBTTagCompound tagCompound = hackedList.getCompoundTagAt(i);
            hackedUsers.add(new GameProfile(tagCompound.hasKey("uuid") ? UUID.fromString(tagCompound.getString("uuid")) : null, tagCompound.getString("name")));
        }
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack){
        return false;
    }

    @Override
    // upgrades in bottom, fuel in the rest.
    public int[] getAccessibleSlotsFromSide(int var1){
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, int j){
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, int j){
        return false;
    }

    @Override
    public void setText(int textFieldID, String text){
        textFieldText = text;
    }

    @Override
    public String getText(int textFieldID){
        return textFieldText;
    }

    /**
     * Returns true if the given player is allowed to interact with the covered area of this Security Station.
     * @param player
     * @return
     */
    public boolean doesAllowPlayer(EntityPlayer player){
        return rebootTimer > 0 || isPlayerOnWhiteList(player) || hasPlayerHacked(player);
    }

    public boolean isPlayerOnWhiteList(EntityPlayer player){
        for(int i = 0; i < sharedUsers.size(); i++) {
            GameProfile user = sharedUsers.get(i);
            if(gameProfileEquals(user, player.getGameProfile())) {
                if(user.getId() == null && player.getGameProfile().getId() != null) {
                    sharedUsers.set(i, player.getGameProfile());
                    Log.info("Legacy conversion: Security Station shared username '" + player.getCommandSenderName() + "' is now using UUID '" + player.getGameProfile().getId() + "'.");
                }
                return true;
            }
        }
        return false;
    }

    public boolean hasPlayerHacked(EntityPlayer player){
        for(int i = 0; i < hackedUsers.size(); i++) {
            GameProfile user = hackedUsers.get(i);
            if(gameProfileEquals(user, player.getGameProfile())) {
                if(user.getId() == null && player.getGameProfile().getId() != null) {
                    hackedUsers.set(i, player.getGameProfile());
                    Log.info("Legacy conversion: Security Station hacked username '" + player.getCommandSenderName() + "' is now using UUID '" + player.getGameProfile().getId() + "'.");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given slots are connected in the network. For this to be true both slots need to have a network component stored as well.
     * @param firstSlot
     * @param secondSlot
     * @return
     */
    public boolean connects(int firstSlot, int secondSlot){
        if(firstSlot < 0 || secondSlot < 0 || firstSlot >= 35 || secondSlot >= 35 || firstSlot == secondSlot || getStackInSlot(firstSlot) == null || getStackInSlot(secondSlot) == null) return false;
        for(int column = -1; column <= 1; column++) {
            for(int row = -1; row <= 1; row++) {
                if(firstSlot + row * 5 + column == secondSlot) {
                    if(firstSlot % 5 > 0 && firstSlot % 5 < 4 || secondSlot % 5 > 0 && secondSlot % 5 < 4 || secondSlot % 5 == firstSlot % 5) return true;
                }
            }
        }
        return false;
    }

    public boolean hasValidNetwork(){
        return validNetwork;
    }

    public enum EnumNetworkValidityProblem{
        NONE, NO_SUBROUTINE, NO_IO_PORT, NO_REGISTRY, TOO_MANY_SUBROUTINES, TOO_MANY_IO_PORTS, TOO_MANY_REGISTRIES, NO_CONNECTION_SUB_AND_IO_PORT, NO_CONNECTION_IO_PORT_AND_REGISTRY
    }

    /**
     * Method used to update the check of the validity of the network.
     * @return optional problem enum
     */
    public EnumNetworkValidityProblem checkForNetworkValidity(){
        validNetwork = false;
        int ioPortSlot = -1;
        int registrySlot = -1;
        int subroutineSlot = -1;
        for(int i = 0; i < 35; i++) {
            if(getStackInSlot(i) != null) {
                switch(getStackInSlot(i).getItemDamage()){
                    case ItemNetworkComponents.DIAGNOSTIC_SUBROUTINE:
                        if(subroutineSlot != -1) return EnumNetworkValidityProblem.TOO_MANY_SUBROUTINES;//only one subroutine per network
                        subroutineSlot = i;
                        break;
                    case ItemNetworkComponents.NETWORK_IO_PORT:
                        if(ioPortSlot != -1) return EnumNetworkValidityProblem.TOO_MANY_IO_PORTS;//only one subroutine per network
                        ioPortSlot = i;
                        break;
                    case ItemNetworkComponents.NETWORK_REGISTRY:
                        if(registrySlot != -1) return EnumNetworkValidityProblem.TOO_MANY_REGISTRIES;//only one subroutine per network
                        registrySlot = i;
                        break;
                }
            }
        }
        if(subroutineSlot == -1) return EnumNetworkValidityProblem.NO_SUBROUTINE;
        if(ioPortSlot == -1) return EnumNetworkValidityProblem.NO_IO_PORT;
        if(registrySlot == -1) return EnumNetworkValidityProblem.NO_REGISTRY;
        if(!traceComponent(subroutineSlot, ioPortSlot, new boolean[35])) return EnumNetworkValidityProblem.NO_CONNECTION_SUB_AND_IO_PORT;//check if there's a valid route between the subroutine/ioPort
        if(!traceComponent(ioPortSlot, registrySlot, new boolean[35])) return EnumNetworkValidityProblem.NO_CONNECTION_IO_PORT_AND_REGISTRY; // and ioPort/registry.
        validNetwork = true;
        return EnumNetworkValidityProblem.NONE;
    }

    private boolean traceComponent(int startSlot, int targetSlot, boolean[] slotsDone){
        for(int i = 0; i < 35; i++) {
            if(!slotsDone[i] && connects(startSlot, i)) {
                if(i == targetSlot) return true;
                slotsDone[i] = true;
                if(traceComponent(i, targetSlot, slotsDone)) return true;
            }
        }
        return false;
    }

    public int getDetectionChance(){
        return Math.min(100, 20 + 20 * getUpgrades(ItemMachineUpgrade.UPGRADE_ENTITY_TRACKER, getUpgradeSlots()));
    }

    public int getSecurityLevel(){
        return 1 + getUpgrades(ItemMachineUpgrade.UPGRADE_SECURITY, getUpgradeSlots());
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
    public boolean isGuiUseableByPlayer(EntityPlayer par1EntityPlayer){
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
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
