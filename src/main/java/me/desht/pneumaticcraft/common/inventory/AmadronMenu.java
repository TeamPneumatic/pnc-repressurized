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

package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.amadron.*;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.subconfig.AmadronPlayerOffers;
import me.desht.pneumaticcraft.common.drone.DroneRegistry;
import me.desht.pneumaticcraft.common.entity.drone.AmadroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.AmadroneEntity.AmadronAction;
import me.desht.pneumaticcraft.common.item.AmadronTabletItem;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AmadronMenu extends AbstractPneumaticCraftMenu<AbstractPneumaticCraftBlockEntity> {
    public static final int HARD_MAX_STACKS = 36;
    public static final int HARD_MAX_MB = 576_000;

    // this will remain valid, because Amadron offers don't get reshuffled if anyone has a tablet GUI open
    public final List<AmadronOffer> activeOffers = new ArrayList<>(AmadronOfferManager.getInstance().getActiveOffers());
    private final MutableBasket shoppingBasket = ShoppingBasket.createMutable();
    private final InteractionHand hand;

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

    public AmadronMenu(int windowId, Inventory invPlayer, InteractionHand hand) {
        super(ModMenuTypes.AMADRON.get(), windowId, invPlayer);

        this.hand = hand;

        addSyncedFields(this);

        if (invPlayer.player instanceof ServerPlayer player) {
            ItemStack tablet = player.getItemInHand(hand);
            ImmutableBasket savedBasket = AmadronTabletItem.loadShoppingCart(tablet);

            MutableBasket availableOffers = ShoppingBasket.createMutable();
            activeOffers.forEach(offer -> availableOffers.setUnits(offer.getOfferId(), Math.max(savedBasket.getUnits(offer.getOfferId()), 1)));
            availableOffers.validate(tablet, false);

            for (int i = 0; i < activeOffers.size(); i++) {
                ResourceLocation offerId = activeOffers.get(i).getOfferId();

                int wantedUnits = savedBasket.getUnits(offerId);
                int availableUnits = availableOffers.getUnits(offerId);
                affordableOffers[i] = availableUnits > 0;

                if (wantedUnits > 0 && availableUnits > 0) {
                    shoppingBasket.setUnits(offerId, Math.min(availableUnits, wantedUnits));
                    // delay packet sending since clientside container not open yet
                    Objects.requireNonNull(player.getServer()).tell(new TickTask(player.getServer().getTickCount(), () ->
                            NetworkHandler.sendToPlayer(new PacketAmadronOrderResponse(offerId, availableUnits), player))
                    );
                }
            }
            basketEmpty = shoppingBasket.isEmpty();
            currentPlayerOffers = AmadronOfferManager.getInstance().countPlayerOffers(player.getGameProfile().getId());
            maxPlayerOffers = PneumaticCraftUtils.isPlayerOp(player) ? Integer.MAX_VALUE : ConfigHelper.common().amadron.maxTradesPerPlayer.get();
            problemState = EnumProblemState.NO_PROBLEMS;
        }
    }

    public AmadronMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public void updateBasket(ResourceLocation offerId, int amount) {
        // called clientside when PacketAmadronOrderResponse is received
        shoppingBasket.setUnits(offerId, amount);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isBasketEmpty() {
        return basketEmpty;
    }

    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.getItemInHand(hand).getItem() == ModItems.AMADRON_TABLET.get()) {
            return PNCCapabilities.getAirHandler(player.getItemInHand(hand)).map(h -> {
                h.addAir(-1);
                if (h.getPressure() > 0) {
                    return true;
                } else {
                    player.displayClientMessage(Component.translatable("pneumaticcraft.gui.tab.problems.notEnoughPressure"), false);
                    return false;
                }
            }).orElse(false);
        }
        return false;
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    /**
     * Called server-side via PacketAmadronOrderUpdate to handle an offer being clicked.
     * @param offerId the offer id
     * @param mouseButton which mouse button
     * @param shiftPressed true if shift-clicked
     * @param player the player
     */
    public void clickOffer(ResourceLocation offerId, int mouseButton, boolean shiftPressed, ServerPlayer player) {
        problemState = EnumProblemState.NO_PROBLEMS;
        if (AmadronOfferManager.getInstance().isActive(offerId)) {
            if (mouseButton == 2) {
                // middle-click: clear slot
                shoppingBasket.remove(offerId);
            } else if (shiftPressed) {
                if (mouseButton == 0) {
                    // shift-left-click: halve amount
                    shoppingBasket.halve(offerId);
                } else {
                    // shift-right-click: double amount (or set to 1 if 0)
                    int units = shoppingBasket.getUnits(offerId);
                    shoppingBasket.addUnitsToOffer(offerId, units == 0 ? 1 : units);
                }
            } else {
                // left-click (-1) or right-click (+1)
                shoppingBasket.addUnitsToOffer(offerId, mouseButton == 0 ? -1 : 1);
            }
            problemState = shoppingBasket.validate(player.getItemInHand(hand), true);
            if (problemState != EnumProblemState.NO_PROBLEMS) {
                // sync the whole basket; modifications have been made
                shoppingBasket.syncToPlayer(player);
            } else {
                // just sync the order that was updated
                NetworkHandler.sendToPlayer(new PacketAmadronOrderResponse(offerId, shoppingBasket.getUnits(offerId)), player);
            }
        }
        basketEmpty = shoppingBasket.isEmpty();
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayer player) {
        super.handleGUIButtonPress(tag, shiftHeld, player);
        if (tag.equals("order")) {
            if (takeOrder(player, player.getItemInHand(hand))) {
                NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.CHIRP.get(), SoundSource.PLAYERS,
                        player.blockPosition(), 0.2f, 1.0f, false), player);
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

    private boolean takeOrder(ServerPlayer player, ItemStack amadronTablet) {
        if (!(amadronTablet.getItem() instanceof AmadronTabletItem)) return false;

        String playerName = player.getName().getString();
        boolean orderPlaced = false;
        for (ResourceLocation offerId : shoppingBasket) {
            int amount = shoppingBasket.getUnits(offerId);
            if (AmadronOfferManager.getInstance().isActive(offerId) && amount > 0) {
                AmadronOffer offer = AmadronOfferManager.getInstance().getOffer(offerId);
                if (offer.isUsableByPlayer(player)) {
                    GlobalPos itemGPos = AmadronTabletItem.getItemProvidingLocation(amadronTablet);
                    GlobalPos fluidGPos = AmadronTabletItem.getFluidProvidingLocation(amadronTablet);
                    AmadroneEntity drone = retrieveOrder(playerName, offer, amount, itemGPos, fluidGPos);
                    if (drone != null) {
                        drone.setHandlingOffer(offer.getOfferId(), amount, amadronTablet, playerName, AmadronAction.TAKING_PAYMENT);
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

    private void openTradeGui(ServerPlayer player) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.empty();
            }

            @Override
            public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                return new AmadronAddTradeMenu(windowId, playerInventory);
            }
        });
    }

    private void tryRemoveCustomOffer(ServerPlayer player, AmadronPlayerOffer offer) {
        if (offer.isRemovableBy(player) && AmadronOfferManager.getInstance().removePlayerOffer(offer)) {
            if (ConfigHelper.common().amadron.notifyOfTradeRemoval.get()) {
                NetworkHandler.sendToAll(new PacketAmadronTradeRemoved(offer));
            }
            offer.returnStock();
            AmadronTabletItem.openGui(player, hand);
        }
    }

    public static AmadroneEntity retrieveOrder(String playerName, AmadronOffer offer, int units, GlobalPos itemGPos, GlobalPos liquidGPos) {
        final boolean isAmadronRestock = playerName == null;
        return offer.getInput().apply(
                itemStack -> retrieveOrderItems(playerName, offer, units, itemGPos, isAmadronRestock),
                fluidStack -> retrieveOrderFluid(playerName, offer, units, liquidGPos, isAmadronRestock)
        );
    }

    private static AmadroneEntity retrieveOrderItems(String playerName, AmadronOffer offer, int units, GlobalPos itemGPos, boolean isAmadronRestock) {
        if (itemGPos == null || !validateStockLevel(playerName, offer, units, isAmadronRestock)) return null;

        ItemStack queryingItems = offer.getInput().getItem();
        ItemStack[] stacks = AmadronUtil.buildStacks(queryingItems, units);
        if (stacks.length == 0) {
            // shouldn't happen but see https://github.com/TeamPneumatic/pnc-repressurized/issues/399
            Log.error("retrieveOrderItems: got empty itemstack list for offer {} x {} @ {}", units, queryingItems, itemGPos);
            return null;
        }

        reduceStockLevel(offer, units, isAmadronRestock);
        return (AmadroneEntity) DroneRegistry.getInstance().retrieveItemsAmazonStyle(itemGPos, stacks);
    }

    private static AmadroneEntity retrieveOrderFluid(String playerName, AmadronOffer offer, int units, GlobalPos liquidGPos, boolean isAmadronRestock) {
        if (liquidGPos == null || !validateStockLevel(playerName, offer, units, isAmadronRestock)) return null;

        FluidStack queryingFluid = AmadronUtil.buildFluidStack(offer.getInput().getFluid(), units);

        reduceStockLevel(offer, units, isAmadronRestock);
        return (AmadroneEntity) DroneRegistry.getInstance().retrieveFluidAmazonStyle(liquidGPos, queryingFluid);
    }

    private static void reduceStockLevel(AmadronOffer offer, int units, boolean isAmadronRestock) {
        // Reduce stock here; if the order fails (e.g. player takes items out of the chest before the Amadron can get them),
        // we'll restore the stock level, in EventHandlerAmadron#onAmadronFailure().
        if (!isAmadronRestock && (offer instanceof AmadronPlayerOffer || offer.getMaxStock() >= 0)) {
            offer.setStock(offer.getStock() - units);
            if (offer instanceof AmadronPlayerOffer) AmadronPlayerOffers.save();
            NetworkHandler.sendNonLocal(new PacketAmadronStockUpdate(offer.getOfferId(), offer.getStock()));
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean validateStockLevel(String playerName, AmadronOffer offer, int units, boolean isAmadronRestock) {
        if (!isAmadronRestock && offer.getStock() >= 0 && units > offer.getStock()) {
            // shouldn't happen normally, but could as result of a player trying to spoof the system
            // by bypassing stock checks in the Amadron GUI (e.g. hacked client)
            // https://github.com/TeamPneumatic/pnc-repressurized/issues/736
            Log.warning("ignoring suspicious order from player [{}] for {} x {} - only {} in stock right now!",
                    playerName, units, offer, offer.getStock());
            return false;
        }
        return true;
    }

    public int getShoppingBasketUnits(ResourceLocation offerId) {
        return shoppingBasket.getUnits(offerId);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide && player.getItemInHand(hand).getItem() == ModItems.AMADRON_TABLET.get()) {
            AmadronTabletItem.saveShoppingCart(player.getItemInHand(hand), shoppingBasket);
        }
    }

}
