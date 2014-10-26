package pneumaticCraft.common.tileentity;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.PneumaticCraftAPIHandler;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.Names;
import pneumaticCraft.lib.PneumaticValues;

import com.mojang.authlib.GameProfile;

public class TileEntityAerialInterface extends TileEntityPneumaticBase implements ISidedInventory, IFluidHandler{
    private ItemStack[] inventory;

    private final int INVENTORY_SIZE = 4;

    private static final int UPGRADE_SLOT_START = 0;
    private static final int UPGRADE_SLOT_END = 3;

    public String playerName = "";
    public String playerUUID = "";

    private Fluid curXpFluid;

    public int redstoneMode;
    private boolean oldRedstoneStatus;
    private boolean updateNeighbours;
    public boolean isConnectedToPlayer;
    private boolean dispenserUpgradeInserted;

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
            updateNeighbours();
        }
        if(!worldObj.isRemote) {
            if(getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE && isConnectedToPlayer) {
                addAir(-PneumaticValues.USAGE_AERIAL_INTERFACE, ForgeDirection.UNKNOWN);
                if(worldObj.getWorldTime() % 40 == 0) dispenserUpgradeInserted = getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) > 0;
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
        EntityPlayer player = getPlayer();
        return player != null ? player.inventory : null;
    }

    private EntityPlayer getPlayer(){
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
                return player;
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
        return inventory.length + (inventoryPlayer != null ? inventoryPlayer.getSizeInventory() + (dispenserUpgradeInserted ? 1 : 0) : 0);
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
            return inventoryPlayer != null ? slot == inventory.length + inventoryPlayer.getSizeInventory() ? null : inventoryPlayer.getStackInSlot(slot - 4) : null;
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
            EntityPlayer player = getPlayer();
            if(dispenserUpgradeInserted) {
                if(itemStack != null) {
                    int startValue = itemStack.stackSize;
                    while(itemStack.stackSize > 0) {
                        ItemStack remainingItem = itemStack.onFoodEaten(player.worldObj, player);
                        remainingItem = ForgeEventFactory.onItemUseFinish(player, itemStack, 0, remainingItem);
                        if(remainingItem != null && remainingItem.stackSize > 0 && (remainingItem != itemStack || remainingItem.stackSize != startValue)) {
                            if(!player.inventory.addItemStackToInventory(remainingItem)) {
                                player.dropPlayerItemWithRandomChoice(remainingItem, false);
                            }
                        }
                        if(itemStack.stackSize == startValue) break;
                    }
                }
            } else {
                InventoryPlayer inventoryPlayer = player != null ? player.inventory : null;
                if(inventoryPlayer != null) {
                    inventoryPlayer.setInventorySlotContents(slot - 4, itemStack);
                } else if(worldObj != null && !worldObj.isRemote) {
                    EntityItem item = new EntityItem(worldObj, xCoord, yCoord, zCoord, itemStack);
                    worldObj.spawnEntityInWorld(item);
                }
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
        if(nbtTagCompound.hasKey("curXpFluid")) curXpFluid = FluidRegistry.getFluid(nbtTagCompound.getString("curXpFluid"));

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
        if(curXpFluid != null) nbtTagCompound.setString("curXpFluid", curXpFluid.getName());

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
        } else if(dispenserUpgradeInserted) {
            return new int[]{44};
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
        EntityPlayer player = getPlayer();
        if(player == null) return false;
        if(getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE) {
            if(!dispenserUpgradeInserted || i >= 40 && i <= 43) {
                return i < 40 || itemstack != null && itemstack.getItem() instanceof ItemArmor && ((ItemArmor)itemstack.getItem()).armorType == 43 - i;
            } else {
                if(i == 4 + player.inventory.getSizeInventory() && getFoodValue(itemstack) > 0) {
                    int curFoodLevel = player.getFoodStats().getFoodLevel();
                    if(20 - curFoodLevel >= getFoodValue(itemstack) * itemstack.stackSize) {
                        return true;
                    }
                }
                return false;
            }
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

    private int getFoodValue(ItemStack item){
        return item != null && item.getItem() instanceof ItemFood ? ((ItemFood)item.getItem()).func_150905_g(item) : 0;
    }

    /*
     * 
     * ------------------------ Liquid XP handling
     * 
     */

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill){
        if(resource != null && canFill(from, resource.getFluid())) {
            EntityPlayer player = getPlayer();
            if(player != null) {
                int liquidToXP = PneumaticCraftAPIHandler.getInstance().liquidXPs.get(resource.getFluid());
                int xpPoints = resource.amount / liquidToXP;
                if(doFill) {
                    player.addExperience(xpPoints);
                    curXpFluid = resource.getFluid();
                }
                return xpPoints * liquidToXP;
            }
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain){
        if(resource != null && canDrain(from, resource.getFluid())) {
            EntityPlayer player = getPlayer();
            if(player != null) {
                int liquidToXP = PneumaticCraftAPIHandler.getInstance().liquidXPs.get(resource.getFluid());
                int pointsDrained = Math.min(getPlayerXP(player), resource.amount / liquidToXP);
                if(doDrain) addPlayerXP(player, -pointsDrained);
                return new FluidStack(resource.getFluid(), pointsDrained * liquidToXP);
            }
        }
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain){
        updateXpFluid();
        if(curXpFluid == null) return null;
        return drain(from, new FluidStack(curXpFluid, maxDrain), doDrain);
    }

    private void updateXpFluid(){
        if(curXpFluid == null) {
            Iterator<Fluid> fluids = PneumaticCraftAPIHandler.getInstance().liquidXPs.keySet().iterator();
            if(fluids.hasNext()) curXpFluid = fluids.next();
        }
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid){
        return fluid != null && PneumaticCraftAPIHandler.getInstance().liquidXPs.containsKey(fluid) && getPlayer() != null && getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE && dispenserUpgradeInserted;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid){
        return (fluid == null || PneumaticCraftAPIHandler.getInstance().liquidXPs.containsKey(fluid)) && getPlayer() != null && getPressure(ForgeDirection.UNKNOWN) > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE && dispenserUpgradeInserted;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from){
        updateXpFluid();
        if(curXpFluid != null) {
            EntityPlayer player = getPlayer();
            if(player != null) {
                return new FluidTankInfo[]{new FluidTankInfo(new FluidStack(curXpFluid, getPlayerXP(player) * PneumaticCraftAPIHandler.getInstance().liquidXPs.get(curXpFluid)), Integer.MAX_VALUE)};
            }
        }
        return null;
    }

    /**
     * This method is copied from OpenMods' OpenModsLib
     * https://github.com/OpenMods/OpenModsLib/blob/master/src/main/java/openmods/utils/EnchantmentUtils.java
     * @param player
     * @return
     */

    private static int getPlayerXP(EntityPlayer player){
        return (int)(getExperienceForLevel(player.experienceLevel) + player.experience * player.xpBarCap());
    }

    /**
     * This method is copied from OpenMods' OpenModsLib
     * https://github.com/OpenMods/OpenModsLib/blob/master/src/main/java/openmods/utils/EnchantmentUtils.java
     * @param player
     * @return
     */

    private static int getExperienceForLevel(int level){
        if(level == 0) {
            return 0;
        }
        if(level > 0 && level < 16) {
            return level * 17;
        } else if(level > 15 && level < 31) {
            return (int)(1.5 * Math.pow(level, 2) - 29.5 * level + 360);
        } else {
            return (int)(3.5 * Math.pow(level, 2) - 151.5 * level + 2220);
        }
    }

    /**
     * This method is copied from OpenMods' OpenModsLib
     * https://github.com/OpenMods/OpenModsLib/blob/master/src/main/java/openmods/utils/EnchantmentUtils.java
     * @param player
     * @return
     */

    private static void addPlayerXP(EntityPlayer player, int amount){
        int experience = getPlayerXP(player) + amount;
        player.experienceTotal = experience;
        player.experienceLevel = getLevelForExperience(experience);
        int expForLevel = getExperienceForLevel(player.experienceLevel);
        player.experience = (float)(experience - expForLevel) / (float)player.xpBarCap();
    }

    /**
     * This method is copied from OpenMods' OpenModsLib
     * https://github.com/OpenMods/OpenModsLib/blob/master/src/main/java/openmods/utils/EnchantmentUtils.java
     * @param player
     * @return
     */

    private static int getLevelForExperience(int experience){
        int i = 0;
        while(getExperienceForLevel(i) <= experience) {
            i++;
        }
        return i - 1;
    }

}
