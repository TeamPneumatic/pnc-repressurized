package me.desht.pneumaticcraft.common.tileentity;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

//TODO TE dep @Optional.InterfaceList({@Optional.Interface(iface = "cofh.api.energy.IEnergyReceiver", modid = ModIds.COFH_CORE), @Optional.Interface(iface = "cofh.api.tileentity.IEnergyInfo", modid = ModIds.COFH_CORE)})
public class TileEntityAerialInterface extends TileEntityPneumaticBase implements IMinWorkingPressure, IRedstoneControl, IComparatorSupport {
    @GuiSynced
    public String playerName = "";
    public String playerUUID = "";

    private Fluid curXpFluid;

    @GuiSynced
    public int redstoneMode;
    @GuiSynced
    public int feedMode = 0;
    private boolean oldRedstoneStatus;
    private boolean updateNeighbours;
    @GuiSynced
    public boolean isConnectedToPlayer;
    private boolean dispenserUpgradeInserted;

    private PlayerInvWrapper playerInvWrapper;
    private PlayerArmorInvWrapper playerArmorInvWrapper;
    private PlayerExperienceHandler playerExperienceHandler;
    private PlayerFoodHandler playerFoodHandler;

    public TileEntityAerialInterface() {
        super(PneumaticValues.DANGER_PRESSURE_AERIAL_INTERFACE, PneumaticValues.MAX_PRESSURE_AERIAL_INTERFACE, PneumaticValues.VOLUME_AERIAL_INTERFACE, 4);
        addApplicableUpgrade(EnumUpgrade.DISPENSER);

        if (isRFAvailable()) initRF();
    }

    public void setPlayer(GameProfile gameProfile) {
        setPlayer(gameProfile.getName(), gameProfile.getId() != null ? gameProfile.getId().toString() : "");
    }

    private void setPlayer(String username, String uuid) {
        playerName = username;
        playerUUID = uuid;
        updateNeighbours = true;
        InventoryPlayer inv = getPlayerInventory();
        playerInvWrapper = inv == null ? null : new PlayerInvWrapper(getPlayerInventory());
        playerArmorInvWrapper = inv == null ? null : new PlayerArmorInvWrapper(getPlayerInventory());
        playerExperienceHandler = new PlayerExperienceHandler();
        playerFoodHandler = new PlayerFoodHandler();
    }

    @Override
    public void update() {
        if (!getWorld().isRemote && updateNeighbours) {
            updateNeighbours = false;
            updateNeighbours();
        }
        if (!getWorld().isRemote) {
            if (getPressure() > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE && isConnectedToPlayer) {
                if (energyRF != null) tickRF();
                addAir(-PneumaticValues.USAGE_AERIAL_INTERFACE);
                if (getWorld().getTotalWorldTime() % 40 == 0)
                    dispenserUpgradeInserted = getUpgrades(EnumUpgrade.DISPENSER) > 0;
                if (getWorld().getTotalWorldTime() % 20 == 0) {
                    EntityPlayer player = getPlayer();
                    if (player != null && player.getAir() <= 280) {
                        player.setAir(player.getAir() + 20);
                        addAir(-4000);
                    }
                }
            }
            if (getWorld().getTotalWorldTime() % 20 == 0) getPlayerInventory();
        }

        if (oldRedstoneStatus != shouldEmitRedstone()) {
            oldRedstoneStatus = shouldEmitRedstone();
            updateNeighbours = true;
        }

        super.update();

    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player) {
        if (buttonID == 0) {
            redstoneMode++;
            if (redstoneMode > 1) redstoneMode = 0;
            // updateNeighbours();
        } else if (buttonID >= 1 && buttonID < 4) {
            feedMode = buttonID - 1;
        }
    }

    private boolean shouldEmitRedstone() {
        switch (redstoneMode) {
            case 0:
                return false;
            case 1:
                return isConnectedToPlayer;
        }
        return false;
    }

    private InventoryPlayer getPlayerInventory() {
        EntityPlayer player = getPlayer();
        return player != null ? player.inventory : null;
    }

