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
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.*;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Optional;
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
            new EmittingRedstoneMode<>("standard.never", Items.GUNPOWDER,
                    te -> false),
            new EmittingRedstoneMode<>("aerialInterface.playerConnected", ModBlocks.AERIAL_INTERFACE,
                    te -> te.isConnectedToPlayer)
    );
    private static final String NO_AERIAL_INTERFACE = "pneumaticcraft:no_aerial_interface";

    private static final Lazy<WildcardedRLMatcher> dimensionBlacklist
            = WildcardedRLMatcher.lazyFromConfig(ConfigHelper.common().machines.aerialInterfaceDimensionBlacklist);

    private static boolean curiosIntegrationEnabled = false;

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

    private final SideConfigurator<ResourceHandler<ItemResource>> itemHandlerSideConfigurator;

    private final PlayerExperienceHandler playerExperienceHandler = new PlayerExperienceHandler();
    private final PlayerFoodHandler playerFoodHandler = new PlayerFoodHandler();
    private final PneumaticEnergyStorage energyStorage = new PneumaticEnergyStorage(ENERGY_CAPACITY);

    private WeakReference<Player> playerRef = new WeakReference<>(null);

    private final IntList chargeableSlots = new IntArrayList();

    private final List<PlayerInvHandler> invHandlers = new ArrayList<>();
    public ResolvableProfile gameProfileClient;  // for rendering
    private boolean validatePlayerNow;

    public AerialInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.AERIAL_INTERFACE.get(), pos, state, PressureTier.TIER_TWO, PneumaticValues.VOLUME_AERIAL_INTERFACE, 4);

        PlayerMainInvHandler playerMainInvHandler = new PlayerMainInvHandler();
        PlayerArmorInvHandler playerArmorInvHandler = new PlayerArmorInvHandler();
        PlayerOffhandInvHandler playerOffhandInvHandler = new PlayerOffhandInvHandler();
        PlayerEnderInvHandler playerEnderInvHandler = new PlayerEnderInvHandler();

        itemHandlerSideConfigurator = new SideConfigurator<>("items", this);
        itemHandlerSideConfigurator.registerHandler("mainInv", new ItemStackTemplate(Blocks.CHEST.asItem()),
                Capabilities.Item.BLOCK, () -> playerMainInvHandler,
                SideConfigurator.RelativeFace.FRONT, SideConfigurator.RelativeFace.BACK, SideConfigurator.RelativeFace.LEFT, SideConfigurator.RelativeFace.RIGHT);
        itemHandlerSideConfigurator.registerHandler("armorInv", new ItemStackTemplate(ModItems.PNEUMATIC_CHESTPLATE.get()),
                Capabilities.Item.BLOCK, () -> playerArmorInvHandler,
                SideConfigurator.RelativeFace.TOP, SideConfigurator.RelativeFace.BOTTOM);
        itemHandlerSideConfigurator.registerHandler("offhandInv", new ItemStackTemplate(Items.SHIELD),
                Capabilities.Item.BLOCK, () -> playerOffhandInvHandler);
        itemHandlerSideConfigurator.registerHandler("enderInv", new ItemStackTemplate(Blocks.ENDER_CHEST.asItem()),
                Capabilities.Item.BLOCK, () -> playerEnderInvHandler);

        invHandlers.add(playerMainInvHandler);
        invHandlers.add(playerArmorInvHandler);
        invHandlers.add(playerOffhandInvHandler);
        invHandlers.add(playerEnderInvHandler);

        // disabled for now:
        // 1) reports of item duplication (which I can't reproduce)
        // 2) curios item handlers don't prevent non curio items being inserted (looks like checking only done at the slot level)

        // TODO 26.1 integration: Curios
