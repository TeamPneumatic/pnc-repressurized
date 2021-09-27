package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.amadron.AmadronUtil;
import me.desht.pneumaticcraft.common.amadron.ShoppingBasket;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.config.subconfig.AmadronPlayerOffers;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.living.EntityAmadrone;
import me.desht.pneumaticcraft.common.entity.living.EntityAmadrone.AmadronAction;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContainerAmadron extends ContainerPneumaticBase<TileEntityBase> {
    public static final int HARD_MAX_STACKS = 36;
    public static final int HARD_MAX_MB = 576_000;

    // this will remain valid, because Amadron offers don't get reshuffled if anyone has a tablet GUI open
    public final List<AmadronRecipe> activeOffers = new ArrayList<>(AmadronOfferManager.getInstance().getActiveOffers());
    private final ShoppingBasket shoppingBasket = new ShoppingBasket();
    private final Hand hand;

    @GuiSynced
    public final boolean[] affordableOffers = new boolean[activeOffers.size()];
    @GuiSynced
    public EnumProblemState problemState = EnumProblemState.NO_PROBLEMS;
    @GuiSynced
    public int maxPlayerOffers = 0;
    @GuiSynced
    public int currentPlayerOffers = 0;
    @GuiSynced
    private boolean basketEmpty = true;

    public enum EnumProblemState implements ITranslatableEnum {
        NO_PROBLEMS("noProblems"),
        NO_INVENTORY("noInventory"),
        NOT_ENOUGH_ITEM_SPACE("notEnoughItemSpace"),
        NOT_ENOUGH_FLUID_SPACE("notEnoughFluidSpace"),
        NOT_ENOUGH_ITEMS("notEnoughItems") /*not a ChickenBones reference*/,
        NOT_ENOUGH_FLUID("notEnoughFluid"),
        OUT_OF_STOCK("outOfStock"),
        NOT_ENOUGH_STOCK("notEnoughStock"),
        TOO_MANY_ITEMS("tooManyItems"),
        TOO_MUCH_FLUID("tooMuchFluid");

        private final String locKey;

        EnumProblemState(String locKey) {
            this.locKey = locKey;
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.gui.tab.problems.amadron." + locKey;
        }

        public EnumProblemState addProblem(EnumProblemState problem) {
            return problem == NO_PROBLEMS ? this : problem;
        }
    }

    public ContainerAmadron(int windowId, PlayerInventory invPlayer, Hand hand) {
        super(ModContainers.AMADRON.get(), windowId, invPlayer);

        this.hand = hand;

        addSyncedFields(this);

        if (invPlayer.player instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) invPlayer.player;
            ItemStack tablet = player.getItemInHand(hand);
            ShoppingBasket savedBasket = ItemAmadronTablet.loadShoppingCart(tablet);

            ShoppingBasket availableOffers = new ShoppingBasket();
            activeOffers.forEach(offer -> availableOffers.setOffer(offer.getId(), Math.max(savedBasket.getUnits(offer.getId()), 1)));
            availableOffers.cap(tablet, false);

            for (int i = 0; i < activeOffers.size(); i++) {
                ResourceLocation offerId = activeOffers.get(i).getId();

                int wantedUnits = savedBasket.getUnits(offerId);
                int availableUnits = availableOffers.getUnits(offerId);
                affordableOffers[i] = availableUnits > 0;

                if (wantedUnits > 0 && availableUnits > 0) {
                    shoppingBasket.setOffer(offerId, Math.min(availableUnits, wantedUnits));
                    // delay packet sending since clientside container not open yet
                    Objects.requireNonNull(player.getServer()).tell(new TickDelayedTask(player.getServer().getTickCount(), () ->
                            NetworkHandler.sendToPlayer(new PacketAmadronOrderResponse(offerId, availableUnits), player))
                    );
                }
            }
            basketEmpty = shoppingBasket.isEmpty();
            currentPlayerOffers = AmadronOfferManager.getInstance().countPlayerOffers(player.getGameProfile().getId());
            maxPlayerOffers = PneumaticCraftUtils.isPlayerOp(player) ? Integer.MAX_VALUE : PNCConfig.Common.Amadron.maxTradesPerPlayer;
            problemState = EnumProblemState.NO_PROBLEMS;
        }
    }

    public ContainerAmadron(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(windowId, playerInventory, buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }

    public void updateBasket(ResourceLocation offerId, int amount) {
        // called clientside when PacketAmadronOrderResponse is received
        shoppingBasket.setOffer(offerId, amount);
    }

    private void validatePurchasesCanFit() {
        AmadronOfferManager offerManager = AmadronOfferManager.getInstance();

        // An Amadrone has 35 inv upgrades, allowing 36 stacks of items and/or 576000mB of fluid to be carried
        // One Amadrone is sent per basket offer, so this is calculated on a per-offer basis
        for (ResourceLocation offerId : shoppingBasket) {
            if (offerManager.isActive(offerId)) {
                AmadronRecipe offer = offerManager.getOffer(offerId);
                AmadronTradeResource outputResource = offer.getOutput();
                int total = outputResource.totalSpaceRequired(shoppingBasket.getUnits(offerId));
                problemState = problemState.addProblem(outputResource.apply(
                        stack -> total > HARD_MAX_STACKS ? EnumProblemState.TOO_MANY_ITEMS : EnumProblemState.NO_PROBLEMS,
                        fluidStack -> total > HARD_MAX_MB ? EnumProblemState.TOO_MUCH_FLUID : EnumProblemState.NO_PROBLEMS
                ));
                // TODO shrink the basket unit count to fit
                if (problemState != EnumProblemState.NO_PROBLEMS) return;
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isBasketEmpty() {
        return basketEmpty;
    }

    public Hand getHand() {
        return hand;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        if (player.getItemInHand(hand).getItem() == ModItems.AMADRON_TABLET.get()) {
            return player.getItemInHand(hand).getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h -> {
                h.addAir(-1);
                if (h.getPressure() > 0) {
                    return true;
                } else {
                    player.displayClientMessage(new TranslationTextComponent("pneumaticcraft.gui.tab.problems.notEnoughPressure"), false);
                    return false;
                }
            }).orElse(false);
        }
        return false;
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    /**
     * Run server-side to handle an offer being clicked.
     * @param offerId numeric offer id; an index into the live offers list
     * @param mouseButton mouse button
     * @param shiftPressed true if shift-clicked
     * @param player the player
     */
    public void clickOffer(ResourceLocation offerId, int mouseButton, boolean shiftPressed, ServerPlayerEntity player) {
        problemState = EnumProblemState.NO_PROBLEMS;
        if (AmadronOfferManager.getInstance().isActive(offerId)) {
            if (mouseButton == 2) {
                // middle-click: clear slot
                shoppingBasket.remove(offerId);
            } else if (shiftPressed) {
                if (mouseButton == 0) {
                    // sneak-left-click: halve amount
                    shoppingBasket.halve(offerId);
                } else {
                    // sneak right-click: double amount (or set to 1 if 0)
                    int cur = shoppingBasket.getUnits(offerId);
                    shoppingBasket.addUnitsToOffer(offerId, cur == 0 ? 1 : cur);
                }
            } else {
                // left or right-click
                if (mouseButton == 0) {
                    shoppingBasket.addUnitsToOffer(offerId, -1);
                } else {
                    shoppingBasket.addUnitsToOffer(offerId, 1);
                }
            }
            problemState = shoppingBasket.cap(player.getItemInHand(hand), true);
            if (problemState != EnumProblemState.NO_PROBLEMS) {
                shoppingBasket.syncToPlayer(player);
            }
            NetworkHandler.sendToPlayer(new PacketAmadronOrderResponse(offerId, shoppingBasket.getUnits(offerId)), player);

            validatePurchasesCanFit();
        }
        basketEmpty = shoppingBasket.isEmpty();
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        super.handleGUIButtonPress(tag, shiftHeld, player);
        if (tag.equals("order")) {
            if (takeOrder(player, player.getItemInHand(hand))) {
                NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.CHIRP.get(), SoundCategory.PLAYERS,
                        player.getX(), player.getY(), player.getZ(), 0.2f, 1.0f, false), player);
            }
        } else if (tag.equals("addPlayerTrade")) {
            openTradeGui(player);
        } else if (tag.startsWith("remove:") && shiftHeld) {
            AmadronRecipe offer = AmadronOfferManager.getInstance().getOffer(new ResourceLocation(tag.substring(7)));
            if (offer instanceof AmadronPlayerOffer) {
                tryRemoveCustomOffer(player, (AmadronPlayerOffer) offer);
            }
        }
    }

    private boolean takeOrder(ServerPlayerEntity player, ItemStack amadronTablet) {
        if (!(amadronTablet.getItem() instanceof ItemAmadronTablet)) return false;

        String playerName = player.getName().getString();
        boolean orderPlaced = false;
        for (ResourceLocation offerId : shoppingBasket) {
            int amount = shoppingBasket.getUnits(offerId);
            if (AmadronOfferManager.getInstance().isActive(offerId) && amount > 0) {
                AmadronRecipe offer = AmadronOfferManager.getInstance().getOffer(offerId);
                if (offer.isUseableByPlayer(player)) {
                    GlobalPos itemGPos = ItemAmadronTablet.getItemProvidingLocation(amadronTablet);
                    GlobalPos fluidGPos = ItemAmadronTablet.getFluidProvidingLocation(amadronTablet);
                    EntityAmadrone drone = retrieveOrder(playerName, offer, amount, itemGPos, fluidGPos);
                    if (drone != null) {
                        drone.setHandlingOffer(offer.getId(), amount, amadronTablet, playerName, AmadronAction.TAKING_PAYMENT);
                        orderPlaced = true;
                    }
                }
            }
        }
        for (ResourceLocation offerId : shoppingBasket) {
            NetworkHandler.sendToPlayer(new PacketAmadronOrderResponse(offerId, 0), player);
        }
        shoppingBasket.clear();
        basketEmpty = true;
        return orderPlaced;
    }

    private void openTradeGui(ServerPlayerEntity player) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return StringTextComponent.EMPTY;
            }

            @Override
            public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return new ContainerAmadronAddTrade(windowId, playerInventory);
            }
        });
    }

    private void tryRemoveCustomOffer(ServerPlayerEntity player, AmadronPlayerOffer offer) {
        if (offer.isRemovableBy(player) && AmadronOfferManager.getInstance().removePlayerOffer(offer)) {
            if (PNCConfig.Common.Amadron.notifyOfTradeRemoval) {
                NetworkHandler.sendToAll(new PacketAmadronTradeRemoved(offer));
            }
            offer.returnStock();
            ItemAmadronTablet.openGui(player, hand);
        }
    }

    public static EntityAmadrone retrieveOrder(String playerName, AmadronRecipe offer, int units, GlobalPos itemGPos, GlobalPos liquidGPos) {
        final boolean isAmadronRestock = playerName == null;
        return offer.getInput().apply(
                itemStack -> retrieveOrderItems(playerName, offer, units, itemGPos, isAmadronRestock),
                fluidStack -> retrieveOrderFluid(playerName, offer, units, liquidGPos, isAmadronRestock)
        );
    }

    private static EntityAmadrone retrieveOrderItems(String playerName, AmadronRecipe offer, int units, GlobalPos itemGPos, boolean isAmadronRestock) {
        if (itemGPos == null || !validateStockLevel(playerName, offer, units, isAmadronRestock)) return null;

        ItemStack queryingItems = offer.getInput().getItem();
        ItemStack[] stacks = AmadronUtil.buildStacks(queryingItems, units);
        if (stacks.length == 0) {
            // shouldn't happen but see https://github.com/TeamPneumatic/pnc-repressurized/issues/399
            Log.error(String.format("retrieveOrderItems: got empty itemstack list for offer %d x %s @ %s", units, queryingItems, itemGPos));
            return null;
        }

        reduceStockLevel(offer, units, isAmadronRestock);
        return (EntityAmadrone) DroneRegistry.getInstance().retrieveItemsAmazonStyle(itemGPos, stacks);
    }

    private static EntityAmadrone retrieveOrderFluid(String playerName, AmadronRecipe offer, int units, GlobalPos liquidGPos, boolean isAmadronRestock) {
        if (liquidGPos == null || !validateStockLevel(playerName, offer, units, isAmadronRestock)) return null;

        FluidStack queryingFluid = AmadronUtil.buildFluidStack(offer.getInput().getFluid(), units);

        reduceStockLevel(offer, units, isAmadronRestock);
        return (EntityAmadrone) DroneRegistry.getInstance().retrieveFluidAmazonStyle(liquidGPos, queryingFluid);
    }

    private static void reduceStockLevel(AmadronRecipe offer, int units, boolean isAmadronRestock) {
        // Reduce stock here; if the order fails (e.g. player takes items out of the chest before the Amadron can get them),
        // we'll restore the stock level, in EventHandlerAmadron#onAmadronFailure().
        if (!isAmadronRestock && (offer instanceof AmadronPlayerOffer || offer.getMaxStock() >= 0)) {
            offer.setStock(offer.getStock() - units);
            if (offer instanceof AmadronPlayerOffer) AmadronPlayerOffers.save();
            NetworkHandler.sendNonLocal(new PacketAmadronStockUpdate(offer.getId(), offer.getStock()));
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean validateStockLevel(String playerName, AmadronRecipe offer, int units, boolean isAmadronRestock) {
        if (!isAmadronRestock && offer.getStock() >= 0 && units > offer.getStock()) {
            // shouldn't happen normally, but could as result of a player trying to spoof the system
            // by bypassing stock checks in the Amadron GUI (e.g. hacked client)
            // https://github.com/TeamPneumatic/pnc-repressurized/issues/736
            Log.warning("ignoring suspicious order from player [%s] for %d x %s - only %d in stock right now!",
                    playerName, units, offer, offer.getStock());
            return false;
        }
        return true;
    }

    public int getShoppingBasketUnits(ResourceLocation offerId) {
        return shoppingBasket.getUnits(offerId);
    }

    @Override
    public void removed(PlayerEntity player) {
        super.removed(player);
        if (!player.level.isClientSide && player.getItemInHand(hand).getItem() == ModItems.AMADRON_TABLET.get()) {
            ItemAmadronTablet.saveShoppingCart(player.getItemInHand(hand), shoppingBasket);
        }
    }

}