    private EntityPlayer getPlayer() {
        if (!getWorld().isRemote) {
            EntityPlayer player = PneumaticCraftUtils.getPlayerFromId(playerUUID);
            isConnectedToPlayer = player != null;
            return player;
        }
        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && dispenserUpgradeInserted)
                || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(playerInvWrapper);
            switch (facing) {
                case UP:
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(playerArmorInvWrapper);
                case DOWN:
                    return super.getCapability(capability, facing);
                default:
                    return dispenserUpgradeInserted ?
                            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(playerFoodHandler) :
                            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(playerInvWrapper);
            }
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && dispenserUpgradeInserted) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(playerExperienceHandler);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public String getName() {
        return Blockss.AERIAL_INTERFACE.getUnlocalizedName();
    }


    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        redstoneMode = tag.getInteger("redstoneMode");
        feedMode = tag.getInteger("feedMode");
        setPlayer(tag.getString("playerName"), tag.getString("playerUUID"));
        isConnectedToPlayer = tag.getBoolean("connected");
        if (tag.hasKey("curXpFluid")) curXpFluid = FluidRegistry.getFluid(tag.getString("curXpFluid"));
        if (energyRF != null) readRF(tag);

        dispenserUpgradeInserted = getUpgrades(EnumUpgrade.DISPENSER) > 0;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        // Write the ItemStacks in the inventory to NBT
        tag.setInteger("redstoneMode", redstoneMode);
        tag.setInteger("feedMode", feedMode);
        tag.setString("playerName", playerName);
        tag.setString("playerUUID", playerUUID);
        if (curXpFluid != null) tag.setString("curXpFluid", curXpFluid.getName());
        if (energyRF != null) saveRF(tag);
        tag.setBoolean("connected", isConnectedToPlayer);
        return tag;
    }


    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE;
    }

    @Override
    public int getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public int getComparatorValue() {
        return shouldEmitRedstone() ? 15 : 0;
    }

    /**
     * RF integration
     */

    private Object energyRF;
    private static final int RF_PER_TICK = 1000;

    private boolean isRFAvailable() {
        return Loader.isModLoaded(ModIds.COFH_CORE);
    }

    private void initRF() {
        // TODO TE dep   energyRF = new EnergyStorage(100000);
    }

    private void saveRF(NBTTagCompound tag) {
        //TODO TE dep     getEnergy().writeToNBT(tag);
    }

    private void readRF(NBTTagCompound tag) {
        //TODO TE dep    getEnergy().readFromNBT(tag);
    }

    private void tickRF() {
        /*TODO TE dep
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
          }*/
    }

    private class PlayerFoodHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            EntityPlayer player = getPlayer();
            if (player == null || getFoodValue(stack) <= 0) return stack;
            if (!okToFeed(stack, player)) return stack;

            int startValue = stack.getCount();
            ItemStack remainingItem = stack;
            while (stack.getCount() > 0) {
                remainingItem = stack.onItemUseFinish(player.world, player);
                remainingItem = ForgeEventFactory.onItemUseFinish(player, stack, 0, remainingItem);
                if (remainingItem.getCount() > 0 && (remainingItem != stack || remainingItem.getCount() != startValue)) {
                    if (!player.inventory.addItemStackToInventory(remainingItem) && remainingItem.getCount() > 0) {
                        player.dropItem(remainingItem, false);
                    }
                }
                if (stack.getCount() == startValue) break;
            }
            return remainingItem;
        }

        private boolean okToFeed(@Nonnull ItemStack stack, EntityPlayer player) {
            int foodValue = getFoodValue(stack);
            int curFoodLevel = player.getFoodStats().getFoodLevel();
            if (feedMode == 2) {
                feedMode = player.getMaxHealth() - player.getHealth() > 0 ? 1 : 0;
            }
            switch (feedMode) {
                case 0:
                    return 20 - curFoodLevel >= foodValue * stack.getCount();
                case 1:
                    return 20 - curFoodLevel >= foodValue * (stack.getCount() - 1) + 1;
            }
            return false;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        private int getFoodValue(ItemStack item) {
            return item.getItem() instanceof ItemFood ? ((ItemFood) item.getItem()).getHealAmount(item) : 0;
        }
    }

    private class PlayerExperienceHandler implements IFluidHandler {
        private void updateXpFluid() {
            if (curXpFluid == null) {
                Iterator<Fluid> fluids = PneumaticCraftAPIHandler.getInstance().liquidXPs.keySet().iterator();
                if (fluids.hasNext()) curXpFluid = fluids.next();
            }
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            updateXpFluid();
            if (curXpFluid != null) {
                EntityPlayer player = getPlayer();
                if (player != null) {
                    return new FluidTankProperties[] {
                        new FluidTankProperties(
                                new FluidStack(curXpFluid, getPlayerXP(player) * PneumaticCraftAPIHandler.getInstance().liquidXPs.get(curXpFluid)),
                                Integer.MAX_VALUE)
                    };
                }
            }
            return null;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource != null && canFill(resource.getFluid())) {
                EntityPlayer player = getPlayer();
                if (player != null) {
                    int liquidToXP = PneumaticCraftAPIHandler.getInstance().liquidXPs.get(resource.getFluid());
                    int xpPoints = resource.amount / liquidToXP;
                    if (doFill) {
                        player.addExperience(xpPoints);
                        curXpFluid = resource.getFluid();
                    }
                    return xpPoints * liquidToXP;
                }
            }
            return 0;
        }

        private boolean canFill(Fluid fluid) {
            return dispenserUpgradeInserted && fluid != null
                    && PneumaticCraftAPIHandler.getInstance().liquidXPs.containsKey(fluid)
                    && getPlayer() != null
                    && getPressure() > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource != null && canDrain(resource.getFluid())) {
                EntityPlayer player = getPlayer();
                if (player != null) {
                    int liquidToXP = PneumaticCraftAPIHandler.getInstance().liquidXPs.get(resource.getFluid());
                    int pointsDrained = Math.min(getPlayerXP(player), resource.amount / liquidToXP);
                    if (doDrain) addPlayerXP(player, -pointsDrained);
                    return new FluidStack(resource.getFluid(), pointsDrained * liquidToXP);
                }
            }
            return null;
        }

        private boolean canDrain(Fluid fluid) {
            return dispenserUpgradeInserted
                    && (fluid == null || PneumaticCraftAPIHandler.getInstance().liquidXPs.containsKey(fluid))
                    && getPlayer() != null
                    && getPressure() > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            updateXpFluid();
            if (curXpFluid == null) return null;
            return drain(new FluidStack(curXpFluid, maxDrain), doDrain);
        }


        /**
         * This method is copied from OpenMods' OpenModsLib
         * https://github.com/OpenMods/OpenModsLib/blob/master/src/main/java/openmods/utils/EnchantmentUtils.java
         *
         * @param player
         * @return
         */
        private int getPlayerXP(EntityPlayer player) {
            return (int) (getExperienceForLevel(player.experienceLevel) + player.experience * player.xpBarCap());
        }

        /**
         * This method is copied from OpenMods' OpenModsLib
         * https://github.com/OpenMods/OpenModsLib/blob/master/src/main/java/openmods/utils/EnchantmentUtils.java
         *
         * @param level
         * @return
         */

        private int getExperienceForLevel(int level) {
            if (level == 0) {
                return 0;
            }
            if (level > 0 && level < 16) {
                return level * 17;
            } else if (level > 15 && level < 31) {
                return (int) (1.5 * Math.pow(level, 2) - 29.5 * level + 360);
            } else {
                return (int) (3.5 * Math.pow(level, 2) - 151.5 * level + 2220);
            }
        }

        /**
         * This method is copied from OpenMods' OpenModsLib
         * https://github.com/OpenMods/OpenModsLib/blob/master/src/main/java/openmods/utils/EnchantmentUtils.java
         *
         * @param player
         * @return
         */

        private void addPlayerXP(EntityPlayer player, int amount) {
            int experience = getPlayerXP(player) + amount;
            player.experienceTotal = experience;
            player.experienceLevel = getLevelForExperience(experience);
            int expForLevel = getExperienceForLevel(player.experienceLevel);
            player.experience = (float) (experience - expForLevel) / (float) player.xpBarCap();
        }

        /**
         * This method is copied from OpenMods' OpenModsLib
         * https://github.com/OpenMods/OpenModsLib/blob/master/src/main/java/openmods/utils/EnchantmentUtils.java
         *
         * @param experience
         * @return
         */

        private int getLevelForExperience(int experience) {
            int i = 0;
            while (getExperienceForLevel(i) <= experience) {
                i++;
            }
            return i - 1;
        }
    }

    /* @Optional.Method(modid = ModIds.COFH_CORE)
     private EnergyStorage getEnergy(){
         return (EnergyStorage)energyRF;
     }

     @Override
     public boolean canConnectEnergy(EnumFacing from){
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
     public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate){
         return getEnergy().receiveEnergy(maxReceive, simulate);
     }

     @Override
     public int getEnergyStored(EnumFacing from){
         return getEnergy().getEnergyStored();
     }

     @Override
     public int getMaxEnergyStored(EnumFacing from){
         return getEnergy().getMaxEnergyStored();
     }*/
}
