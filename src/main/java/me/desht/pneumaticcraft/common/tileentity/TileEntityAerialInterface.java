package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.thirdparty.baubles.Baubles;
import me.desht.pneumaticcraft.common.tileentity.SideConfigurator.RelativeFace;
import me.desht.pneumaticcraft.common.util.EnchantmentUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileEntityAerialInterface extends TileEntityPneumaticBase
        implements IMinWorkingPressure, IRedstoneControl, IComparatorSupport, ISideConfigurable {

    private static final int ENERGY_CAPACITY = 100000;
    private static final int RF_PER_TICK = 1000;

    @GameRegistry.ObjectHolder("baubles:ring")
    private static final Item BAUBLES_RING = null;

    @GuiSynced
    @DescSynced
    public String playerName = "";
    @DescSynced
    private String playerUUID = "";

    private Fluid curXpFluid;
    @DescSynced
    public int curXPFluidIndex = -1;  // index into PneumaticCraftAPIHandler.availableLiquidXPs, -1 = disabled

    @GuiSynced
    public int redstoneMode;
    @GuiSynced
    public int feedMode = 0;
    private boolean oldRedstoneStatus;
    private boolean updateNeighbours;
    @GuiSynced
    public boolean isConnectedToPlayer = false;
    @GuiSynced
    public boolean dispenserUpgradeInserted;

    private final SideConfigurator<IItemHandler> itemHandlerSideConfigurator;

    private final PlayerExperienceHandler playerExperienceHandler;
    private final PlayerFoodHandler playerFoodHandler;
    private WeakReference<EntityPlayer> playerRef = new WeakReference<>(null);

    private final PneumaticEnergyStorage energyStorage;
    private final List<Integer> chargeableSlots = new ArrayList<>();

    public TileEntityAerialInterface() {
        super(PneumaticValues.DANGER_PRESSURE_AERIAL_INTERFACE, PneumaticValues.MAX_PRESSURE_AERIAL_INTERFACE, PneumaticValues.VOLUME_AERIAL_INTERFACE, 4);
        addApplicableUpgrade(EnumUpgrade.DISPENSER);

        PlayerMainInvHandler playerMainInvHandler = new PlayerMainInvHandler();
        PlayerArmorInvHandler playerArmorInvHandler = new PlayerArmorInvHandler();
        PlayerOffhandInvHandler playerOffhandInvHandler = new PlayerOffhandInvHandler();
        PlayerEnderInvHandler playerEnderInvHandler = new PlayerEnderInvHandler();
        playerExperienceHandler = new PlayerExperienceHandler();
        playerFoodHandler = new PlayerFoodHandler();

        itemHandlerSideConfigurator = new SideConfigurator<>("items", this, 5);
        itemHandlerSideConfigurator.registerHandler("mainInv", new ItemStack(Blocks.CHEST),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, playerMainInvHandler,
                RelativeFace.FRONT, RelativeFace.BACK, RelativeFace.LEFT, RelativeFace.RIGHT);
        itemHandlerSideConfigurator.registerHandler("armorInv", new ItemStack(Itemss.PNEUMATIC_CHESTPLATE),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, playerArmorInvHandler,
                RelativeFace.TOP, RelativeFace.BOTTOM);
        itemHandlerSideConfigurator.registerHandler("offhandInv", new ItemStack(Items.SHIELD),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, playerOffhandInvHandler);
        itemHandlerSideConfigurator.registerHandler("enderInv", new ItemStack(Blocks.ENDER_CHEST),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, playerEnderInvHandler);
        if (Baubles.available) {
            itemHandlerSideConfigurator.registerHandler("baublesInv", new ItemStack(BAUBLES_RING),
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new PlayerInvHandler() {
                        @Override
                        protected IItemHandler getInvWrapper() {
                            return getPlayer().getCapability(Baubles.CAPABILITY_BAUBLES, null);
                        }
                    });
        }

        energyStorage = new PneumaticEnergyStorage(ENERGY_CAPACITY);
    }

    public void setPlayer(EntityPlayer player) {
        playerRef = new WeakReference<>(player);
        boolean old = isConnectedToPlayer;
        if (player == null) {
            isConnectedToPlayer = false;
        } else {
            setPlayer(player.getGameProfile().getName(), player.getGameProfile().getId().toString());
            isConnectedToPlayer = true;
        }
        if (old != isConnectedToPlayer) {
            updateNeighbours = true;
            scanForChargeableItems();
        }
    }

    private void setPlayer(String username, String uuid) {
        if (!playerUUID.equals(uuid)) {
            updateNeighbours = true;
        }
        playerName = username;
        playerUUID = uuid;
    }

    @Override
    protected void onUpgradesChanged() {
        super.onUpgradesChanged();
        boolean old = dispenserUpgradeInserted;
        dispenserUpgradeInserted = getUpgrades(EnumUpgrade.DISPENSER) > 0;
        if (old != dispenserUpgradeInserted) {
            updateNeighbours = true;
        }
    }

    @Override
    protected void onFirstServerUpdate() {
        super.onFirstServerUpdate();
        SideConfigurator.validateBlockRotation(this);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote && updateNeighbours) {
            updateNeighbours = false;
            updateNeighbours();
        }
        if (!getWorld().isRemote) {
            if (getPressure() > PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE && isConnectedToPlayer) {
                addAir(-PneumaticValues.USAGE_AERIAL_INTERFACE);

                if ((getWorld().getTotalWorldTime() & 0x3f) == 0) {
                    scanForChargeableItems();
                }
                supplyEnergyToPlayer();

                // check every 16 ticks
                if ((getWorld().getTotalWorldTime() & 0xf) == 0) {
                    EntityPlayer player = getPlayer();
                    if (player != null && player.getAir() <= 280) {
                        player.setAir(player.getAir() + 16);
                        addAir(-80);  // 5 pneumatic air per player air
                    }
                }
            }
            if ((getWorld().getTotalWorldTime() & 0xf) == 0 && !playerUUID.isEmpty()) {
                setPlayer(PneumaticCraftUtils.getPlayerFromId(playerUUID));
            }
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
        } else if (buttonID == 4) {
            curXPFluidIndex++;
            List<Fluid> available = PneumaticCraftAPIHandler.getInstance().availableLiquidXPs;
            if (curXPFluidIndex >= available.size()) {
                curXPFluidIndex = -1;
            }
            if (curXPFluidIndex >= 0 && curXPFluidIndex < available.size()) {
                curXpFluid = available.get(curXPFluidIndex);
            } else {
                curXpFluid = null;
            }
        } else if (itemHandlerSideConfigurator.handleButtonPress(buttonID)) {
            updateNeighbours = true;
        }
    }

    public boolean shouldEmitRedstone() {
        switch (redstoneMode) {
            case 0:
                return false;
            case 1:
                return isConnectedToPlayer;
        }
        return false;
    }

    private EntityPlayer getPlayer() {
        return playerRef.get();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return ((isConnectedToPlayer &&
                (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && dispenserUpgradeInserted && curXpFluid != null)
                || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) && itemHandlerSideConfigurator.getHandler(facing) != null)
                || capability == CapabilityEnergy.ENERGY
                || super.hasCapability(capability, facing);

    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (dispenserUpgradeInserted) {
                if (facing == EnumFacing.UP && ConfigHandler.machineProperties.aerialInterfaceArmorCompat) {
                    // https://github.com/TeamPneumatic/pnc-repressurized/issues/278
                    IItemHandler handler = itemHandlerSideConfigurator.getHandler(EnumFacing.UP);
                    if (handler instanceof PlayerArmorInvHandler) {
                        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler);
                    }
                }
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(playerFoodHandler);
            } else {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandlerSideConfigurator.getHandler(facing));
            }
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && dispenserUpgradeInserted && curXpFluid != null) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(playerExperienceHandler);
        } else if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(energyStorage);
        } else {
            return super.getCapability(capability, facing);
        }
    }

    @Override
    public String getName() {
        return Blockss.AERIAL_INTERFACE.getTranslationKey();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        redstoneMode = tag.getInteger("redstoneMode");
        feedMode = tag.getInteger("feedMode");
        setPlayer(tag.getString("playerName"), tag.getString("playerUUID"));
        curXpFluid = tag.hasKey("curXpFluid") ? FluidRegistry.getFluid(tag.getString("curXpFluid")) : null;
        energyStorage.readFromNBT(tag);

        curXPFluidIndex = curXpFluid == null ? -1 : PneumaticCraftAPIHandler.getInstance().availableLiquidXPs.indexOf(curXpFluid);
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
        energyStorage.writeToNBT(tag);
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

    private void scanForChargeableItems() {
        chargeableSlots.clear();
        if (isConnectedToPlayer) {
            InventoryPlayer inv = playerRef.get().inventory;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                if (inv.getStackInSlot(i).hasCapability(CapabilityEnergy.ENERGY, null)) {
                    chargeableSlots.add(i);
                }
            }
        }
    }

    private void supplyEnergyToPlayer() {
        if (!isConnectedToPlayer) return;

        InventoryPlayer inv = playerRef.get().inventory;
        for (int slot : chargeableSlots) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
                IEnergyStorage receivingStorage = stack.getCapability(CapabilityEnergy.ENERGY, null);
                int energyLeft = energyStorage.getEnergyStored();
                if (energyLeft > 0) {
                    energyStorage.extractEnergy(receivingStorage.receiveEnergy(Math.min(energyLeft, RF_PER_TICK), false), false);
                }
                if (energyStorage.getEnergyStored() == 0) {
                    break;
                }
            }
        }

        if (Baubles.available && energyStorage.getEnergyStored() > 0) {
            Baubles.chargeBaubles(getPlayer(), energyStorage, RF_PER_TICK);
        }
    }

    private static final List<String> REDSTONE_LABELS = ImmutableList.of(
            "gui.tab.redstoneBehaviour.button.never",
            "gui.tab.redstoneBehaviour.aerialInterface.button.playerConnected"
    );

    @Override
    protected List<String> getRedstoneButtonLabels() {
        return REDSTONE_LABELS;
    }

    @Override
    public List<SideConfigurator> getSideConfigurators() {
        return Collections.singletonList(itemHandlerSideConfigurator);
    }

    @Override
    public EnumFacing byIndex() {
        return getRotation();
    }

    private abstract class PlayerInvHandler implements IItemHandler {
        /**
         * Get an item handler for the current player, which must be non-null.
         * @return an item handler for the appropriate part of the player's inventory
         */
        protected abstract IItemHandler getInvWrapper();

        @Override
        public int getSlots() {
            return playerRef.get() == null ? 0 : getInvWrapper().getSlots();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return playerRef.get() == null ? ItemStack.EMPTY : getInvWrapper().getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return playerRef.get() == null ? stack : getInvWrapper().insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return playerRef.get() == null ? ItemStack.EMPTY : getInvWrapper().extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return playerRef.get() == null ? 1 : getInvWrapper().getSlotLimit(slot);
        }
    }

    private class PlayerMainInvHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper() {
            return new PlayerMainInvWrapper(getPlayer().inventory);
        }
    }

    private class PlayerArmorInvHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper() {
            return new PlayerArmorInvWrapper(getPlayer().inventory);
        }
    }

    private class PlayerOffhandInvHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper() {
            return new PlayerOffhandInvWrapper(getPlayer().inventory);
        }
    }

    private class PlayerEnderInvHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper() {
            return new InvWrapper(getPlayer().getInventoryEnderChest());
        }
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

            if (simulate) return ItemStack.EMPTY;

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
            return remainingItem.getCount() > 0 ? remainingItem : ItemStack.EMPTY;
        }

        private boolean okToFeed(@Nonnull ItemStack stack, EntityPlayer player) {
            int foodValue = getFoodValue(stack);
            int curFoodLevel = player.getFoodStats().getFoodLevel();
            int tmpFeedMode = feedMode;
            if (tmpFeedMode == 2) {
                tmpFeedMode = player.getMaxHealth() - player.getHealth() > 0 ? 1 : 0;
            }
            switch (tmpFeedMode) {
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

        @Override
        public IFluidTankProperties[] getTankProperties() {
            if (curXpFluid != null) {
                EntityPlayer player = getPlayer();
                if (player != null) {
                    return new FluidTankProperties[] {
                        new FluidTankProperties(
                                new FluidStack(curXpFluid, EnchantmentUtils.getPlayerXP(player) * PneumaticCraftAPIHandler.getInstance().liquidXPs.get(curXpFluid)),
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
                    int pointsAdded = resource.amount / liquidToXP;
                    if (doFill) {
                        player.addExperience(pointsAdded);
                    }
                    return pointsAdded * liquidToXP;
                }
            }
            return 0;
        }

        private boolean canFill(Fluid fluid) {
            return dispenserUpgradeInserted && fluid != null && fluid == curXpFluid
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
                    int pointsDrained = Math.min(EnchantmentUtils.getPlayerXP(player), resource.amount / liquidToXP);
                    if (doDrain) EnchantmentUtils.addPlayerXP(player, -pointsDrained);
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
            if (curXpFluid == null) return null;
            return drain(new FluidStack(curXpFluid, maxDrain), doDrain);
        }
    }
}
