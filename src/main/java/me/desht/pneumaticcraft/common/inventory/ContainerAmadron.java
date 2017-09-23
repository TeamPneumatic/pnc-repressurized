package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.DroneRegistry;
import me.desht.pneumaticcraft.common.config.AmadronOfferSettings;
import me.desht.pneumaticcraft.common.config.AmadronOfferStaticConfig;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeRemoved;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.proxy.CommonProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

public class ContainerAmadron extends ContainerPneumaticBase {
    public static final int ROWS = 4;

    public List<AmadronOffer> offers = new ArrayList<>(AmadronOfferManager.getInstance().getAllOffers());

    private final ItemStackHandler inv = new ItemStackHandler(ROWS * 4);
//    private final InventoryBasic inv = new InventoryBasic("amadron", true, ROWS * 4);
    @GuiSynced
    private final int[] shoppingItems = new int[8];
    @GuiSynced
    private final int[] shoppingAmounts = new int[8];
    @GuiSynced
    public boolean[] buyableOffers = new boolean[offers.size()];
    @GuiSynced
    public EnumProblemState problemState = EnumProblemState.NO_PROBLEMS;
    @GuiSynced
    public int maxOffers = 0;
    @GuiSynced
    public int currentOffers = 0;

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

        public String getLocalizationKey() {
            return "gui.tab.problems.amadron." + locKey;
        }
    }

    public ContainerAmadron(EntityPlayer player) {
        super(null);
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < 2; x++) {
                addSlotToContainer(new SlotUntouchable(inv, y * 4 + x * 2, x * 73 + 12, y * 35 + 70));
                addSlotToContainer(new SlotUntouchable(inv, y * 4 + x * 2 + 1, x * 73 + 57, y * 35 + 70));
            }
        }
        addSyncedFields(this);
        Arrays.fill(shoppingItems, -1);

        if (!player.world.isRemote) {
            IItemHandler itemHandler = ItemAmadronTablet.getItemProvider(player.getHeldItemMainhand());
            IFluidHandler fluidHandler = ItemAmadronTablet.getLiquidProvider(player.getHeldItemMainhand());
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
            currentOffers = AmadronOfferManager.getInstance().countOffers(player.getGameProfile().getId().toString());
            maxOffers = PneumaticCraftUtils.isPlayerOp(player) ? Integer.MAX_VALUE : AmadronOfferSettings.maxTradesPerPlayer;
        }
    }

    @Override
    public void putStackInSlot(int slot, @Nonnull ItemStack stack) {
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        if (player.getHeldItemMainhand().getItem() == Itemss.AMADRON_TABLET) {
            IPressurizable pressurizable = (IPressurizable) Itemss.AMADRON_TABLET;
            pressurizable.addAir(player.getHeldItemMainhand(), -1);
            if (pressurizable.getPressure(player.getHeldItemMainhand()) > 0) {
                return true;
            } else {
                player.sendStatusMessage(new TextComponentTranslation("gui.tab.problems.notEnoughPressure"), false);
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
    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int p_82846_2_) {
        return ItemStack.EMPTY;
    }

    public void clickOffer(int offerId, int mouseButton, boolean sneaking, EntityPlayer player) {
        problemState = EnumProblemState.NO_PROBLEMS;
        int cartSlot = getCartSlot(offerId);
        if (cartSlot >= 0) {
            if (mouseButton == 2) {
                shoppingAmounts[cartSlot] = 0;
            } else if (sneaking) {
                if (mouseButton == 0) shoppingAmounts[cartSlot] /= 2;
                else {
                    AmadronOffer offer = offers.get(offerId);
                    if (offer instanceof AmadronOfferCustom) {
                        AmadronOfferCustom custom = (AmadronOfferCustom) offer;
                        if (custom.getPlayerId().equals(player.getGameProfile().getId().toString())) {
                            if (AmadronOfferManager.getInstance().removeStaticOffer(custom)) {
                                if (AmadronOfferSettings.notifyOfTradeRemoval)
                                    NetworkHandler.sendToAll(new PacketAmadronTradeRemoved(custom));
                                custom.returnStock();
                                try {
                                    AmadronOfferStaticConfig.INSTANCE.writeToFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                player.closeScreen();
                            }
                        }
                    }
                    shoppingAmounts[cartSlot] *= 2;
                    if (shoppingAmounts[cartSlot] == 0) shoppingAmounts[cartSlot] = 1;
                }
            } else {
                if (mouseButton == 0) shoppingAmounts[cartSlot]--;
                else shoppingAmounts[cartSlot]++;
            }
            if (shoppingAmounts[cartSlot] <= 0) {
                shoppingAmounts[cartSlot] = 0;
                shoppingItems[cartSlot] = -1;
            } else {
                shoppingAmounts[cartSlot] = capShoppingAmount(offerId, shoppingAmounts[cartSlot], player);
                if (shoppingAmounts[cartSlot] > 0) shoppingItems[cartSlot] = offerId;
                else shoppingItems[cartSlot] = -1;
            }
        }
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player) {
        super.handleGUIButtonPress(guiID, player);
        if (guiID == 1) {
            for (int i = 0; i < shoppingItems.length; i++) {
                if (shoppingItems[i] >= 0) {
                    AmadronOffer offer = offers.get(shoppingItems[i]);
                    BlockPos itemPos = ItemAmadronTablet.getItemProvidingLocation(player.getHeldItemMainhand());
                    World itemWorld;
                    if (itemPos == null) {
                        itemPos = new BlockPos((int) player.posX, (int) player.posY, (int) player.posZ);
                        itemWorld = player.world;
                    } else {
                        itemWorld = DimensionManager.getWorld(ItemAmadronTablet.getItemProvidingDimension(player.getHeldItemMainhand()));
                    }
                    BlockPos liquidPos = ItemAmadronTablet.getLiquidProvidingLocation(player.getHeldItemMainhand());
                    World liquidWorld = null;
                    if (liquidPos != null) {
                        liquidWorld = DimensionManager.getWorld(ItemAmadronTablet.getLiquidProvidingDimension(player.getHeldItemMainhand()));
                    }
                    EntityDrone drone = retrieveOrderItems(offer, shoppingAmounts[i], itemWorld, itemPos, liquidWorld, liquidPos);
                    if (drone != null)
                        drone.setHandlingOffer(offer, shoppingAmounts[i], player.getHeldItemMainhand(), player.getName());
                }
            }
            Arrays.fill(shoppingAmounts, 0);
            Arrays.fill(shoppingItems, -1);
        } else if (guiID == 2) {
            player.openGui(PneumaticCraftRepressurized.instance, CommonProxy.EnumGuiId.AMADRON_ADD_TRADE.ordinal(), player.world, 0, 0, 0);
        }
    }

    public static EntityDrone retrieveOrderItems(AmadronOffer offer, int times, World itemWorld, BlockPos itemPos, World liquidWorld, BlockPos liquidPos) {
        if (offer.getInput() instanceof ItemStack) {
            if (itemWorld == null || itemPos == null) return null;
            ItemStack queryingItems = (ItemStack) offer.getInput();
            int amount = queryingItems.getCount() * times;
            NonNullList<ItemStack> stacks = NonNullList.create();
            while (amount > 0) {
                ItemStack stack = queryingItems.copy();
                stack.setCount(Math.min(amount, stack.getMaxStackSize()));
                stacks.add(stack);
                amount -= stack.getCount();
            }
            return (EntityDrone) DroneRegistry.getInstance().retrieveItemsAmazonStyle(itemWorld, itemPos, stacks.toArray(new ItemStack[stacks.size()]));
        } else {
            if (liquidWorld == null || liquidPos == null) return null;
            FluidStack queryingFluid = ((FluidStack) offer.getInput()).copy();
            queryingFluid.amount *= times;
            return (EntityDrone) DroneRegistry.getInstance().retrieveFluidAmazonStyle(liquidWorld, liquidPos, queryingFluid);
        }
    }

    private int capShoppingAmount(int offerId, int wantedAmount, EntityPlayer player) {
        IItemHandler inv = ItemAmadronTablet.getItemProvider(player.getHeldItemMainhand());
        IFluidHandler fluidHandler = ItemAmadronTablet.getLiquidProvider(player.getHeldItemMainhand());
        return capShoppingAmount(offers.get(offerId), wantedAmount, inv, fluidHandler, this);
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
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.world.isRemote && player.getHeldItemMainhand().getItem() == Itemss.AMADRON_TABLET) {
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
