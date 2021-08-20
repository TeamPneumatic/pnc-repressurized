package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.config.subconfig.AmadronPlayerOffers;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.living.EntityAmadrone;
import me.desht.pneumaticcraft.common.entity.living.EntityAmadrone.AmadronAction;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.network.*;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.*;

public class ContainerAmadron extends ContainerPneumaticBase<TileEntityBase> {
    public static final int ROWS = 4;
    public static final int OFFERS_PER_PAGE = ROWS * 2;

    public final List<AmadronRecipe> activeOffers = new ArrayList<>(AmadronOfferManager.getInstance().getActiveOffers());
    private final ItemStackHandler inv = new ItemStackHandler(OFFERS_PER_PAGE * 2);
    @GuiSynced
    private final int[] shoppingItems = new int[OFFERS_PER_PAGE];
    @GuiSynced
    private final int[] shoppingAmounts = new int[OFFERS_PER_PAGE];
    @GuiSynced
    public final boolean[] buyableOffers = new boolean[activeOffers.size()];
    private final Hand hand;
    @GuiSynced
    public EnumProblemState problemState = EnumProblemState.NO_PROBLEMS;
    @GuiSynced
    public int maxOffers = 0;
    @GuiSynced
    public int currentOffers = 0;
    @GuiSynced
    private boolean basketEmpty = true;