//        if (Curios.available && curiosIntegrationEnabled) {
//            PlayerCuriosHandler playerCuriosHandler = new PlayerCuriosHandler();
//            itemHandlerSideConfigurator.registerHandler("curiosInv", new ItemStackTemplate(Items.DIAMOND),
//                    Capabilities.Item.BLOCK, () -> playerCuriosHandler);
//            invHandlers.add(playerCuriosHandler);
//        }
    }

    public static void onConfigRefresh() {
        curiosIntegrationEnabled = ConfigHelper.common().integration.curiosAerialInterfaceAccess.get();
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();

        GlobalBlockEntityCacheManager.getInstance(getLevel()).getAerialInterfaces().add(this);

        if (!nonNullLevel().isClientSide()) {
            NeoForge.EVENT_BUS.register(this);
        }
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
            playerName = player.getGameProfile().name();
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
            // resolved (skin etc.) by the renderer when needed (PlayerSkinRenderCache in AerialInterfaceRenderer)
            gameProfileClient = ResolvableProfile.createUnresolved(playerName);
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
            if (player != null && player.entityTags().contains(NO_AERIAL_INTERFACE)) {
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
        return dimensionBlacklist.get().test(beLevel.dimension().identifier())
                || player != null && dimensionBlacklist.get().test(player.level().dimension().identifier());
    }

    public static void clearDimensionBlacklist() {
        dimensionBlacklist.invalidate();
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
    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction dir) {
        return dispenserUpgradeInserted ? playerFoodHandler : itemHandlerSideConfigurator.getHandler(dir);
    }

    @Override
    public ResourceHandler<FluidResource> getFluidHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return dispenserUpgradeInserted && curXpFluid != Fluids.EMPTY ? playerExperienceHandler : null;
    }

    @Override
    public EnergyHandler getEnergyHandler(@org.jetbrains.annotations.Nullable Direction dir) {
        return energyStorage;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        playerUUID = UUID.fromString(tag.getStringOr("playerUUID", ""));
        feedMode = FeedMode.valueOf(tag.getStringOr("feedMode", ""));
        curXpFluid = tag.contains("curXpFluid") ? BuiltInRegistries.FLUID.getValue(Identifier.parse(tag.getStringOr("curXpFluid", ""))) : Fluids.EMPTY;
        curXpRatio = XPFluidManager.getInstance().getXPRatio(curXpFluid);
        energyStorage.readFromNBT(tag);

        curXPFluidIndex = curXpFluid == Fluids.EMPTY ? -1 : XPFluidManager.getInstance().getAvailableLiquidXPs().indexOf(curXpFluid);
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
        if (energyStorage.getAmountAsInt() == 0) return;

        chargeableSlots.clear();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (IOHelper.getEnergyStorageForItem(inv.getItem(i)).isPresent()) {
                chargeableSlots.add(i);
            }
        }
    }

    private void supplyEnergyToPlayer(Player player) {
        if (energyStorage.getAmountAsInt() > 0) {
            Inventory inv = player.getInventory();
            for (int slot : chargeableSlots) {
                ItemStack stack = inv.getItem(slot);
                int energyLeft = IOHelper.getEnergyStorageForItem(stack).map(receivingStorage -> {
                    int stored = energyStorage.getAmountAsInt();
                    if (stored > 0) {
                        IOHelper.extractEnergy(energyStorage, IOHelper.insertEnergy(receivingStorage, Math.min(stored, RF_PER_TICK), false), false);
                    }
                    return energyStorage.getAmountAsInt();
                }).orElse(energyStorage.getAmountAsInt());
                if (energyLeft == 0) break;
            }

            // TODO 26.1 integration: Curios
//            if (Curios.available && energyStorage.getEnergyStored() > 0) {
//                CuriosUtils.chargeItems(player, energyStorage, RF_PER_TICK);
//            }
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

    private abstract class PlayerInvHandler implements ResourceHandler<ItemResource> {
        ResourceHandler<ItemResource> cached = null;

        /**
         * Force a recache of the item handler wrappers for the various player inventory parts.
         * Called when the player entity object changes (player connects/disconnects/changes dimension)
         */
        void invalidate() {
            cached = null;
        }

        ResourceHandler<ItemResource> getCachedHandler(Player p, Function<Inventory,ResourceHandler<ItemResource>> f) {
            if (cached == null) cached = f.apply(p.getInventory());
            return cached;
        }

        /**
         * Get an item handler for the current player, which must be non-null.
         * @return an item handler for the appropriate part of the player's inventory
         * @param player the player
         */
        protected abstract ResourceHandler<ItemResource> getInvWrapper(@Nonnull Player player);

        private Optional<ResourceHandler<ItemResource>> getOperatingWrapper() {
            return getPlayer().filter(p -> aiCanOperate()).map(this::getInvWrapper);
        }

        @Override
        public int size() {
            return getOperatingWrapper().map(ResourceHandler::size).orElse(0);
        }

        @Override
        public ItemResource getResource(int index) {
            return getOperatingWrapper().map(h -> h.getResource(index)).orElse(ItemResource.EMPTY);
        }

        @Override
        public long getAmountAsLong(int index) {
            return getOperatingWrapper().map(h -> h.getAmountAsLong(index)).orElse(0L);
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return getOperatingWrapper().map(h -> h.getCapacityAsLong(index, resource)).orElse(1L);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return aiCanOperate();
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return getOperatingWrapper().map(h -> h.insert(index, resource, amount, transaction)).orElse(0);
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return getOperatingWrapper().map(h -> h.extract(index, resource, amount, transaction)).orElse(0);
        }
    }

    private class PlayerMainInvHandler extends PlayerInvHandler {
        @Override
        protected ResourceHandler<ItemResource> getInvWrapper(Player player) {
            return getCachedHandler(player, inv -> PlayerInventoryWrapper.of(inv).getMainSlots());
        }
    }

    private class PlayerArmorInvHandler extends PlayerInvHandler {
        @Override
        protected ResourceHandler<ItemResource> getInvWrapper(Player player) {
            return getCachedHandler(player, inv -> PlayerInventoryWrapper.of(inv).getArmorSlots());
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return EnchantmentHelper.has(resource.toStack(), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) ?
                    0 :
                    super.extract(index, resource, amount, transaction);
        }
    }

    private class PlayerOffhandInvHandler extends PlayerInvHandler {
        @Override
        protected ResourceHandler<ItemResource> getInvWrapper(Player player) {
            return getCachedHandler(player, inv -> PlayerInventoryWrapper.of(inv).getHandSlot(InteractionHand.OFF_HAND));
        }
    }

    private class PlayerEnderInvHandler extends PlayerInvHandler {
        @Override
        protected ResourceHandler<ItemResource> getInvWrapper(Player player) {
            return getCachedHandler(player, inv -> VanillaContainerWrapper.of(inv.player.getEnderChestInventory()));
        }
    }

    // TODO 26.1 integration: Curios
//    private class PlayerCuriosHandler extends PlayerInvHandler {
//        @Override
//        protected ResourceHandler<ItemResource> getInvWrapper(Player player) {
//            // do not cache curios handlers because they can change size dynamically
//            return CuriosUtils.makeCombinedInvWrapper(player);
//        }
//    }

    private class PlayerFoodHandler implements ResourceHandler<ItemResource> {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return 64;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return getFoodValue(resource.toStack()) > 0;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (!aiCanOperate()) return 0;

            return getPlayer().map(player -> {
                ItemStack stack = resource.toStack(amount);
                if (getFoodValue(stack) <= 0 || !okToFeed(stack, player)) {
                    return 0;
                }
                // only actually feed the player if the transaction is committed
                new RootCommitJournal(() -> feed(player, stack)).updateSnapshots(transaction);
                return amount;
            }).orElse(0);
        }

        private void feed(Player player, ItemStack stack) {
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
                player.sendOverlayMessage(Component.translatable("pneumaticcraft.gui.aerial_interface.fedItem", copy.getHoverName()));
                if (stack.getCount() == startValue) break;
            }
            // anything which couldn't be eaten goes to the player
            if (remainingItem == stack && !stack.isEmpty() && !player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
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

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        private int getFoodValue(ItemStack stack) {
            //noinspection ConstantConditions
            return stack.has(DataComponents.FOOD) ? stack.get(DataComponents.FOOD).nutrition() : 0;
        }
    }

    private class PlayerExperienceHandler implements ResourceHandler<FluidResource> {
        // XP points to be added to (or removed from, if negative) the player when the current transaction is committed
        private final XpJournal xpJournal = new XpJournal();

        private int getPlayerXP(Player player) {
            return EnchantmentUtils.getPlayerXP(player) + xpJournal.pending;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int index) {
            return curXpFluid != Fluids.EMPTY && getAmountAsLong(index) > 0 ? FluidResource.of(curXpFluid) : FluidResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            if (curXpFluid != Fluids.EMPTY) {
                return getPlayer().map(p -> (long) getPlayerXP(p) * curXpRatio).orElse(0L);
            }
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return curXpFluid != Fluids.EMPTY && resource.getFluid() == curXpFluid;
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            return getPlayer().map(player -> {
                if (curXpRatio != 0 && canFill(resource.getFluid())) {
                    int pointsAdded = amount / curXpRatio;
                    if (pointsAdded > 0) {
                        xpJournal.updateSnapshots(transaction);
                        xpJournal.pending += pointsAdded;
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
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            return getPlayer().map(player -> {
                if (curXpRatio != 0 && dispenserUpgradeInserted && aiCanOperate() && resource.getFluid() == curXpFluid) {
                    int pointsDrained = Math.min(getPlayerXP(player), amount / curXpRatio);
                    if (pointsDrained > 0) {
                        xpJournal.updateSnapshots(transaction);
                        xpJournal.pending -= pointsDrained;
                    }
                    return pointsDrained * curXpRatio;
                }
                return 0;
            }).orElse(0);
        }

        private class XpJournal extends SnapshotJournal<Integer> {
            private int pending = 0;

            @Override
            protected Integer createSnapshot() {
                return pending;
            }

            @Override
            protected void revertToSnapshot(Integer snapshot) {
                pending = snapshot;
            }

            @Override
            protected void onRootCommit(Integer originalState) {
                int delta = pending;
                pending = 0;
                getPlayer().ifPresent(player -> {
                    if (delta > 0) {
                        player.giveExperiencePoints(delta);
                    } else if (delta < 0) {
                        EnchantmentUtils.addPlayerXP(player, delta);
                    }
                });
            }
        }
    }

    public enum FeedMode implements ITranslatableEnum {
        FRUGAL("frugal", Items.COOKED_BEEF),
        GREEDY("greedy", Items.APPLE),
        SMART("smart", Items.GOLDEN_APPLE);

        private final String key;
        private final ItemStackTemplate icon;

        FeedMode(String key, Item item) {
            this.key = key;
            this.icon = new ItemStackTemplate(item);
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.tab.info.aerialInterface.feedMode." + key;
        }

        public String getDescTranslationKey() {
            return getTranslationKey() + ".desc";
        }

        public ItemStack getIconStack() {
            return icon.create();
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
