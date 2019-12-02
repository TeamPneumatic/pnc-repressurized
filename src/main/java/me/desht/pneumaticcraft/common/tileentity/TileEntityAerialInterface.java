package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModTileEntityTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerEnergy;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.thirdparty.baubles.Baubles;
import me.desht.pneumaticcraft.common.tileentity.SideConfigurator.RelativeFace;
import me.desht.pneumaticcraft.common.util.EnchantmentUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade.DISPENSER;

public class TileEntityAerialInterface extends TileEntityPneumaticBase
        implements IMinWorkingPressure, IRedstoneControl, IComparatorSupport, ISideConfigurable, INamedContainerProvider {

    private static final int ENERGY_CAPACITY = 100000;
    private static final int RF_PER_TICK = 1000;

    @ObjectHolder("baubles:ring")
    private static Item BAUBLES_RING = null;

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
    public FeedMode feedMode = FeedMode.FULLY_UTILIZE;
    private boolean oldRedstoneStatus;
    private boolean updateNeighbours;
    @GuiSynced
    public boolean isConnectedToPlayer = false;
    @GuiSynced
    public boolean dispenserUpgradeInserted;

    private final SideConfigurator<IItemHandler> itemHandlerSideConfigurator;

    private final PlayerExperienceHandler playerExperienceHandler = new PlayerExperienceHandler();
    private final LazyOptional<IFluidHandler> playerExpCap = LazyOptional.of(() -> playerExperienceHandler);
    private final PlayerFoodHandler playerFoodHandler = new PlayerFoodHandler();
    private final LazyOptional<IItemHandler> playerFoodCap = LazyOptional.of(() -> playerFoodHandler);
    private final PneumaticEnergyStorage energyStorage = new PneumaticEnergyStorage(ENERGY_CAPACITY);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energyStorage);

    private WeakReference<PlayerEntity> playerRef = new WeakReference<>(null);

    private final List<Integer> chargeableSlots = new ArrayList<>();

    public TileEntityAerialInterface() {
        super(ModTileEntityTypes.AERIAL_INTERFACE, PneumaticValues.DANGER_PRESSURE_AERIAL_INTERFACE, PneumaticValues.MAX_PRESSURE_AERIAL_INTERFACE, PneumaticValues.VOLUME_AERIAL_INTERFACE, 4);
        addApplicableUpgrade(DISPENSER);

        PlayerMainInvHandler playerMainInvHandler = new PlayerMainInvHandler();
        PlayerArmorInvHandler playerArmorInvHandler = new PlayerArmorInvHandler();
        PlayerOffhandInvHandler playerOffhandInvHandler = new PlayerOffhandInvHandler();
        PlayerEnderInvHandler playerEnderInvHandler = new PlayerEnderInvHandler();

        itemHandlerSideConfigurator = new SideConfigurator<>("items", this);
        itemHandlerSideConfigurator.registerHandler("mainInv", new ItemStack(Blocks.CHEST),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> playerMainInvHandler,
                RelativeFace.FRONT, RelativeFace.BACK, RelativeFace.LEFT, RelativeFace.RIGHT);
        itemHandlerSideConfigurator.registerHandler("armorInv", new ItemStack(ModItems.PNEUMATIC_CHESTPLATE),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> playerArmorInvHandler,
                RelativeFace.TOP, RelativeFace.BOTTOM);
        itemHandlerSideConfigurator.registerHandler("offhandInv", new ItemStack(Items.SHIELD),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> playerOffhandInvHandler);
        itemHandlerSideConfigurator.registerHandler("enderInv", new ItemStack(Blocks.ENDER_CHEST),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> playerEnderInvHandler);
//        if (Baubles.available) {
//            itemHandlerSideConfigurator.registerHandler("baublesInv", new ItemStack(BAUBLES_RING),
//                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new PlayerBaublesHandler());
//        }
    }

    public void setPlayer(PlayerEntity player) {
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
    public void tick() {
        if (!getWorld().isRemote && updateNeighbours) {
            updateNeighbours = false;
            updateNeighbours();
        }
        if (!getWorld().isRemote) {
            if (getPressure() >= getMinWorkingPressure() && isConnectedToPlayer) {
                addAir(-PneumaticValues.USAGE_AERIAL_INTERFACE);

                if ((getWorld().getGameTime() & 0x3f) == 0) {
                    scanForChargeableItems();
                }
                supplyEnergyToPlayer();

                // check every 16 ticks
                if ((getWorld().getGameTime() & 0xf) == 0) {
                    PlayerEntity player = getPlayer();
                    if (player != null && player.getAir() <= 280) {
                        player.setAir(player.getAir() + 16);
                        addAir(-80);  // 5 pneumatic air per player air
                    }
                }
            }
            if ((getWorld().getGameTime() & 0xf) == 0 && !playerUUID.isEmpty()) {
                setPlayer(PneumaticCraftUtils.getPlayerFromId(playerUUID));
            }
        }

        if (oldRedstoneStatus != shouldEmitRedstone()) {
            oldRedstoneStatus = shouldEmitRedstone();
            updateNeighbours = true;
        }

        super.tick();
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        if (tag.equals(IGUIButtonSensitive.REDSTONE_TAG)) {
            redstoneMode++;
            if (redstoneMode > 1) redstoneMode = 0;
        } else if (tag.equals("xpType")) {
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
        } else if (tag.startsWith("SideConf") && itemHandlerSideConfigurator.handleButtonPress(tag)) {
            updateNeighbours = true;
        } else {
            try {
                feedMode = FeedMode.valueOf(tag);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public IItemHandlerModifiable getPrimaryInventory() {
        return null;
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

    private PlayerEntity getPlayer() {
        return playerRef.get();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (dispenserUpgradeInserted) {
                return playerFoodCap.cast();
            } else {
                return itemHandlerSideConfigurator.getHandler(side).cast();
            }
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && dispenserUpgradeInserted && curXpFluid != null) {
            return playerExpCap.cast();
        } else if (cap == CapabilityEnergy.ENERGY) {
            return energyCap.cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        redstoneMode = tag.getInt("redstoneMode");
        feedMode = FeedMode.valueOf(tag.getString("feedMode"));
        setPlayer(tag.getString("playerName"), tag.getString("playerUUID"));
        curXpFluid = tag.contains("curXpFluid") ? ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("curXpFluid"))) : null;
        energyStorage.readFromNBT(tag);

        curXPFluidIndex = curXpFluid == null ? -1 : PneumaticCraftAPIHandler.getInstance().availableLiquidXPs.indexOf(curXpFluid);
        dispenserUpgradeInserted = getUpgrades(EnumUpgrade.DISPENSER) > 0;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        // Write the ItemStacks in the inventory to NBT
        tag.putInt("redstoneMode", redstoneMode);
        tag.putString("feedMode", feedMode.toString());
        tag.putString("playerName", playerName);
        tag.putString("playerUUID", playerUUID);
        if (curXpFluid != null) tag.putString("curXpFluid", curXpFluid.getRegistryName().toString());
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
            PlayerInventory inv = playerRef.get().inventory;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                if (inv.getStackInSlot(i).getCapability(CapabilityEnergy.ENERGY).isPresent()) {
                    chargeableSlots.add(i);
                }
            }
        }
    }

    private void supplyEnergyToPlayer() {
        if (!isConnectedToPlayer) return;

        PlayerInventory inv = playerRef.get().inventory;
        for (int slot : chargeableSlots) {
            ItemStack stack = inv.getStackInSlot(slot);
            int energyLeft = stack.getCapability(CapabilityEnergy.ENERGY).map(receivingStorage -> {
                int stored = energyStorage.getEnergyStored();
                if (stored > 0) {
                    energyStorage.extractEnergy(receivingStorage.receiveEnergy(Math.min(stored, RF_PER_TICK), false), false);
                }
                return energyStorage.getEnergyStored();
            }).orElse(energyStorage.getEnergyStored());
            if (energyLeft == 0) break;
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
    public Direction byIndex() {
        return getRotation();
    }

    @Override
    public ITextComponent getDisplayName() {
        return getDisplayNameInternal();
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerEnergy(ModContainerTypes.AERIAL_INTERFACE, i, playerInventory, getPos());
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
            if (playerRef.get() == null || getPressure() < getMinWorkingPressure()) return stack;

            return getInvWrapper().insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (playerRef.get() == null || getPressure() < getMinWorkingPressure()) return ItemStack.EMPTY;

            return getInvWrapper().extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return playerRef.get() == null ? 1 : getInvWrapper().getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
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

//    private class PlayerBaublesHandler extends PlayerInvHandler {
//        @Override
//        protected IItemHandler getInvWrapper() {
//            return getPlayer().getCapability(Baubles.CAPABILITY_BAUBLES, null);
//        }
//    }

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
            if (getPressure() < getMinWorkingPressure()) return stack;

            PlayerEntity player = getPlayer();
            if (player == null || getFoodValue(stack) <= 0 || !okToFeed(stack, player)) {
                return stack;
            }

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

        private boolean okToFeed(@Nonnull ItemStack stack, PlayerEntity player) {
            int foodValue = getFoodValue(stack);
            int curFoodLevel = player.getFoodStats().getFoodLevel();
            FeedMode tmpFeedMode = feedMode;
            if (tmpFeedMode == FeedMode.FULLY_ELSE_WHEN_POSSIBLE) {
                tmpFeedMode = player.getMaxHealth() - player.getHealth() > 0 ? FeedMode.WHEN_POSSIBLE : FeedMode.FULLY_UTILIZE;
            }
            switch (tmpFeedMode) {
                case FULLY_UTILIZE:
                    return 20 - curFoodLevel >= foodValue * stack.getCount();
                case WHEN_POSSIBLE:
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

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return getFoodValue(stack) > 0;
        }

        private int getFoodValue(ItemStack stack) {
            return stack.getItem().isFood() ? stack.getItem().getFood().getHealing() : 0;
        }
    }

    private class PlayerExperienceHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            if (curXpFluid != null) {
                PlayerEntity player = getPlayer();
                if (player != null) {
                    return new FluidStack(curXpFluid, EnchantmentUtils.getPlayerXP(player) * PneumaticCraftAPIHandler.getInstance().liquidXPs.get(curXpFluid));
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return curXpFluid != null && stack.getFluid() == curXpFluid;
        }

        @Override
        public int fill(FluidStack resource, FluidAction doFill) {
            if (resource != null && canFill(resource.getFluid())) {
                PlayerEntity player = getPlayer();
                if (player != null) {
                    int liquidToXP = PneumaticCraftAPIHandler.getInstance().liquidXPs.get(resource.getFluid());
                    int pointsAdded = resource.getAmount() / liquidToXP;
                    if (doFill.execute()) {
                        player.giveExperiencePoints(pointsAdded);
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
                    && getPressure() >= getMinWorkingPressure();
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            if (resource != null && canDrain(resource.getFluid())) {
                PlayerEntity player = getPlayer();
                if (player != null) {
                    int liquidToXP = PneumaticCraftAPIHandler.getInstance().liquidXPs.get(resource.getFluid());
                    int pointsDrained = Math.min(EnchantmentUtils.getPlayerXP(player), resource.getAmount() / liquidToXP);
                    if (doDrain.execute()) EnchantmentUtils.addPlayerXP(player, -pointsDrained);
                    return new FluidStack(resource.getFluid(), pointsDrained * liquidToXP);
                }
            }
            return null;
        }

        private boolean canDrain(Fluid fluid) {
            return dispenserUpgradeInserted
                    && (fluid == null || PneumaticCraftAPIHandler.getInstance().liquidXPs.containsKey(fluid))
                    && getPlayer() != null
                    && getPressure() >= getMinWorkingPressure();
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            if (curXpFluid == null) return null;
            return drain(new FluidStack(curXpFluid, maxDrain), doDrain);
        }
    }

    public enum FeedMode {
        FULLY_UTILIZE,
        WHEN_POSSIBLE,
        FULLY_ELSE_WHEN_POSSIBLE
    }
}
