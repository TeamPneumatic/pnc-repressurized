package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.config.AmadronOfferSettings;
import me.desht.pneumaticcraft.common.config.AmadronOfferStaticConfig;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.Sounds;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeRemoved;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer.TradeType;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.server.permission.PermissionAPI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class ContainerAmadron extends ContainerPneumaticBase<TileEntityBase> {
    public static final int ROWS = 4;
    public static final int OFFERS_PER_PAGE = ROWS * 2;

    // Set client-side when a PacketSyncAmadronOffers is received.  Only controls button visibility; actual control
    // is done server-side via permissions API.  So make button available unless server tells us no.  Worst that
    // can happen is button does nothing.
    public static boolean mayAddPeriodicOffers = true;
    public static boolean mayAddStaticOffers = true;

    public final List<AmadronOffer> offers = new ArrayList<>(AmadronOfferManager.getInstance().getAllOffers());

    private final ItemStackHandler inv = new ItemStackHandler(OFFERS_PER_PAGE * 2);

    @GuiSynced
    private final int[] shoppingItems = new int[OFFERS_PER_PAGE];
    @GuiSynced
    private final int[] shoppingAmounts = new int[OFFERS_PER_PAGE];
    @GuiSynced
    public final boolean[] buyableOffers = new boolean[offers.size()];
    private final Hand hand;
    @GuiSynced
    public EnumProblemState problemState = EnumProblemState.NO_PROBLEMS;
    @GuiSynced
    public int maxOffers = 0;
    @GuiSynced
    public int currentOffers = 0;
    @GuiSynced
    private boolean basketEmpty = true;

    public enum EnumProblemState {
        NO_PROBLEMS("noProblems"),
        NO_ITEM_PROVIDER("noItemProvider"),
        NO_FLUID_PROVIDER("noFluidProvider"),
        NOT_ENOUGH_ITEM_SPACE("notEnoughItemSpace"),
        NOT_ENOUGH_FLUID_SPACE("notEnoughFluidSpace"),
        NOT_ENOUGH_ITEMS("notEnoughItems") /*not a ChickenBones reference*/,
        NOT_ENOUGH_FLUID("notEnoughFluid"),
        OUT_OF_STOCK("outOfStock");

        private final String locKey;

        EnumProblemState(String locKey) {
            this.locKey = locKey;
        }

        public String getTranslationKey() {
            return "gui.tab.problems.amadron." + locKey;
        }

    }
    public ContainerAmadron(int windowId, PlayerInventory invPlayer, Hand hand) {
        super(ModContainerTypes.AMADRON, windowId, invPlayer);

        this.hand = hand;

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < 2; x++) {
                addSlot(new SlotUntouchable(inv, y * 4 + x * 2, x * 73 + 12, y * 35 + 70));
                addSlot(new SlotUntouchable(inv, y * 4 + x * 2 + 1, x * 73 + 57, y * 35 + 70));
            }
        }
        addSyncedFields(this);
        Arrays.fill(shoppingItems, -1);

        PlayerEntity player = invPlayer.player;
        if (!player.world.isRemote) {
            IItemHandler itemHandler = ItemAmadronTablet.getItemProvider(player.getHeldItemMainhand()).map(h -> h).orElse(null);
            IFluidHandler fluidHandler = ItemAmadronTablet.getFluidProvider(player.getHeldItemMainhand()).map(h -> h).orElse(null);
            for (int i = 0; i < offers.size(); i++) {
                int amount = capShoppingAmount(offers.get(i), 1, itemHandler, fluidHandler, this);
                buyableOffers[i] = amount > 0;
            }
            problemState = EnumProblemState.NO_PROBLEMS;

            Map<AmadronOffer, Integer> shoppingCart = ItemAmadronTablet.getShoppingCart(player.getHeldItemMainhand());
            for (Map.Entry<AmadronOffer, Integer> cartItem : shoppingCart.entrySet()) {
                int offerId = offers.indexOf(cartItem.getKey());
                if (offerId >= 0) {
                    int index = getCartSlot(offerId);
                    if (index >= 0) {
                        shoppingItems[index] = offerId;
                        shoppingAmounts[index] = cartItem.getValue();
                    }
                }
            }
            basketEmpty = Arrays.stream(shoppingAmounts).noneMatch(shoppingAmount -> shoppingAmount > 0);
            currentOffers = AmadronOfferManager.getInstance().countOffers(player.getGameProfile().getId().toString());
            maxOffers = PneumaticCraftUtils.isPlayerOp(player) ? Integer.MAX_VALUE : AmadronOfferSettings.maxTradesPerPlayer;
        }
    }

    public ContainerAmadron(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(windowId, playerInventory, buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }

    public boolean isBasketEmpty() {
        return basketEmpty;
    }

    public Hand getHand() {
        return hand;
    }

    @Override
    public void putStackInSlot(int slot, @Nonnull ItemStack stack) {
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        if (player.getHeldItemMainhand().getItem() == ModItems.AMADRON_TABLET) {
            IPressurizable pressurizable = (IPressurizable) ModItems.AMADRON_TABLET;
            pressurizable.addAir(player.getHeldItemMainhand(), -1);
            if (pressurizable.getPressure(player.getHeldItemMainhand()) > 0) {
                return true;
            } else {
                player.sendStatusMessage(new TranslationTextComponent("gui.tab.problems.notEnoughPressure"), false);
            }
        }
        return false;
    }

    public void clearStacks() {
        for (int i = 0; i < inv.getSlots(); i++) {
            inv.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public void setStack(int index, ItemStack stack) {
        inv.setStackInSlot(index, stack);
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity p_82846_1_, int p_82846_2_) {
        return ItemStack.EMPTY;
    }

    public void clickOffer(int offerId, int mouseButton, boolean sneaking, PlayerEntity player) {
        problemState = EnumProblemState.NO_PROBLEMS;
        int cartSlot = getCartSlot(offerId);
        if (cartSlot >= 0) {
            if (mouseButton == 2) {  // middle-click
                shoppingAmounts[cartSlot] = 0;
            } else if (sneaking) {
                if (mouseButton == 0) { // sneak-left-click
                    shoppingAmounts[cartSlot] /= 2;
                } else { // sneak-right-click
                    AmadronOffer offer = offers.get(offerId);
                    if (offer instanceof AmadronOfferCustom) {
                        removeCustomOffer(player, (AmadronOfferCustom) offer);
                    } else {
                        shoppingAmounts[cartSlot] *= 2;
                        if (shoppingAmounts[cartSlot] == 0) shoppingAmounts[cartSlot] = 1;
                    }
                }
            } else { // left or right-click
                if (mouseButton == 0) shoppingAmounts[cartSlot]--;
                else shoppingAmounts[cartSlot]++;
            }
            if (shoppingAmounts[cartSlot] <= 0) {
                shoppingAmounts[cartSlot] = 0;
                shoppingItems[cartSlot] = -1;
            } else {
                shoppingAmounts[cartSlot] = capShoppingAmount(offerId, shoppingAmounts[cartSlot], player);
                shoppingItems[cartSlot] = shoppingAmounts[cartSlot] > 0 ? offerId : -1;
            }
        }
        basketEmpty = Arrays.stream(shoppingAmounts).noneMatch(shoppingAmount -> shoppingAmount > 0);
    }

    private void removeCustomOffer(PlayerEntity player, AmadronOfferCustom offer) {
        if (offer.getPlayerId().equals(player.getGameProfile().getId().toString())) {
            if (AmadronOfferManager.getInstance().removeStaticOffer(offer)) {
                if (AmadronOfferSettings.notifyOfTradeRemoval) NetworkHandler.sendToAll(new PacketAmadronTradeRemoved(offer));
                offer.returnStock();
                try {
                    AmadronOfferStaticConfig.INSTANCE.writeToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                player.closeScreen();
            }
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, PlayerEntity player) {
        super.handleGUIButtonPress(tag, player);
        if (tag.equals("order")) {
            boolean placed = false;
            for (int i = 0; i < shoppingItems.length; i++) {
                if (shoppingItems[i] >= 0) {
                    AmadronOffer offer = offers.get(shoppingItems[i]);
                    GlobalPos itemGPos = ItemAmadronTablet.getItemProvidingLocation(player.getHeldItemMainhand());
                    GlobalPos fluidGPos = ItemAmadronTablet.getFluidProvidingLocation(player.getHeldItemMainhand());
                    EntityDrone drone = retrieveOrderItems(offer, shoppingAmounts[i], itemGPos, fluidGPos);
                    if (drone != null) {
                        drone.setHandlingOffer(offer, shoppingAmounts[i], player.getHeldItemMainhand(), player.getName().getFormattedText());
                        placed = true;
                    }
                }
                if (placed && player instanceof ServerPlayerEntity) {
                    NetworkHandler.sendToPlayer(new PacketPlaySound(Sounds.CHIRP, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 0.2f, 1.0f, false), (ServerPlayerEntity) player);
                }
            }
            Arrays.fill(shoppingAmounts, 0);
            Arrays.fill(shoppingItems, -1);
            basketEmpty = true;
        } else if (tag.equals("addPlayerTrade")) {
            openTradeGui(player, TradeType.PLAYER);
        } else if (tag.equals("addPeriodicTrade") && PermissionAPI.hasPermission(player, Names.AMADRON_ADD_PERIODIC_TRADE)) {
            openTradeGui(player, TradeType.PERIODIC);
        } else if (tag.equals("addStaticTrade") && PermissionAPI.hasPermission(player, Names.AMADRON_ADD_STATIC_TRADE)) {
            openTradeGui(player, TradeType.STATIC);
        }
    }

    private void openTradeGui(PlayerEntity player, TradeType type) {
        NetworkHooks.openGui((ServerPlayerEntity) player, new AmadronAddTradeContainerProvider(type), buf -> buf.writeByte(type.ordinal()));
    }

    private static class AmadronAddTradeContainerProvider implements INamedContainerProvider {
        private final TradeType tradeType;

        private AmadronAddTradeContainerProvider(TradeType tradeType) {
            this.tradeType = tradeType;
        }

        @Override
        public ITextComponent getDisplayName() {
            return new StringTextComponent("Add Trade");
        }

        @Nullable
        @Override
        public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
            return new ContainerAmadronAddTrade(windowId, playerInventory, tradeType);
        }
    }

    public static EntityDrone retrieveOrderItems(AmadronOffer offer, int times, GlobalPos itemGPos, GlobalPos liquidGPos) {
        if (offer.getInput() instanceof ItemStack) {
            if (itemGPos == null) return null;
            ItemStack queryingItems = (ItemStack) offer.getInput();
            int amount = queryingItems.getCount() * times;
            NonNullList<ItemStack> stacks = NonNullList.create();
            while (amount > 0) {
                ItemStack stack = queryingItems.copy();
                stack.setCount(Math.min(amount, stack.getMaxStackSize()));
                stacks.add(stack);
                amount -= stack.getCount();
            }
            return (EntityDrone) DroneRegistry.getInstance().retrieveItemsAmazonStyle(itemGPos, stacks.toArray(new ItemStack[0]));
        } else {
            if (liquidGPos == null) return null;
            FluidStack queryingFluid = ((FluidStack) offer.getInput()).copy();
            queryingFluid.amount *= times;
            return (EntityDrone) DroneRegistry.getInstance().retrieveFluidAmazonStyle(liquidGPos, queryingFluid);
        }
    }

    private int capShoppingAmount(int offerId, int wantedAmount, PlayerEntity player) {
        return ItemAmadronTablet.getItemProvider(player.getHeldItemMainhand()).map(itemHandler ->
                ItemAmadronTablet.getFluidProvider(player.getHeldItemMainhand()).map(fluidHandler ->
                        capShoppingAmount(offers.get(offerId), wantedAmount, itemHandler, fluidHandler, this)
                ).orElse(0)).orElse(0);
    }

    private static int capShoppingAmount(AmadronOffer offer, int wantedAmount, IItemHandler inv, IFluidHandler fluidHandler, ContainerAmadron container) {
        return capShoppingAmount(offer, wantedAmount, inv, inv, fluidHandler, fluidHandler, container);
    }

    public static int capShoppingAmount(AmadronOffer offer, int wantedAmount, IItemHandler inputInv, IItemHandler outputInv,
                                        IFluidHandler inputFluidHandler, IFluidHandler outputFluidHandler, ContainerAmadron container) {
        if (container != null && offer.getStock() >= 0 && wantedAmount > offer.getStock()) {
            wantedAmount = offer.getStock();
            container.problemState = EnumProblemState.OUT_OF_STOCK;
        }
        if (offer.getInput() instanceof ItemStack) {
            if (inputInv != null) {
                ItemStack searchingItem = (ItemStack) offer.getInput();
                int count = 0;
                for (int i = 0; i < inputInv.getSlots(); i++) {
                    if (inputInv.getStackInSlot(i).isItemEqual(searchingItem) && ItemStack.areItemStackTagsEqual(inputInv.getStackInSlot(i), searchingItem)) {
                        count += inputInv.getStackInSlot(i).getCount();
                    }
                }
                int maxAmount = count / ((ItemStack) offer.getInput()).getCount();
                if (wantedAmount > maxAmount) {
                    if (container != null) container.problemState = EnumProblemState.NOT_ENOUGH_ITEMS;
                    wantedAmount = maxAmount;
                }
            } else if (outputInv == null) {
                wantedAmount = 0;
                if (container != null) container.problemState = EnumProblemState.NO_ITEM_PROVIDER;
            }
        } else {
            if (inputFluidHandler != null) {
                FluidStack searchingFluid = ((FluidStack) offer.getInput()).copy();
                searchingFluid.amount = Integer.MAX_VALUE;
                FluidStack extracted = inputFluidHandler.drain(searchingFluid, false);
                int maxAmount = 0;
                if (extracted != null) maxAmount = extracted.amount / ((FluidStack) offer.getInput()).amount;
                if (wantedAmount > maxAmount) {
                    if (container != null) container.problemState = EnumProblemState.NOT_ENOUGH_FLUID;
                    wantedAmount = maxAmount;
                }
            } else if (outputFluidHandler == null) {
                wantedAmount = 0;
                if (container != null) container.problemState = EnumProblemState.NO_FLUID_PROVIDER;
            }
        }
        if (offer.getOutput() instanceof ItemStack) {
            if (outputInv != null) {
                ItemStack providingItem = ((ItemStack) offer.getOutput()).copy();
                providingItem.setCount(providingItem.getCount() * wantedAmount);
                ItemStack remainder = ItemHandlerHelper.insertItem(outputInv, providingItem.copy(), true);
                if (!remainder.isEmpty()) {
                    int maxAmount = (providingItem.getCount() - remainder.getCount()) / ((ItemStack) offer.getOutput()).getCount();
                    if (wantedAmount > maxAmount) {
                        wantedAmount = maxAmount;
                        if (container != null) container.problemState = EnumProblemState.NOT_ENOUGH_ITEM_SPACE;
                    }
                }
            } else if (inputInv == null) {
                wantedAmount = 0;
                if (container != null) container.problemState = EnumProblemState.NO_ITEM_PROVIDER;
            }
        } else {
            if (outputFluidHandler != null) {
                FluidStack providingFluid = ((FluidStack) offer.getOutput()).copy();
                providingFluid.amount *= wantedAmount;
                int amountFilled = outputFluidHandler.fill(providingFluid, false);
                int maxAmount = amountFilled / ((FluidStack) offer.getOutput()).amount;
                if (wantedAmount > maxAmount) {
                    wantedAmount = maxAmount;
                    if (container != null) container.problemState = EnumProblemState.NOT_ENOUGH_FLUID_SPACE;
                }
            } else if (inputFluidHandler == null) {
                wantedAmount = 0;
                if (container != null) container.problemState = EnumProblemState.NO_FLUID_PROVIDER;
            }
        }
        return wantedAmount;
    }

    private int getCartSlot(int offerId) {
        int freeSlot = -1;
        for (int i = 0; i < shoppingItems.length; i++) {
            if (shoppingItems[i] == offerId) {
                return i;
            } else if (freeSlot == -1 && shoppingItems[i] == -1) {
                freeSlot = i;
            }
        }
        return freeSlot;
    }

    public int getShoppingCartAmount(AmadronOffer offer) {
        int offerId = offers.indexOf(offer);
        for (int i = 0; i < shoppingItems.length; i++) {
            if (shoppingItems[i] == offerId) {
                return shoppingAmounts[i];
            }
        }
        return 0;
    }

    @Override
    public void onContainerClosed(PlayerEntity player) {
        super.onContainerClosed(player);
        if (!player.world.isRemote && player.getHeldItemMainhand().getItem() == ModItems.AMADRON_TABLET) {
            Map<AmadronOffer, Integer> shoppingCart = new HashMap<>();
            for (int i = 0; i < shoppingItems.length; i++) {
                if (shoppingItems[i] >= 0) {
                    shoppingCart.put(offers.get(shoppingItems[i]), shoppingAmounts[i]);
                }
            }
            ItemAmadronTablet.setShoppingCart(player.getHeldItemMainhand(), shoppingCart);
        }
    }
}
