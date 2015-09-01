package pneumaticCraft.common.tileentity;

import java.util.Iterator;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.PneumaticCraftAPIHandler;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.lib.PneumaticValues;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyReceiver;
import cofh.api.tileentity.IEnergyInfo;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;

@Optional.InterfaceList({@Optional.Interface(iface = "cofh.api.energy.IEnergyReceiver", modid = ModIds.COFH_CORE), @Optional.Interface(iface = "cofh.api.tileentity.IEnergyInfo", modid = ModIds.COFH_CORE)})
public class TileEntityAerialInterface extends TileEntityPneumaticBase implements ISidedInventory, IFluidHandler,
        IMinWorkingPressure, IRedstoneControl, IEnergyReceiver, IEnergyInfo{
    private ItemStack[] inventory;

    private final int INVENTORY_SIZE = 4;

    private static final int UPGRADE_SLOT_START = 0;
    private static final int UPGRADE_SLOT_END = 3;

    @GuiSynced
    public String playerName = "";
    public String playerUUID = "";

    private Fluid curXpFluid;

    @GuiSynced
    public int redstoneMode;
    @GuiSynced
    public int feedMode;
    private boolean oldRedstoneStatus;
    private boolean updateNeighbours;
    @GuiSynced
    public boolean isConnectedToPlayer;
    private boolean dispenserUpgradeInserted;

    public TileEntityAerialInterface(){
        super(PneumaticValues.DANGER_PRESSURE_AERIAL_INTERFACE, PneumaticValues.MAX_PRESSURE_AERIAL_INTERFACE, PneumaticValues.VOLUME_AERIAL_INTERFACE);
        inventory = new ItemStack[INVENTORY_SIZE];
        setUpgradeSlots(new int[]{UPGRADE_SLOT_START, 1, 2, UPGRADE_SLOT_END});
        if(isRFAvailable()) initRF();
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
                if(energyRF != null) tickRF();
                addAir(-PneumaticValues.USAGE_AERIAL_INTERFACE, ForgeDirection.UNKNOWN);
                if(worldObj.getTotalWorldTime() % 40 == 0) dispenserUpgradeInserted = getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) > 0;
                if(worldObj.getTotalWorldTime() % 20 == 0) {
                    EntityPlayer player = getPlayer();
                    if(player != null && player.getAir() <= 280) {
                        player.setAir(player.getAir() + 20);
                        addAir(-4000, null);
                    }
                }
            }
            if(worldObj.getTotalWorldTime() % 20 == 0) getPlayerInventory();
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
        } else if(buttonID >= 1 && buttonID < 4) {
            feedMode = buttonID - 1;
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
            EntityPlayer player = PneumaticCraftUtils.getPlayerFromId(playerUUID);
            isConnectedToPlayer = player != null;
            return player;
        }
        return null;
    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
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
                            if(!player.inventory.addItemStackToInventory(remainingItem) && remainingItem.stackSize > 0) {
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

        return Blockss.aerialInterface.getUnlocalizedName();
    }

    @Override
    public int getInventoryStackLimit(){

        return 64;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){

        super.readFromNBT(tag);
        // Read in the ItemStacks in the inventory from NBT

        redstoneMode = tag.getInteger("redstoneMode");
        feedMode = tag.getInteger("feedMode");
        setPlayer(tag.getString("playerName"), tag.getString("playerUUID"));
        isConnectedToPlayer = tag.getBoolean("connected");
        if(tag.hasKey("curXpFluid")) curXpFluid = FluidRegistry.getFluid(tag.getString("curXpFluid"));
        if(energyRF != null) readRF(tag);

        NBTTagList tagList = tag.getTagList("Items", 10);
        inventory = new ItemStack[4];
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            byte slot = tagCompound.getByte("Slot");
            if(slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }
        dispenserUpgradeInserted = getUpgrades(ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE) > 0;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){

        super.writeToNBT(tag);
        // Write the ItemStacks in the inventory to NBT
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setInteger("feedMode", feedMode);
        tag.setString("playerName", playerName);
        tag.setString("playerUUID", playerUUID);
        if(curXpFluid != null) tag.setString("curXpFluid", curXpFluid.getName());
        if(energyRF != null) saveRF(tag);

        tag.setBoolean("connected", isConnectedToPlayer);
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
                    int foodValue = getFoodValue(itemstack);
                    int curFoodLevel = player.getFoodStats().getFoodLevel();
                    int feedMode = this.feedMode;
                    if(feedMode == 2) {
                        feedMode = player.getMaxHealth() - player.getHealth() > 0 ? 1 : 0;
                    }
                    switch(feedMode){
                        case 0:
                            return 20 - curFoodLevel >= foodValue * itemstack.stackSize;
                        case 1:
                            return 20 - curFoodLevel >= foodValue * (itemstack.stackSize - 1) + 1;
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

    @Override
    public float getMinWorkingPressure(){
        return PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE;
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }

    /**
     * RF integration
     */

    private Object energyRF;
    private static final int RF_PER_TICK = 1000;

    private boolean isRFAvailable(){
        return Loader.isModLoaded(ModIds.COFH_CORE);
    }

    private void initRF(){
        energyRF = new EnergyStorage(100000);
    }

    private void saveRF(NBTTagCompound tag){
        getEnergy().writeToNBT(tag);
    }

    private void readRF(NBTTagCompound tag){
        getEnergy().readFromNBT(tag);
    }

    private void tickRF(){

        if(getEnergyStored(null) > 0) {
            InventoryPlayer inv = getPlayerInventory();
            if(inv != null) {
                for(int i = 0; i < inv.getSizeInventory(); i++) {
                    ItemStack stack = inv.getStackInSlot(i);
                    if(stack != null && stack.getItem() instanceof IEnergyContainerItem) {
                        IEnergyContainerItem chargingItem = (IEnergyContainerItem)stack.getItem();
                        int energyLeft = getEnergyStored(null);
                        if(energyLeft > 0) {
                            getEnergy().extractEnergy(chargingItem.receiveEnergy(stack, energyLeft, false), false);
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    @Optional.Method(modid = ModIds.COFH_CORE)
    private EnergyStorage getEnergy(){
        return (EnergyStorage)energyRF;
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from){
        return true;
    }

    @Override
    public int getInfoEnergyPerTick(){
        return RF_PER_TICK;
    }

    @Override
    public int getInfoMaxEnergyPerTick(){
        return RF_PER_TICK;
    }

    @Override
    public int getInfoEnergyStored(){
        return getEnergy().getEnergyStored();
    }

    @Override
    public int getInfoMaxEnergyStored(){
        return getEnergy().getMaxEnergyStored();
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate){
        return getEnergy().receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(ForgeDirection from){
        return getEnergy().getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from){
        return getEnergy().getMaxEnergyStored();
    }

}
