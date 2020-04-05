package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.living.EntityAmadrone;
import me.desht.pneumaticcraft.common.entity.living.EntityAmadrone.AmadronAction;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeRemoved;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
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
import javax.annotation.Nullable;
import java.util.*;

public class ContainerAmadron extends ContainerPneumaticBase<TileEntityBase> {
    public static final int ROWS = 4;
    public static final int OFFERS_PER_PAGE = ROWS * 2;

    public final List<AmadronOffer> activeOffers = new ArrayList<>(AmadronOfferManager.getInstance().getActiveOffers());
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
            LazyOptional<IItemHandler> itemCap = ItemAmadronTablet.getItemCapability(player.getHeldItem(hand));
            LazyOptional<IFluidHandler> fluidCap = ItemAmadronTablet.getFluidCapability(player.getHeldItem(hand));
            for (int i = 0; i < activeOffers.size(); i++) {
                int amount = capShoppingAmount(activeOffers.get(i), 1, itemCap, fluidCap);
                buyableOffers[i] = amount > 0;
                liveIndex.put(activeOffers.get(i).getOfferId(), i);
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
                            shoppingAmounts[index] = count;
                        }
                    }
                });
            }

            basketEmpty = Arrays.stream(shoppingAmounts).noneMatch(shoppingAmount -> shoppingAmount > 0);
            currentOffers = AmadronOfferManager.getInstance().countOffers(player.getGameProfile().getId().toString());
            maxOffers = PneumaticCraftUtils.isPlayerOp(player) ? Integer.MAX_VALUE : PNCConfig.Common.Amadron.maxTradesPerPlayer;
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
        if (player.getHeldItem(hand).getItem() == ModItems.AMADRON_TABLET.get()) {
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

    /**
     * Run server-side to handle an offer being clicked.
     * @param offerId numeric offer id; an index into the live offers list
     * @param mouseButton mouse button
     * @param shiftPressed true if shift-clicked
     * @param player the player
     */
    public void clickOffer(int offerId, int mouseButton, boolean shiftPressed, PlayerEntity player) {
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
                    // sneak-right-click
                    AmadronOffer offer = activeOffers.get(offerId);
                    if (offer instanceof AmadronPlayerOffer) {
                        removeCustomOffer(player, (AmadronPlayerOffer) offer);
                    } else {
                        shoppingAmounts[cartSlot] *= 2;
                        if (shoppingAmounts[cartSlot] == 0) shoppingAmounts[cartSlot] = 1;
                    }
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
        }
        basketEmpty = Arrays.stream(shoppingAmounts).noneMatch(shoppingAmount -> shoppingAmount > 0);
    }

    private void removeCustomOffer(PlayerEntity player, AmadronPlayerOffer offer) {
        if (offer.getPlayerId().equals(player.getGameProfile().getId().toString())) {
            if (AmadronOfferManager.getInstance().removePlayerOffer(offer)) {
                if (PNCConfig.Common.Amadron.notifyOfTradeRemoval) {
                    NetworkHandler.sendToAll(new PacketAmadronTradeRemoved(offer));
                }
                offer.returnStock();
                player.closeScreen();
            }
        }
    }

    @Override
    public void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player) {
        super.handleGUIButtonPress(tag, shiftHeld, player);
        if (tag.equals("order")) {
            boolean placed = false;
            for (int i = 0; i < shoppingItems.length; i++) {
                if (shoppingItems[i] >= 0) {
                    AmadronOffer offer = activeOffers.get(shoppingItems[i]);
                    GlobalPos itemGPos = ItemAmadronTablet.getItemProvidingLocation(player.getHeldItem(hand));
                    GlobalPos fluidGPos = ItemAmadronTablet.getFluidProvidingLocation(player.getHeldItem(hand));
                    EntityAmadrone drone = retrieveOrderItems(offer, shoppingAmounts[i], itemGPos, fluidGPos);
                    if (drone != null) {
                        drone.setHandlingOffer(offer.getOfferId(), shoppingAmounts[i], player.getHeldItem(hand), player.getName().getFormattedText(), AmadronAction.TAKING_PAYMENT);
                        placed = true;
                    }
                }
                if (placed && player instanceof ServerPlayerEntity) {
                    NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.CHIRP.get(), SoundCategory.PLAYERS, player.getPosX(), player.getPosY(), player.getPosZ(), 0.2f, 1.0f, false), (ServerPlayerEntity) player);
                }
            }
            Arrays.fill(shoppingAmounts, 0);
            Arrays.fill(shoppingItems, -1);
            basketEmpty = true;
        } else if (tag.equals("addPlayerTrade")) {
            openTradeGui(player);
        }
    }

    private void openTradeGui(PlayerEntity player) {
        NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new StringTextComponent("");
            }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return new ContainerAmadronAddTrade(windowId, playerInventory);
            }
        });
    }

    public static EntityAmadrone retrieveOrderItems(AmadronOffer offer, int times, GlobalPos itemGPos, GlobalPos liquidGPos) {
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
            return (EntityAmadrone) DroneRegistry.getInstance().retrieveItemsAmazonStyle(itemGPos, stacks.toArray(new ItemStack[0]));
        } else if (offer.getInput().getType() == AmadronTradeResource.Type.FLUID) {
            if (liquidGPos == null) return null;
            FluidStack queryingFluid = offer.getInput().getFluid().copy();
            queryingFluid.setAmount(queryingFluid.getAmount() * times);
            return (EntityAmadrone) DroneRegistry.getInstance().retrieveFluidAmazonStyle(liquidGPos, queryingFluid);
        } else {
            return null;
        }
    }

    private int capShoppingAmount(int offerId, int wantedAmount, PlayerEntity player) {
        ItemStack tablet = player.getHeldItem(hand);
        return capShoppingAmount(activeOffers.get(offerId), wantedAmount,
                ItemAmadronTablet.getItemCapability(tablet),
                ItemAmadronTablet.getFluidCapability(tablet));
    }

    private int capShoppingAmount(AmadronOffer offer, int wantedAmount,
                                         LazyOptional<IItemHandler> itemCap,
                                         LazyOptional<IFluidHandler> fluidCap) {
        return capShoppingAmount(offer, wantedAmount, itemCap, itemCap, fluidCap, fluidCap);
    }

    private int capShoppingAmount(AmadronOffer offer, int wantedTradeCount,
                                         LazyOptional<IItemHandler> itemCapIn,
                                         LazyOptional<IItemHandler> itemCapOut,
                                         LazyOptional<IFluidHandler> fluidCapIn,
                                         LazyOptional<IFluidHandler> fluidCapOut) {
        EnumProblemState problem = null;

        if (offer.getStock() >= 0 && wantedTradeCount > offer.getStock()) {
            wantedTradeCount = offer.getStock();
            problem = EnumProblemState.OUT_OF_STOCK;
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

    public int getShoppingCartAmount(AmadronOffer offer) {
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
                    shoppingCart.put(activeOffers.get(shoppingItems[i]).getOfferId(), shoppingAmounts[i]);
                }
            }
            ItemAmadronTablet.saveShoppingCart(player.getHeldItem(hand), shoppingCart);
        }
    }
}
