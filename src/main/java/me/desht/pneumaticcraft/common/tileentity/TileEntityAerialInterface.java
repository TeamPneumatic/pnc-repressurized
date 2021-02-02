package me.desht.pneumaticcraft.common.tileentity;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.inventory.ContainerAerialInterface;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.thirdparty.curios.Curios;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.EmittingRedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.tileentity.SideConfigurator.RelativeFace;
import me.desht.pneumaticcraft.common.util.EnchantmentUtils;
import me.desht.pneumaticcraft.common.util.GlobalTileEntityCacheManager;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
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
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;

public class TileEntityAerialInterface extends TileEntityPneumaticBase
        implements IMinWorkingPressure, IRedstoneControl<TileEntityAerialInterface>, IComparatorSupport, ISideConfigurable, INamedContainerProvider {

    private static final UUID NO_PLAYER = new UUID(0L, 0L);
    private static final int ENERGY_CAPACITY = 100000;
    private static final int RF_PER_TICK = 1000;

    private static final List<RedstoneMode<TileEntityAerialInterface>> REDSTONE_MODES = ImmutableList.of(
            new EmittingRedstoneMode<>("standard.never", new ItemStack(Items.GUNPOWDER),
                    te -> false),
            new EmittingRedstoneMode<>("aerialInterface.playerConnected", new ItemStack(ModBlocks.AERIAL_INTERFACE.get()),
                    te -> te.isConnectedToPlayer)
    );

    @GuiSynced
    public String playerName = "";
    private UUID playerUUID = NO_PLAYER;

    private Fluid curXpFluid = Fluids.EMPTY;
    private int curXpRatio = 0;
    @GuiSynced
    public int curXPFluidIndex = -1;  // index into PneumaticCraftAPIHandler.availableLiquidXPs, -1 = disabled

    @GuiSynced
    public FeedMode feedMode = FeedMode.FRUGAL;
    private boolean oldRedstoneStatus;
    private boolean needUpdateNeighbours;
    @GuiSynced
    public boolean isConnectedToPlayer = false;
    @GuiSynced
    public boolean dispenserUpgradeInserted;
    @GuiSynced
    private final RedstoneController<TileEntityAerialInterface> rsController = new RedstoneController<>(this, REDSTONE_MODES);

    private final SideConfigurator<IItemHandler> itemHandlerSideConfigurator;

    private final PlayerExperienceHandler playerExperienceHandler = new PlayerExperienceHandler();
    private final LazyOptional<IFluidHandler> playerExpCap = LazyOptional.of(() -> playerExperienceHandler);
    private final PlayerFoodHandler playerFoodHandler = new PlayerFoodHandler();
    private final LazyOptional<IItemHandler> playerFoodCap = LazyOptional.of(() -> playerFoodHandler);
    private final PneumaticEnergyStorage energyStorage = new PneumaticEnergyStorage(ENERGY_CAPACITY);
    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energyStorage);

    private WeakReference<PlayerEntity> playerRef = new WeakReference<>(null);

    private final List<Integer> chargeableSlots = new ArrayList<>();

    private final List<PlayerInvHandler> invHandlers = new ArrayList<>();

    public TileEntityAerialInterface() {
        super(ModTileEntities.AERIAL_INTERFACE.get(), PneumaticValues.DANGER_PRESSURE_AERIAL_INTERFACE, PneumaticValues.MAX_PRESSURE_AERIAL_INTERFACE, PneumaticValues.VOLUME_AERIAL_INTERFACE, 4);

        PlayerMainInvHandler playerMainInvHandler = new PlayerMainInvHandler();
        PlayerArmorInvHandler playerArmorInvHandler = new PlayerArmorInvHandler();
        PlayerOffhandInvHandler playerOffhandInvHandler = new PlayerOffhandInvHandler();
        PlayerEnderInvHandler playerEnderInvHandler = new PlayerEnderInvHandler();

        itemHandlerSideConfigurator = new SideConfigurator<>("items", this);
        itemHandlerSideConfigurator.registerHandler("mainInv", new ItemStack(Blocks.CHEST),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> playerMainInvHandler,
                RelativeFace.FRONT, RelativeFace.BACK, RelativeFace.LEFT, RelativeFace.RIGHT);
        itemHandlerSideConfigurator.registerHandler("armorInv", new ItemStack(ModItems.PNEUMATIC_CHESTPLATE.get()),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> playerArmorInvHandler,
                RelativeFace.TOP, RelativeFace.BOTTOM);
        itemHandlerSideConfigurator.registerHandler("offhandInv", new ItemStack(Items.SHIELD),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> playerOffhandInvHandler);
        itemHandlerSideConfigurator.registerHandler("enderInv", new ItemStack(Blocks.ENDER_CHEST),
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> playerEnderInvHandler);

        invHandlers.add(playerMainInvHandler);
        invHandlers.add(playerArmorInvHandler);
        invHandlers.add(playerOffhandInvHandler);
        invHandlers.add(playerEnderInvHandler);

        if (Curios.available) {
            PlayerCuriosHandler playerCuriosHandler = new PlayerCuriosHandler();
            itemHandlerSideConfigurator.registerHandler("curiosInv", new ItemStack(Items.DIAMOND),
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> playerCuriosHandler);
            invHandlers.add(playerCuriosHandler);
        }
    }

    @Override
    public void validate() {
        super.validate();

        GlobalTileEntityCacheManager.getInstance().aerialInterfaces.add(this);
    }

    @Override
    public void remove() {
        super.remove();

        GlobalTileEntityCacheManager.getInstance().aerialInterfaces.remove(this);

        itemHandlerSideConfigurator.invalidateCaps();
        playerExpCap.invalidate();
        playerFoodCap.invalidate();
        energyCap.invalidate();
    }

    public void setPlayer(PlayerEntity player) {
        if (player == playerRef.get()) return;

        invHandlers.forEach(PlayerInvHandler::invalidate);
        playerRef = new WeakReference<>(player);
        boolean wasConnected = isConnectedToPlayer;
        if (player == null) {
            isConnectedToPlayer = false;
        } else {
            setPlayerId(player.getGameProfile().getName(), player.getGameProfile().getId());
            scanForChargeableItems(player);
            isConnectedToPlayer = true;
        }
        if (wasConnected != isConnectedToPlayer) {
            needUpdateNeighbours = true;
            markDirty();
        }
    }

    private void setPlayerId(String username, UUID uuid) {
        playerUUID = uuid;
        playerName = username;
    }

    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();

        boolean wasInserted = dispenserUpgradeInserted;
        dispenserUpgradeInserted = getUpgrades(EnumUpgrade.DISPENSER) > 0;
        if (wasInserted != dispenserUpgradeInserted) {
            needUpdateNeighbours = true;
        }
    }

    @Override
    public void tick() {
        if (!getWorld().isRemote) {
            if (needUpdateNeighbours) {
                needUpdateNeighbours = false;
                updateNeighbours();
            }
            if (getWorld() instanceof ServerWorld && (getWorld().getGameTime() & 0xf) == 0) {
                setPlayer(((ServerWorld) getWorld()).getServer().getPlayerList().getPlayerByUUID(playerUUID));
            }
            getPlayer().ifPresent(player -> {
                if (getPressure() >= getMinWorkingPressure()) {
                    addAir(-PneumaticValues.USAGE_AERIAL_INTERFACE);
                    if ((getWorld().getGameTime() & 0x3f) == 0) {
                        scanForChargeableItems(player);
                    }
                    supplyEnergyToPlayer(player);
                    supplyAirToPlayer(player);
                }
            });

            if (oldRedstoneStatus != rsController.shouldEmit()) {
                oldRedstoneStatus = rsController.shouldEmit();
                needUpdateNeighbours = true;
            }
        }

        super.tick();
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        if (rsController.parseRedstoneMode(tag))
            return;

        if (tag.equals("xpType")) {
            curXPFluidIndex++;
            List<Fluid> available = XPFluidManager.getInstance().getAvailableLiquidXPs();
            if (curXPFluidIndex >= available.size()) {
                curXPFluidIndex = -1;
            }
            if (curXPFluidIndex >= 0 && curXPFluidIndex < available.size()) {
                curXpFluid = available.get(curXPFluidIndex);
            } else {
                curXpFluid = Fluids.EMPTY;
            }
            curXpRatio = XPFluidManager.getInstance().getXPRatio(curXpFluid);
        } else if (tag.startsWith("SideConf") && itemHandlerSideConfigurator.handleButtonPress(tag)) {
            needUpdateNeighbours = true;
        } else {
            try {
                feedMode = FeedMode.valueOf(tag);
            } catch (IllegalArgumentException ignored) {
            }
        }
        markDirty();
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    private Optional<PlayerEntity> getPlayer() {
        PlayerEntity player = playerRef.get();
        return player != null && player.isAlive() ? Optional.of(player) : Optional.empty();
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
        } else if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && dispenserUpgradeInserted && curXpFluid != Fluids.EMPTY) {
            return playerExpCap.cast();
        } else if (cap == CapabilityEnergy.ENERGY) {
            return energyCap.cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        feedMode = FeedMode.valueOf(tag.getString("feedMode"));
        setPlayerId(tag.getString("playerName"), UUID.fromString(tag.getString("playerUUID")));
        curXpFluid = tag.contains("curXpFluid") ? ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("curXpFluid"))) : Fluids.EMPTY;
        curXpRatio = XPFluidManager.getInstance().getXPRatio(curXpFluid);
        energyStorage.readFromNBT(tag);

        curXPFluidIndex = curXpFluid == Fluids.EMPTY ? -1 : XPFluidManager.getInstance().getAvailableLiquidXPs().indexOf(curXpFluid);
        dispenserUpgradeInserted = getUpgrades(EnumUpgrade.DISPENSER) > 0;
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);

        tag.putString("feedMode", feedMode.toString());
        tag.putString("playerName", playerName);
        tag.putString("playerUUID", playerUUID.toString());
        tag.putString("curXpFluid", curXpFluid.getRegistryName().toString());
        energyStorage.writeToNBT(tag);

        return tag;
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE;
    }

    @Override
    public int getComparatorValue() {
        return rsController.shouldEmit() ? 15 : 0;
    }

    private void scanForChargeableItems(PlayerEntity player) {
        if (energyStorage.getEnergyStored() == 0) return;

        chargeableSlots.clear();
        PlayerInventory inv = player.inventory;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (inv.getStackInSlot(i).getCapability(CapabilityEnergy.ENERGY).isPresent()) {
                chargeableSlots.add(i);
            }
        }
    }

    private void supplyEnergyToPlayer(PlayerEntity player) {
        if (energyStorage.getEnergyStored() > 0) {
            PlayerInventory inv = player.inventory;
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

            if (Curios.available && energyStorage.getEnergyStored() > 0) {
                Curios.chargeItems(player, energyStorage, RF_PER_TICK);
            }
        }
    }

    private void supplyAirToPlayer(PlayerEntity player) {
        // check every 16 ticks
        if ((getWorld().getGameTime() & 0xf) == 0) {
            if (player.getAir() <= 280) {
                player.setAir(player.getAir() + 16);
                addAir(-80);  // 5 pneumatic air per player air
            }
        }
    }

    @Override
    public List<SideConfigurator<?>> getSideConfigurators() {
        return Collections.singletonList(itemHandlerSideConfigurator);
    }

    @Override
    public Direction byIndex() {
        return getRotation();
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerAerialInterface(windowId, playerInventory, getPos());
    }

    @Override
    public RedstoneController<TileEntityAerialInterface> getRedstoneController() {
        return rsController;
    }

    private abstract class PlayerInvHandler implements IItemHandler {
        IItemHandler cached = null;

        /**
         * Force a recache of the item handler wrappers for the various player inventory parts.
         * Called when the player entity object changes (player connects/disconnects/changes dimension)
         */
        void invalidate() {
            cached = null;
        }

        IItemHandler getCachedHandler(PlayerEntity p, Function<PlayerInventory,IItemHandler> f) {
            if (cached == null) cached = f.apply(p.inventory);
            return cached;
        }

        /**
         * Get an item handler for the current player, which must be non-null.
         * @return an item handler for the appropriate part of the player's inventory
         * @param player the player
         */
        protected abstract IItemHandler getInvWrapper(@Nonnull PlayerEntity player);

        @Override
        public int getSlots() {
            return getPlayer().map(p -> getInvWrapper(p).getSlots()).orElse(0);
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return getPlayer().map(p -> getInvWrapper(p).getStackInSlot(slot)).orElse(ItemStack.EMPTY);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return getPlayer()
                    .filter(p -> getPressure() >= getMinWorkingPressure())
                    .map(p -> getInvWrapper(p).insertItem(slot, stack, simulate))
                    .orElse(stack);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return getPlayer().filter(p -> getPressure() >= getMinWorkingPressure())
                    .map(p -> getInvWrapper(p).extractItem(slot, amount, simulate))
                    .orElse(ItemStack.EMPTY);
        }

        @Override
        public int getSlotLimit(int slot) {
            return getPlayer().map(p -> getInvWrapper(p).getSlotLimit(slot)).orElse(1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }
    }

    private class PlayerMainInvHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper(PlayerEntity player) {
            return getCachedHandler(player, PlayerMainInvWrapper::new);
        }
    }

    private class PlayerArmorInvHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper(PlayerEntity player) {
            return getCachedHandler(player, PlayerArmorInvWrapper::new);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack stack = getStackInSlot(slot);
            return EnchantmentHelper.getEnchantmentLevel(Enchantments.BINDING_CURSE, stack) > 0 ?
                    ItemStack.EMPTY :
                    super.extractItem(slot, amount, simulate);
        }
    }

    private class PlayerOffhandInvHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper(PlayerEntity player) {
            return getCachedHandler(player, PlayerOffhandInvWrapper::new);
        }
    }

    private class PlayerEnderInvHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper(PlayerEntity player) {
            if (cached == null) cached = new InvWrapper(player.getInventoryEnderChest());
            return cached;
        }
    }

    private class PlayerCuriosHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper(PlayerEntity player) {
            if (cached == null) cached = Curios.makeCombinedInvWrapper(player);
            return cached;
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
            if (getPressure() < getMinWorkingPressure()) return stack;

            return getPlayer().map(player -> {
                if (getFoodValue(stack) <= 0 || !okToFeed(stack, player)) {
                    return stack;
                }

                if (simulate) return ItemStack.EMPTY;

                int startValue = stack.getCount();
                ItemStack remainingItem = stack;
                ItemStack copy = stack.copy();
                while (stack.getCount() > 0) {
                    remainingItem = stack.onItemUseFinish(player.world, player);
                    remainingItem = ForgeEventFactory.onItemUseFinish(player, stack, 0, remainingItem);
                    if (remainingItem.getCount() > 0 && (remainingItem != stack || remainingItem.getCount() != startValue)) {
                        if (!player.inventory.addItemStackToInventory(remainingItem) && remainingItem.getCount() > 0) {
                            player.dropItem(remainingItem, false);
                        }
                    }
                    player.sendStatusMessage(new TranslationTextComponent("pneumaticcraft.gui.aerial_interface.fedItem", copy.getDisplayName()), true);
                    if (stack.getCount() == startValue) break;
                }
                return remainingItem.getCount() > 0 ? remainingItem : ItemStack.EMPTY;
            }).orElse(stack);
        }

        private boolean okToFeed(@Nonnull ItemStack stack, PlayerEntity player) {
            int foodValue = getFoodValue(stack);
            int curFoodLevel = player.getFoodStats().getFoodLevel();
            FeedMode effectiveFeedMode = feedMode == FeedMode.SMART ?
                    (player.getHealth() < player.getMaxHealth() ? FeedMode.GREEDY : FeedMode.FRUGAL) :
                    feedMode;
            switch (effectiveFeedMode) {
                case FRUGAL:
                    return 20 - curFoodLevel >= foodValue * stack.getCount();
                case GREEDY:
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
            //noinspection ConstantConditions
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
            if (curXpFluid != Fluids.EMPTY) {
                return getPlayer().map(p -> new FluidStack(curXpFluid, EnchantmentUtils.getPlayerXP(p) * curXpRatio))
                        .orElse(FluidStack.EMPTY);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return curXpFluid != Fluids.EMPTY && stack.getFluid() == curXpFluid;
        }

        @Override
        public int fill(FluidStack resource, FluidAction doFill) {
            return getPlayer().map(player -> {
                if (curXpRatio != 0 && canFill(resource.getFluid())) {
                    int pointsAdded = resource.getAmount() / curXpRatio;
                    if (doFill.execute()) {
                        player.giveExperiencePoints(pointsAdded);
                    }
                    return pointsAdded * curXpRatio;
                } else {
                    return 0;
                }
            }).orElse(0);
        }

        private boolean canFill(Fluid fluid) {
            return dispenserUpgradeInserted && fluid != Fluids.EMPTY && fluid == curXpFluid
                    && curXpRatio != 0
                    && getPressure() >= getMinWorkingPressure();
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            return getPlayer().map(player -> {
                if (curXpRatio != 0 && dispenserUpgradeInserted && getPressure() >= getMinWorkingPressure()) {
                    int pointsDrained = Math.min(EnchantmentUtils.getPlayerXP(player), resource.getAmount() / curXpRatio);
                    if (doDrain.execute()) EnchantmentUtils.addPlayerXP(player, -pointsDrained);
                    return new FluidStack(resource.getFluid(), pointsDrained * curXpRatio);
                }
                return FluidStack.EMPTY;
            }).orElse(FluidStack.EMPTY);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            if (curXpFluid == Fluids.EMPTY) return FluidStack.EMPTY;
            return drain(new FluidStack(curXpFluid, maxDrain), doDrain);
        }
    }

    public enum FeedMode implements ITranslatableEnum {
        FRUGAL("frugal", Items.COOKED_BEEF),
        GREEDY("greedy", Items.APPLE),
        SMART("smart", Items.GOLDEN_APPLE);

        private final String key;
        private final ItemStack stack;

        FeedMode(String key, Item item) {
            this.key = key;
            this.stack = new ItemStack(item);
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.tab.info.aerialInterface.feedMode." + key;
        }

        public String getDescTranslationKey() {
            return getTranslationKey() + ".desc";
        }

        public ItemStack getIconStack() {
            return stack;
        }
    }
}
