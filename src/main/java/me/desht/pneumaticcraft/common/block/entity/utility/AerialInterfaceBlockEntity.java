/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.block.entity.utility;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.block.entity.*;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.EmittingRedstoneMode;
import me.desht.pneumaticcraft.common.block.entity.RedstoneController.RedstoneMode;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.inventory.AerialInterfaceMenu;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.thirdparty.curios.Curios;
import me.desht.pneumaticcraft.common.thirdparty.curios.CuriosUtils;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.*;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerArmorInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerOffhandInvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;

public class AerialInterfaceBlockEntity extends AbstractAirHandlingBlockEntity
        implements IMinWorkingPressure, IRedstoneControl<AerialInterfaceBlockEntity>, IComparatorSupport, ISideConfigurable, MenuProvider {

    private static final UUID NO_PLAYER = new UUID(0L, 0L);
    private static final int ENERGY_CAPACITY = 100000;
    private static final int RF_PER_TICK = 1000;
    private static final int PLAYER_AIR_MULTIPLIER = 5;

    private static final List<RedstoneMode<AerialInterfaceBlockEntity>> REDSTONE_MODES = ImmutableList.of(
            new EmittingRedstoneMode<>("standard.never", new ItemStack(Items.GUNPOWDER),
                    te -> false),
            new EmittingRedstoneMode<>("aerialInterface.playerConnected", new ItemStack(ModBlocks.AERIAL_INTERFACE.get()),
                    te -> te.isConnectedToPlayer)
    );
    private static final String NO_AERIAL_INTERFACE = "pneumaticcraft:no_aerial_interface";

    @DescSynced
    private String playerName = "";
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
    private final RedstoneController<AerialInterfaceBlockEntity> rsController = new RedstoneController<>(this, REDSTONE_MODES);
    @GuiSynced
    public OperatingProblem operatingProblem = OperatingProblem.OK;

    private final SideConfigurator<IItemHandler> itemHandlerSideConfigurator;

    private final PlayerExperienceHandler playerExperienceHandler = new PlayerExperienceHandler();
    private final PlayerFoodHandler playerFoodHandler = new PlayerFoodHandler();
    private final PneumaticEnergyStorage energyStorage = new PneumaticEnergyStorage(ENERGY_CAPACITY);

    private WeakReference<Player> playerRef = new WeakReference<>(null);

    private final IntList chargeableSlots = new IntArrayList();

    private final List<PlayerInvHandler> invHandlers = new ArrayList<>();
    public GameProfile gameProfileClient;  // for rendering
    private static WildcardedRLMatcher dimensionBlacklist;
    private boolean validatePlayerNow;

    public AerialInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.AERIAL_INTERFACE.get(), pos, state, PressureTier.TIER_TWO, PneumaticValues.VOLUME_AERIAL_INTERFACE, 4);

        PlayerMainInvHandler playerMainInvHandler = new PlayerMainInvHandler();
        PlayerArmorInvHandler playerArmorInvHandler = new PlayerArmorInvHandler();
        PlayerOffhandInvHandler playerOffhandInvHandler = new PlayerOffhandInvHandler();
        PlayerEnderInvHandler playerEnderInvHandler = new PlayerEnderInvHandler();

        itemHandlerSideConfigurator = new SideConfigurator<>("items", this);
        itemHandlerSideConfigurator.registerHandler("mainInv", new ItemStack(Blocks.CHEST),
                Capabilities.ItemHandler.BLOCK, () -> playerMainInvHandler,
                SideConfigurator.RelativeFace.FRONT, SideConfigurator.RelativeFace.BACK, SideConfigurator.RelativeFace.LEFT, SideConfigurator.RelativeFace.RIGHT);
        itemHandlerSideConfigurator.registerHandler("armorInv", new ItemStack(ModItems.PNEUMATIC_CHESTPLATE.get()),
                Capabilities.ItemHandler.BLOCK, () -> playerArmorInvHandler,
                SideConfigurator.RelativeFace.TOP, SideConfigurator.RelativeFace.BOTTOM);
        itemHandlerSideConfigurator.registerHandler("offhandInv", new ItemStack(Items.SHIELD),
                Capabilities.ItemHandler.BLOCK, () -> playerOffhandInvHandler);
        itemHandlerSideConfigurator.registerHandler("enderInv", new ItemStack(Blocks.ENDER_CHEST),
                Capabilities.ItemHandler.BLOCK, () -> playerEnderInvHandler);

        invHandlers.add(playerMainInvHandler);
        invHandlers.add(playerArmorInvHandler);
        invHandlers.add(playerOffhandInvHandler);
        invHandlers.add(playerEnderInvHandler);

        // disabled for now:
        // 1) reports of item duplication (which I can't reproduce)
        // 2) curios item handlers don't prevent non curio items being inserted (looks like checking only done at the slot level)

        if (Curios.available) {
            PlayerCuriosHandler playerCuriosHandler = new PlayerCuriosHandler();
            itemHandlerSideConfigurator.registerHandler("curiosInv", new ItemStack(Items.DIAMOND),
                    Capabilities.ItemHandler.BLOCK, () -> playerCuriosHandler);
            invHandlers.add(playerCuriosHandler);
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();

        GlobalBlockEntityCacheManager.getInstance(getLevel()).getAerialInterfaces().add(this);

        NeoForge.EVENT_BUS.register(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        NeoForge.EVENT_BUS.unregister(this);

        GlobalBlockEntityCacheManager.getInstance(getLevel()).getAerialInterfaces().remove(this);
    }

    @SubscribeEvent
    public void onPlayerDimChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!isRemoved() && isConnectedToPlayer && event.getEntity() == playerRef.get()) {
            validatePlayerNow = true;
        }
    }

    public void setPlayer(Player player) {
        if (player == playerRef.get()) return;

        invHandlers.forEach(PlayerInvHandler::invalidate);
        playerRef = new WeakReference<>(player);
        boolean wasConnected = isConnectedToPlayer;
        if (player == null) {
            isConnectedToPlayer = false;
            playerName = "";
        } else {
            isConnectedToPlayer = true;
            playerName = player.getGameProfile().getName();
            scanForChargeableItems(player);
        }
        if (wasConnected != isConnectedToPlayer) {
            needUpdateNeighbours = true;
            setChanged();
        }
    }

    public void setPlayerId(UUID uuid) {
        playerUUID = uuid;
    }

    @Override
    public void onDescUpdate() {
        super.onDescUpdate();

        if (playerName.isEmpty()) {
            gameProfileClient = null;
        } else {
            SkullBlockEntity.fetchGameProfile(playerName)
                    .thenAcceptAsync(optProfile -> gameProfileClient = optProfile.orElse(null));
        }
    }

    @Override
    public void onUpgradesChanged() {
        super.onUpgradesChanged();

        boolean wasInserted = dispenserUpgradeInserted;
        dispenserUpgradeInserted = getUpgrades(ModUpgrades.DISPENSER.get()) > 0;
        if (wasInserted != dispenserUpgradeInserted) {
            needUpdateNeighbours = true;
            invalidateCapabilities();
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (needUpdateNeighbours) {
            needUpdateNeighbours = false;
            updateNeighbours();
        }
        if (getLevel() instanceof ServerLevel serverLevel && (validatePlayerNow || (getLevel().getGameTime() & 0xf) == 0)) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(playerUUID);
            setPlayer(player);
            if (player != null && player.getTags().contains(NO_AERIAL_INTERFACE)) {
                operatingProblem = OperatingProblem.PLAYER_BARRED;
            } else if (isDimensionBlacklisted(serverLevel, player)) {
                operatingProblem = OperatingProblem.BAD_DIMENSION;
            } else {
                operatingProblem = OperatingProblem.OK;
            }
            validatePlayerNow = false;
        }
        getPlayer().ifPresent(player -> {
            if (aiCanOperate()) {
                addAir(-PneumaticValues.USAGE_AERIAL_INTERFACE);
                if ((nonNullLevel().getGameTime() & 0x3f) == 0) {
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

    private static boolean isDimensionBlacklisted(ServerLevel beLevel, ServerPlayer player) {
        return getDimensionBlacklist().test(beLevel.dimension().location())
                || player != null && getDimensionBlacklist().test(player.level().dimension().location());
    }

    private static WildcardedRLMatcher getDimensionBlacklist() {
        if (dimensionBlacklist == null) {
            dimensionBlacklist = new WildcardedRLMatcher(ConfigHelper.common().machines.aerialInterfaceDimensionBlacklist.get());
        }
        return dimensionBlacklist;
    }

    public static void clearDimensionBlacklist() {
        dimensionBlacklist = null;
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        if (rsController.parseRedstoneMode(tag))
            return;

        if (tag.equals("xpType")) {
            curXPFluidIndex++;
            List<Fluid> available = XPFluidManager.getInstance().getAvailableLiquidXPs();
            if (curXPFluidIndex >= available.size()) {
                curXPFluidIndex = -1;
            }
            curXpFluid = curXPFluidIndex >= 0 ? available.get(curXPFluidIndex) : Fluids.EMPTY;
            curXpRatio = XPFluidManager.getInstance().getXPRatio(curXpFluid);
            invalidateCapabilities();
        } else if (itemHandlerSideConfigurator.handleButtonPress(tag, shiftHeld)) {
            needUpdateNeighbours = true;
        } else {
            try {
                feedMode = FeedMode.valueOf(tag);
            } catch (IllegalArgumentException ignored) {
            }
        }
        setChanged();
    }

    private Optional<Player> getPlayer() {
        Player player = playerRef.get();
        return player != null && player.isAlive() ? Optional.of(player) : Optional.empty();
    }

    @Override
    public boolean hasFluidCapability() {
        return true;
    }

    @Override
    public boolean hasEnergyCapability() {
        return true;
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return dispenserUpgradeInserted ? playerFoodHandler : itemHandlerSideConfigurator.getHandler(dir);
    }

    @Override
    public IFluidHandler getFluidHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return dispenserUpgradeInserted && curXpFluid != Fluids.EMPTY ? playerExperienceHandler : null;
    }

    @Override
    public IEnergyStorage getEnergyHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return energyStorage;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        playerUUID = UUID.fromString(tag.getString("playerUUID"));
        feedMode = FeedMode.valueOf(tag.getString("feedMode"));
        curXpFluid = tag.contains("curXpFluid") ? BuiltInRegistries.FLUID.get(ResourceLocation.parse(tag.getString("curXpFluid"))) : Fluids.EMPTY;
        curXpRatio = XPFluidManager.getInstance().getXPRatio(curXpFluid);
        energyStorage.readFromNBT(tag);

        curXPFluidIndex = curXpFluid == Fluids.EMPTY ? -1 : XPFluidManager.getInstance().getAvailableLiquidXPs().indexOf(curXpFluid);
        dispenserUpgradeInserted = getUpgrades(ModUpgrades.DISPENSER.get()) > 0;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        tag.putString("playerUUID", playerUUID.toString());
        tag.putString("feedMode", feedMode.toString());
        tag.putString("curXpFluid", PneumaticCraftUtils.getRegistryName(curXpFluid).orElseThrow().toString());
        energyStorage.writeToNBT(tag);
    }

    @Override
    public float getMinWorkingPressure() {
        return PneumaticValues.MIN_PRESSURE_AERIAL_INTERFACE;
    }

    @Override
    public int getComparatorValue() {
        return rsController.shouldEmit() ? 15 : 0;
    }

    private void scanForChargeableItems(Player player) {
        if (energyStorage.getEnergyStored() == 0) return;

        chargeableSlots.clear();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (IOHelper.getEnergyStorageForItem(inv.getItem(i)).isPresent()) {
                chargeableSlots.add(i);
            }
        }
    }

    private void supplyEnergyToPlayer(Player player) {
        if (energyStorage.getEnergyStored() > 0) {
            Inventory inv = player.getInventory();
            for (int slot : chargeableSlots) {
                ItemStack stack = inv.getItem(slot);
                int energyLeft = IOHelper.getEnergyStorageForItem(stack).map(receivingStorage -> {
                    int stored = energyStorage.getEnergyStored();
                    if (stored > 0) {
                        energyStorage.extractEnergy(receivingStorage.receiveEnergy(Math.min(stored, RF_PER_TICK), false), false);
                    }
                    return energyStorage.getEnergyStored();
                }).orElse(energyStorage.getEnergyStored());
                if (energyLeft == 0) break;
            }

            if (Curios.available && energyStorage.getEnergyStored() > 0) {
                CuriosUtils.chargeItems(player, energyStorage, RF_PER_TICK);
            }
        }
    }

    private void supplyAirToPlayer(Player player) {
        // A little higher than the pneumatic helmet with scuba upgrade, so aerial interface
        // takes precedence if both are available
        if (player.getAirSupply() <= 170) {
            int playerAir = 300 - player.getAirSupply();
            player.setAirSupply(300);
            addAir(-playerAir * PLAYER_AIR_MULTIPLIER);
            NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.SCUBA.get(), SoundSource.PLAYERS, player.blockPosition(), 1f, 0.9f, false), (ServerPlayer) player);
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
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new AerialInterfaceMenu(windowId, playerInventory, getBlockPos());
    }

    @Override
    public RedstoneController<AerialInterfaceBlockEntity> getRedstoneController() {
        return rsController;
    }

    private boolean aiCanOperate() {
        return operatingProblem == OperatingProblem.OK && getPressure() >= getMinWorkingPressure();
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

        IItemHandler getCachedHandler(Player p, Function<Inventory,IItemHandler> f) {
            if (cached == null) cached = f.apply(p.getInventory());
            return cached;
        }

        /**
         * Get an item handler for the current player, which must be non-null.
         * @return an item handler for the appropriate part of the player's inventory
         * @param player the player
         */
        protected abstract IItemHandler getInvWrapper(@Nonnull Player player);

        @Override
        public int getSlots() {
            return getPlayer()
                    .filter(p -> aiCanOperate())
                    .map(p -> getInvWrapper(p).getSlots())
                    .orElse(0);
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return getPlayer()
                    .filter(p -> aiCanOperate())
                    .map(p -> getInvWrapper(p).getStackInSlot(slot))
                    .orElse(ItemStack.EMPTY);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return getPlayer()
                    .filter(p -> aiCanOperate())
                    .map(p -> getInvWrapper(p).insertItem(slot, stack, simulate))
                    .orElse(stack);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return getPlayer()
                    .filter(p -> aiCanOperate())
                    .map(p -> getInvWrapper(p).extractItem(slot, amount, simulate))
                    .orElse(ItemStack.EMPTY);
        }

        @Override
        public int getSlotLimit(int slot) {
            return getPlayer()
                    .filter(p -> aiCanOperate())
                    .map(p -> getInvWrapper(p).getSlotLimit(slot))
                    .orElse(1);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return aiCanOperate();
        }
    }

    private class PlayerMainInvHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper(Player player) {
            return getCachedHandler(player, PlayerMainInvWrapper::new);
        }
    }

    private class PlayerArmorInvHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper(Player player) {
            return getCachedHandler(player, PlayerArmorInvWrapper::new);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack stack = getStackInSlot(slot);
            return EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) ?
                    ItemStack.EMPTY :
                    super.extractItem(slot, amount, simulate);
        }
    }

    private class PlayerOffhandInvHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper(Player player) {
            return getCachedHandler(player, PlayerOffhandInvWrapper::new);
        }
    }

    private class PlayerEnderInvHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper(Player player) {
            return getCachedHandler(player, p -> new InvWrapper(p.player.getEnderChestInventory()));
        }
    }

    private class PlayerCuriosHandler extends PlayerInvHandler {
        @Override
        protected IItemHandler getInvWrapper(Player player) {
            return getCachedHandler(player, p -> CuriosUtils.makeCombinedInvWrapper(p.player));
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
            if (!aiCanOperate()) return stack;

            return getPlayer().map(player -> {
                if (getFoodValue(stack) <= 0 || !okToFeed(stack, player)) {
                    return stack;
                }

                if (simulate) return ItemStack.EMPTY;

                int startValue = stack.getCount();
                ItemStack remainingItem = stack;
                ItemStack copy = stack.copy();
                while (stack.getCount() > 0) {
                    remainingItem = stack.finishUsingItem(player.level(), player);
                    remainingItem = EventHooks.onItemUseFinish(player, stack, 0, remainingItem);
                    if (remainingItem.getCount() > 0 && (remainingItem != stack || remainingItem.getCount() != startValue)) {
                        if (!player.getInventory().add(remainingItem) && remainingItem.getCount() > 0) {
                            player.drop(remainingItem, false);
                        }
                    }
                    player.displayClientMessage(Component.translatable("pneumaticcraft.gui.aerial_interface.fedItem", copy.getHoverName()), true);
                    if (stack.getCount() == startValue) break;
                }
                return remainingItem.getCount() > 0 ? remainingItem : ItemStack.EMPTY;
            }).orElse(stack);
        }

        private boolean okToFeed(@Nonnull ItemStack stack, Player player) {
            int foodValue = getFoodValue(stack);
            int curFoodLevel = player.getFoodData().getFoodLevel();
            FeedMode effectiveFeedMode = feedMode == FeedMode.SMART ?
                    (player.getHealth() < player.getMaxHealth() ? FeedMode.GREEDY : FeedMode.FRUGAL) :
                    feedMode;
            return switch (effectiveFeedMode) {
                case FRUGAL -> 20 - curFoodLevel >= foodValue * stack.getCount();
                case GREEDY -> 20 - curFoodLevel >= foodValue * (stack.getCount() - 1) + 1;
                default -> false;
            };
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
            return stack.has(DataComponents.FOOD) ? stack.get(DataComponents.FOOD).nutrition() : 0;
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
                    && aiCanOperate();
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            return getPlayer().map(player -> {
                if (curXpRatio != 0 && dispenserUpgradeInserted && aiCanOperate()) {
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

    public enum OperatingProblem implements ITranslatableEnum {
        OK("ok"),
        BAD_DIMENSION("bad_dimension"),
        PLAYER_BARRED("player_barred");

        private final String name;

        OperatingProblem(String name) {
            this.name = name;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.tab.info.aerialInterface.problem." + name;
        }
    }
}
