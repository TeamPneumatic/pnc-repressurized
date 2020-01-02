package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.config.aux.AmadronOfferStaticConfig;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeRemoved;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer.TradeType;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
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
import net.minecraft.util.IntReferenceHolder;
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
//    public static boolean mayAddPeriodicOffers = true;
//    public static boolean mayAddStaticOffers = true;

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

    private final IntReferenceHolder permsHolder = IntReferenceHolder.single();

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

        trackInt(permsHolder);


        PlayerEntity player = invPlayer.player;
        if (!player.world.isRemote) {
            IItemHandler itemHandler = ItemAmadronTablet.getItemCapability(player.getHeldItem(hand)).map(h -> h).orElse(null);
            IFluidHandler fluidHandler = ItemAmadronTablet.getFluidCapability(player.getHeldItem(hand)).map(h -> h).orElse(null);
            for (int i = 0; i < offers.size(); i++) {
                int amount = capShoppingAmount(offers.get(i), 1, itemHandler, fluidHandler, this);
                buyableOffers[i] = amount > 0;
            }
            problemState = EnumProblemState.NO_PROBLEMS;

            Map<AmadronOffer, Integer> shoppingCart = ItemAmadronTablet.getShoppingCart(player.getHeldItem(hand));
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
            maxOffers = PneumaticCraftUtils.isPlayerOp(player) ? Integer.MAX_VALUE : PNCConfig.Common.Amadron.maxTradesPerPlayer;

            int n = (PermissionAPI.hasPermission(player, Names.AMADRON_ADD_PERIODIC_TRADE) ? 0x1 : 0)
                    | (PermissionAPI.hasPermission(player, Names.AMADRON_ADD_STATIC_TRADE) ? 0x2 : 0);
            permsHolder.set(n);
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

    public boolean mayAddPeriodicTrades() {
        return (permsHolder.get() & 0x1) != 0;
    }

    public boolean mayAddStaticTrades() {
        return (permsHolder.get() & 0x2) != 0;
    }

    @Override
    public void putStackInSlot(int slot, @Nonnull ItemStack stack) {
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        if (player.getHeldItem(hand).getItem() == ModItems.AMADRON_TABLET) {
            return player.getHeldItem(hand).getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h -> {
                h.addAir(-1);
                if (h.getPressure() > 0) {
                    return true;
                } else {
                    player.sendStatusMessage(new TranslationTextComponent("gui.tab.problems.notEnoughPressure"), false);
                    return false;
                }
            }).orElse(false);
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
                if (PNCConfig.Common.Amadron.notifyOfTradeRemoval) NetworkHandler.sendToAll(new PacketAmadronTradeRemoved(offer));
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
                    GlobalPos itemGPos = ItemAmadronTablet.getItemProvidingLocation(player.getHeldItem(hand));
                    GlobalPos fluidGPos = ItemAmadronTablet.getFluidProvidingLocation(player.getHeldItem(hand));
                    EntityDrone drone = retrieveOrderItems(offer, shoppingAmounts[i], itemGPos, fluidGPos);
                    if (drone != null) {
                        drone.setHandlingOffer(offer, shoppingAmounts[i], player.getHeldItem(hand), player.getName().getFormattedText());
                        placed = true;
                    }
                }
                if (placed && player instanceof ServerPlayerEntity) {
                    NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.CHIRP, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 0.2f, 1.0f, false), (ServerPlayerEntity) player);
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
        if (offer.getInput().getType() == AmadronTradeResource.Type.ITEM) {
            if (itemGPos == null) return null;
            ItemStack queryingItems = offer.getInput().getItem();
            int amount = queryingItems.getCount() * times;
            NonNullList<ItemStack> stacks = NonNullList.create();
            while (amount > 0) {
                ItemStack stack = queryingItems.copy();
                stack.setCount(Math.min(amount, stack.getMaxStackSize()));
                stacks.add(stack);
                amount -= stack.getCount();
            }
            return (EntityDrone) DroneRegistry.getInstance().retrieveItemsAmazonStyle(itemGPos, stacks.toArray(new ItemStack[0]));
        } else if (offer.getInput().getType() == AmadronTradeResource.Type.FLUID) {
            if (liquidGPos == null) return null;
            FluidStack queryingFluid = offer.getInput().getFluid().copy();
            queryingFluid.setAmount(queryingFluid.getAmount() * times);
            return (EntityDrone) DroneRegistry.getInstance().retrieveFluidAmazonStyle(liquidGPos, queryingFluid);
        } else {
            return null;
        }
    }

    private int capShoppingAmount(int offerId, int wantedAmount, PlayerEntity player) {
        // this is a bit of an abuse of lazy optionals, but try doing it another way...
        ItemStack tablet = player.getHeldItem(hand);
        IItemHandler itemHandler = ItemAmadronTablet.getItemCapability(tablet).map(h -> h).orElse(null);
        IFluidHandler fluidHandler = ItemAmadronTablet.getFluidCapability(tablet).map(h -> h).orElse(null);

        return capShoppingAmount(offers.get(offerId), wantedAmount, itemHandler, fluidHandler, this);
    }

    private static int capShoppingAmount(AmadronOffer offer, int wantedAmount, IItemHandler inv, IFluidHandler fluidHandler, ContainerAmadron container) {
        return capShoppingAmount(offer, wantedAmount, inv, inv, fluidHandler, fluidHandler, container);
    }

    public static int capShoppingAmount(AmadronOffer offer, int wantedTradeCount, IItemHandler inputInv, IItemHandler outputInv,
                                        IFluidHandler inputFluidHandler, IFluidHandler outputFluidHandler, ContainerAmadron container) {
        if (container != null && offer.getStock() >= 0 && wantedTradeCount > offer.getStock()) {
            wantedTradeCount = offer.getStock();
            container.problemState = EnumProblemState.OUT_OF_STOCK;
        }

        EnumProblemState problem = null;

        // check there's enough of the required item/fluid in the input inventory/tank
        if (offer.getInput().getType() == AmadronTradeResource.Type.ITEM) {
            if (inputInv != null) {
                int availableTrades = offer.getInput().countTradesInInventory(inputInv);
                if (wantedTradeCount > availableTrades) {
                    problem = EnumProblemState.NOT_ENOUGH_ITEMS;
                    wantedTradeCount = availableTrades;
                }
            } else if (outputInv == null) {
                wantedTradeCount = 0;
                problem = EnumProblemState.NO_ITEM_PROVIDER;
            }
        } else if (offer.getInput().getType() == AmadronTradeResource.Type.FLUID) {
            if (inputFluidHandler != null) {
                int availableTrades = offer.getInput().countTradesInTank(inputFluidHandler);
                if (wantedTradeCount > availableTrades) {
                    problem = EnumProblemState.NOT_ENOUGH_FLUID;
                    wantedTradeCount = availableTrades;
                }
            } else if (outputFluidHandler == null) {
                wantedTradeCount = 0;
                problem = EnumProblemState.NO_FLUID_PROVIDER;
            }
        }

        // check there's enough space for the returned item/fluid in the output inventory/tank
        if (offer.getOutput().getType() == AmadronTradeResource.Type.ITEM) {
            if (outputInv != null) {
                int availableTrades = offer.getOutput().findSpaceInOutput(outputInv, wantedTradeCount);
                if (wantedTradeCount > availableTrades) {
                    wantedTradeCount = availableTrades;
                    problem = EnumProblemState.NOT_ENOUGH_ITEM_SPACE;
                }
            } else if (inputInv == null) {
                wantedTradeCount = 0;
                problem = EnumProblemState.NO_ITEM_PROVIDER;
            }
        } else if (offer.getOutput().getType() == AmadronTradeResource.Type.FLUID) {
            if (outputFluidHandler != null) {
                int availableTrades = offer.getOutput().findSpaceInOutput(outputFluidHandler, wantedTradeCount);
                if (wantedTradeCount > availableTrades) {
                    wantedTradeCount = availableTrades;
                    problem = EnumProblemState.NOT_ENOUGH_FLUID_SPACE;
                }
            } else if (inputFluidHandler == null) {
                wantedTradeCount = 0;
                problem = EnumProblemState.NO_FLUID_PROVIDER;
            }
        }

        if (problem != null && container != null) container.problemState = problem;
        return wantedTradeCount;
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
        if (!player.world.isRemote && player.getHeldItem(hand).getItem() == ModItems.AMADRON_TABLET) {
            Map<AmadronOffer, Integer> shoppingCart = new HashMap<>();
            for (int i = 0; i < shoppingItems.length; i++) {
                if (shoppingItems[i] >= 0) {
                    shoppingCart.put(offers.get(shoppingItems[i]), shoppingAmounts[i]);
                }
            }
            ItemAmadronTablet.setShoppingCart(player.getHeldItem(hand), shoppingCart);
        }
    }
}