    public enum EnumProblemState implements ITranslatableEnum {
        NO_PROBLEMS("noProblems"),
        NO_ITEM_PROVIDER("noItemProvider"),
        NO_FLUID_PROVIDER("noFluidProvider"),
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

    }
    public ContainerAmadron(int windowId, PlayerInventory invPlayer, Hand hand) {
        super(ModContainers.AMADRON.get(), windowId, invPlayer);

        this.hand = hand;

        addSyncedFields(this);

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < 2; x++) {
                addSlot(new SlotUntouchable(inv, y * 4 + x * 2, x * 73 + 12, y * 35 + 70));
                addSlot(new SlotUntouchable(inv, y * 4 + x * 2 + 1, x * 73 + 57, y * 35 + 70));
            }
        }

        Arrays.fill(shoppingItems, -1);

        PlayerEntity player = invPlayer.player;
        if (!player.world.isRemote) {
            Map<ResourceLocation, Integer> liveIndex = new HashMap<>(); // map offer id -> index into the live offer list
            for (int i = 0; i < activeOffers.size(); i++) {
                int amount = capShoppingAmount(i, 1, player);
                buyableOffers[i] = amount > 0;
                liveIndex.put(activeOffers.get(i).getId(), i);
            }
            problemState = EnumProblemState.NO_PROBLEMS;

            Map<ResourceLocation, Integer> shoppingCart = ItemAmadronTablet.loadShoppingCart(player.getHeldItem(hand));
            if (!shoppingCart.isEmpty()) {
                shoppingCart.forEach((id, count) -> {
                    if (liveIndex.containsKey(id)) {
                        int offerId = liveIndex.get(id);
                        int index = getCartSlot(offerId);
                        if (index >= 0) {
                            shoppingItems[index] = offerId;
                            shoppingAmounts[index] = capShoppingAmount(offerId, count, player);
                        }
                    }
                });
            }

            basketEmpty = Arrays.stream(shoppingAmounts).noneMatch(shoppingAmount -> shoppingAmount > 0);
            currentOffers = AmadronOfferManager.getInstance().countPlayerOffers(player.getGameProfile().getId());
            maxOffers = PneumaticCraftUtils.isPlayerOp(player) ? Integer.MAX_VALUE : PNCConfig.Common.Amadron.maxTradesPerPlayer;
        }
    }

    public ContainerAmadron(int windowId, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(windowId, playerInventory, buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }

    private void validatePurchasesCanFit() {
        int totalStacks = 0;
        int totalMb = 0;
        for (int i = 0; i < shoppingItems.length; i++) {
            if (shoppingItems[i] >= 0 && shoppingItems[i] < activeOffers.size()) {
                AmadronRecipe offer = activeOffers.get(shoppingItems[i]);
                AmadronTradeResource out = offer.getOutput();
                switch (out.getType()) {
                    case ITEM:
                        totalStacks += (((out.getAmount() * shoppingAmounts[i]) - 1) / out.getItem().getMaxStackSize()) + 1;
                        break;
                    case FLUID:
                        totalMb += out.getAmount() * shoppingAmounts[i];
                        break;
                }
            }
        }
        // an Amadrone has 35 inv upgrades, allowing 36 stacks of items and 576000mB of fluid to be carried
        if (totalStacks > 36) {
            problemState = EnumProblemState.TOO_MANY_ITEMS;
        } else if (totalMb > 576000) {
            problemState = EnumProblemState.TOO_MUCH_FLUID;
        }
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
        if (player.getHeldItem(hand).getItem() == ModItems.AMADRON_TABLET.get()) {
            return player.getHeldItem(hand).getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h -> {
                h.addAir(-1);
                if (h.getPressure() > 0) {
                    return true;
                } else {
                    player.sendStatusMessage(new TranslationTextComponent("pneumaticcraft.gui.tab.problems.notEnoughPressure"), false);
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

    /**
     * Run server-side to handle an offer being clicked.
     * @param offerId numeric offer id; an index into the live offers list
     * @param mouseButton mouse button
     * @param shiftPressed true if shift-clicked
     * @param player the player
     */
    public void clickOffer(int offerId, int mouseButton, boolean shiftPressed, ServerPlayerEntity player) {
        problemState = EnumProblemState.NO_PROBLEMS;
        int cartSlot = getCartSlot(offerId);
        if (cartSlot >= 0) {
            if (mouseButton == 2) {
                // middle-click: clear slot
                shoppingAmounts[cartSlot] = 0;
            } else if (shiftPressed) {
                if (mouseButton == 0) {
                    // sneak-left-click: halve amount
                    shoppingAmounts[cartSlot] /= 2;
                } else {
                    shoppingAmounts[cartSlot] *= 2;
                    if (shoppingAmounts[cartSlot] == 0) shoppingAmounts[cartSlot] = 1;
                }
            } else {
                // left or right-click
                if (mouseButton == 0)
                    shoppingAmounts[cartSlot]--;
                else
                    shoppingAmounts[cartSlot]++;
            }
            if (shoppingAmounts[cartSlot] <= 0) {
                shoppingAmounts[cartSlot] = 0;
                shoppingItems[cartSlot] = -1;
            } else {
                shoppingAmounts[cartSlot] = capShoppingAmount(offerId, shoppingAmounts[cartSlot], player);
                shoppingItems[cartSlot] = shoppingAmounts[cartSlot] > 0 ? offerId : -1;
            }

            validatePurchasesCanFit();
        }
        basketEmpty = Arrays.stream(shoppingAmounts).noneMatch(shoppingAmount -> shoppingAmount > 0);
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, ServerPlayerEntity player) {
        super.handleGUIButtonPress(tag, shiftHeld, player);
        if (tag.equals("order")) {
            takeOrder(player);
        } else if (tag.equals("addPlayerTrade")) {
            openTradeGui(player);
        } else if (tag.startsWith("remove:") && shiftHeld) {
            AmadronRecipe offer = AmadronOfferManager.getInstance().getOffer(new ResourceLocation(tag.substring(7)));
            if (offer instanceof AmadronPlayerOffer) {
                tryRemoveCustomOffer(player, (AmadronPlayerOffer) offer);
            }
        }
    }

    private void takeOrder(ServerPlayerEntity player) {
        boolean placed = false;
        for (int i = 0; i < shoppingItems.length; i++) {
            if (shoppingItems[i] >= 0) {
                AmadronRecipe offer = activeOffers.get(shoppingItems[i]);
                GlobalPos itemGPos = ItemAmadronTablet.getItemProvidingLocation(player.getHeldItem(hand));
                GlobalPos fluidGPos = ItemAmadronTablet.getFluidProvidingLocation(player.getHeldItem(hand));
                EntityAmadrone drone = retrieveOrderItems(player, offer, shoppingAmounts[i], itemGPos, fluidGPos);
                if (drone != null) {
                    drone.setHandlingOffer(offer.getId(), shoppingAmounts[i], player.getHeldItem(hand), player.getName().getString(), AmadronAction.TAKING_PAYMENT);
                    placed = true;
                }
            }
            if (placed) {
                NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.CHIRP.get(), SoundCategory.PLAYERS, player.getPosX(), player.getPosY(), player.getPosZ(), 0.2f, 1.0f, false), player);
            }
        }
        Arrays.fill(shoppingAmounts, 0);
        Arrays.fill(shoppingItems, -1);
        basketEmpty = true;
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

    public static EntityAmadrone retrieveOrderItems(PlayerEntity orderingPlayer, AmadronRecipe offer, int times, GlobalPos itemGPos, GlobalPos liquidGPos) {
        boolean isAmadronRestock = orderingPlayer == null;
        if (offer.getInput().getType() == AmadronTradeResource.Type.ITEM) {
            if (itemGPos == null) return null;
            if (!validateStockLevel(orderingPlayer, offer, times, isAmadronRestock)) return null;

            ItemStack queryingItems = offer.getInput().getItem();
            int amount = queryingItems.getCount() * times;
            NonNullList<ItemStack> stacks = NonNullList.create();
            while (amount > 0 && stacks.size() < 36) {
                ItemStack stack = queryingItems.copy();
                stack.setCount(Math.min(amount, stack.getMaxStackSize()));
                stacks.add(stack);
                amount -= stack.getCount();
            }
            if (stacks.isEmpty()) {
                // shouldn't happen but see https://github.com/TeamPneumatic/pnc-repressurized/issues/399
                Log.error(String.format("retrieveOrderItems: got empty itemstack list for offer %d x %s @ %s", times, queryingItems, itemGPos));
                return null;
            }

            reduceStockLevel(offer, times, isAmadronRestock);
            return (EntityAmadrone) DroneRegistry.getInstance().retrieveItemsAmazonStyle(itemGPos, stacks.toArray(new ItemStack[0]));
        } else if (offer.getInput().getType() == AmadronTradeResource.Type.FLUID) {
            if (liquidGPos == null) return null;
            if (!validateStockLevel(orderingPlayer, offer, times, isAmadronRestock)) return null;

            FluidStack queryingFluid = offer.getInput().getFluid().copy();
            queryingFluid.setAmount(queryingFluid.getAmount() * times);

            reduceStockLevel(offer, times, isAmadronRestock);
            return (EntityAmadrone) DroneRegistry.getInstance().retrieveFluidAmazonStyle(liquidGPos, queryingFluid);
        } else {
            return null;
        }
    }

    private static void reduceStockLevel(AmadronRecipe offer, int times, boolean isAmadronRestock) {
        // Reduce stock here; if the order fails (e.g. player takes items out of the chest before the Amadron can get them),
        // we'll restore the stock level, in EventHandlerAmadron#onAmadronFailure().
        if (!isAmadronRestock && (offer instanceof AmadronPlayerOffer || offer.getMaxStock() >= 0)) {
            offer.setStock(offer.getStock() - times);
            if (offer instanceof AmadronPlayerOffer) AmadronPlayerOffers.save();
            NetworkHandler.sendNonLocal(new PacketAmadronStockUpdate(offer.getId(), offer.getStock()));
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean validateStockLevel(PlayerEntity orderingPlayer, AmadronRecipe offer, int times, boolean isAmadronRestock) {
        if (!isAmadronRestock && offer.getStock() >= 0 && times > offer.getStock()) {
            // shouldn't happen normally, but could as result of a player trying to spoof the system
            // by bypassing stock checks in the Amadron GUI (hacked client)
            // https://github.com/TeamPneumatic/pnc-repressurized/issues/736
            Log.warning("ignoring suspicious order from player [%s] for %d x %s - only %d in stock right now!",
                    orderingPlayer.getName().getString(), times, offer, offer.getStock());
            return false;
        }
        return true;
    }

    private int capShoppingAmount(int offerId, int wantedAmount, PlayerEntity player) {
        ItemStack tablet = player.getHeldItem(hand);
        return capShoppingAmount(activeOffers.get(offerId), wantedAmount,
                ItemAmadronTablet.getItemCapability(tablet),
                ItemAmadronTablet.getFluidCapability(tablet));
    }

    private int capShoppingAmount(AmadronRecipe offer, int wantedAmount,
                                  LazyOptional<IItemHandler> itemCap,
                                  LazyOptional<IFluidHandler> fluidCap) {
        return capShoppingAmount(offer, wantedAmount, itemCap, itemCap, fluidCap, fluidCap);
    }

    private int capShoppingAmount(AmadronRecipe offer, int wantedTradeCount,
                                  LazyOptional<IItemHandler> itemCapIn,
                                  LazyOptional<IItemHandler> itemCapOut,
                                  LazyOptional<IFluidHandler> fluidCapIn,
                                  LazyOptional<IFluidHandler> fluidCapOut) {
        EnumProblemState problem = null;

        // check there's enough of the wanted item in stock
        if (offer.getStock() >= 0 && wantedTradeCount > offer.getStock()) {
            wantedTradeCount = offer.getStock();
            problem = offer.getStock() == 0 ? EnumProblemState.OUT_OF_STOCK : EnumProblemState.NOT_ENOUGH_STOCK;
        }

        // check there's enough of the required item/fluid in the input inventory/tank
        if (offer.getInput().getType() == AmadronTradeResource.Type.ITEM) {
            if (itemCapIn.isPresent()) {
                int availableTrades = offer.getInput().countTradesInInventory(itemCapIn);
                if (availableTrades < wantedTradeCount) {
                    problem = EnumProblemState.NOT_ENOUGH_ITEMS;
                    wantedTradeCount = availableTrades;
                }
            } else if (!itemCapOut.isPresent()) {
                wantedTradeCount = 0;
                problem = EnumProblemState.NO_ITEM_PROVIDER;
            }
        } else if (offer.getInput().getType() == AmadronTradeResource.Type.FLUID) {
            if (fluidCapIn.isPresent()) {
                int availableTrades = offer.getInput().countTradesInTank(fluidCapIn);
                if (availableTrades < wantedTradeCount) {
                    problem = EnumProblemState.NOT_ENOUGH_FLUID;
                    wantedTradeCount = availableTrades;
                }
            } else if (!fluidCapOut.isPresent()) {
                wantedTradeCount = 0;
                problem = EnumProblemState.NO_FLUID_PROVIDER;
            }
        }

        // check there's enough space for the returned item/fluid in the output inventory/tank
        if (offer.getOutput().getType() == AmadronTradeResource.Type.ITEM) {
            if (itemCapOut.isPresent()) {
                int availableSpace = offer.getOutput().findSpaceInItemOutput(itemCapOut, wantedTradeCount);
                if (availableSpace < wantedTradeCount) {
                    wantedTradeCount = availableSpace;
                    problem = EnumProblemState.NOT_ENOUGH_ITEM_SPACE;
                }
            } else if (!itemCapIn.isPresent()) {
                wantedTradeCount = 0;
                problem = EnumProblemState.NO_ITEM_PROVIDER;
            }
        } else if (offer.getOutput().getType() == AmadronTradeResource.Type.FLUID) {
            if (fluidCapOut.isPresent()) {
                int availableTrades = offer.getOutput().findSpaceInFluidOutput(fluidCapOut, wantedTradeCount);
                if (availableTrades < wantedTradeCount) {
                    wantedTradeCount = availableTrades;
                    problem = EnumProblemState.NOT_ENOUGH_FLUID_SPACE;
                }
            } else if (!fluidCapIn.isPresent()) {
                wantedTradeCount = 0;
                problem = EnumProblemState.NO_FLUID_PROVIDER;
            }
        }

        if (problem != null) problemState = problem;
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

    public int getShoppingCartAmount(AmadronRecipe offer) {
        int offerId = activeOffers.indexOf(offer);
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
        if (!player.world.isRemote && player.getHeldItem(hand).getItem() == ModItems.AMADRON_TABLET.get()) {
            Map<ResourceLocation, Integer> shoppingCart = new HashMap<>();
            for (int i = 0; i < shoppingItems.length; i++) {
                if (shoppingItems[i] >= 0) {
                    shoppingCart.put(activeOffers.get(shoppingItems[i]).getId(), shoppingAmounts[i]);
                }
            }
            ItemAmadronTablet.saveShoppingCart(player.getHeldItem(hand), shoppingCart);
        }
    }
}
