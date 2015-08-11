package pneumaticCraft.common.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemAmadronTablet;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.recipes.AmadronOffer;
import pneumaticCraft.common.recipes.PneumaticRecipeRegistry;
import pneumaticCraft.common.util.IOHelper;
import pneumaticCraft.proxy.CommonProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerAmadron extends ContainerPneumaticBase{
    public static final int ROWS = 4;

    public List<AmadronOffer> offers = new ArrayList<AmadronOffer>(PneumaticRecipeRegistry.getInstance().amadronOffers);

    private final InventoryBasic inv = new InventoryBasic("amadron", true, ROWS * 4);
    @GuiSynced
    private final int[] shoppingItems = new int[8];
    @GuiSynced
    private final int[] shoppingAmounts = new int[8];
    @GuiSynced
    public boolean[] buyableOffers = new boolean[offers.size()];
    @GuiSynced
    public EnumProblemState problemState = EnumProblemState.NO_PROBLEMS;

    public static enum EnumProblemState{
        NO_PROBLEMS("noProblems"), NO_ITEM_PROVIDER("noItemProvider"), NO_FLUID_PROVIDER("noFluidProvider"), NOT_ENOUGH_ITEM_SPACE(
                "notEnoughItemSpace"), NOT_ENOUGH_FLUID_SPACE("notEnoughFluidSpace"), NOT_ENOUGH_ITEMS("notEnoughItems") /*not a ChickenBones reference*/, NOT_ENOUGH_FLUID(
                "notEnoughFluid");
        private final String locKey;

        private EnumProblemState(String locKey){
            this.locKey = locKey;
        }

        public String getLocalizationKey(){
            return "gui.tab.problems.amadron." + locKey;
        }
    }

    public ContainerAmadron(EntityPlayer player){
        super(null);
        for(int y = 0; y < ROWS; y++) {
            for(int x = 0; x < 2; x++) {
                addSlotToContainer(new SlotUntouchable(inv, y * 4 + x * 2, x * 73 + 12, y * 35 + 70));
                addSlotToContainer(new SlotUntouchable(inv, y * 4 + x * 2 + 1, x * 73 + 57, y * 35 + 70));
            }
        }
        addSyncedFields(this);
        Arrays.fill(shoppingItems, -1);

        if(!player.worldObj.isRemote) {
            IInventory inv = ItemAmadronTablet.getItemProvider(player.getCurrentEquippedItem());
            IFluidHandler fluidHandler = ItemAmadronTablet.getLiquidProvider(player.getCurrentEquippedItem());
            for(int i = 0; i < offers.size(); i++) {
                int amount = capShoppingAmount(i, 1, inv, fluidHandler);
                buyableOffers[i] = amount > 0;
            }
            problemState = EnumProblemState.NO_PROBLEMS;

            Map<AmadronOffer, Integer> shoppingCart = ItemAmadronTablet.getShoppingCart(player.getCurrentEquippedItem());
            for(Map.Entry<AmadronOffer, Integer> cartItem : shoppingCart.entrySet()) {
                int offerId = offers.indexOf(cartItem.getKey());
                if(offerId >= 0) {
                    int index = getCartSlot(offerId);
                    if(index >= 0) {
                        shoppingItems[index] = offerId;
                        shoppingAmounts[index] = cartItem.getValue();
                    }
                }
            }
        }
    }

    /**
     * args: slotID, itemStack to put in slot
     */
    @Override
    public void putStackInSlot(int p_75141_1_, ItemStack p_75141_2_){

    }

    /**
     * places itemstacks in first x slots, x being aitemstack.lenght
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void putStacksInSlots(ItemStack[] p_75131_1_){

    }

    @Override
    public boolean canInteractWith(EntityPlayer player){
        if(player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Itemss.amadronTablet) {
            IPressurizable pressurizable = (IPressurizable)Itemss.amadronTablet;
            pressurizable.addAir(player.getCurrentEquippedItem(), -1);
            if(pressurizable.getPressure(player.getCurrentEquippedItem()) > 0) return true;
            else {
                player.addChatMessage(new ChatComponentTranslation("gui.tab.problems.notEnoughPressure"));
            }
        }
        return false;
    }

    public void clearStacks(){
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            inv.setInventorySlotContents(i, null);
        }
    }

    public void setStack(int index, ItemStack stack){
        inv.setInventorySlotContents(index, stack);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int p_82846_2_){
        return null;
    }

    public void clickOffer(int offerId, int mouseButton, boolean sneaking, EntityPlayer player){
        problemState = EnumProblemState.NO_PROBLEMS;
        int cartSlot = getCartSlot(offerId);
        if(cartSlot >= 0) {
            if(mouseButton == 2) {
                shoppingAmounts[cartSlot] = 0;
            } else if(sneaking) {
                if(mouseButton == 0) shoppingAmounts[cartSlot] /= 2;
                else {
                    shoppingAmounts[cartSlot] *= 2;
                    if(shoppingAmounts[cartSlot] == 0) shoppingAmounts[cartSlot] = 1;
                }
            } else {
                if(mouseButton == 0) shoppingAmounts[cartSlot]--;
                else shoppingAmounts[cartSlot]++;
            }
            if(shoppingAmounts[cartSlot] <= 0) {
                shoppingAmounts[cartSlot] = 0;
                shoppingItems[cartSlot] = -1;
            } else {
                shoppingAmounts[cartSlot] = capShoppingAmount(offerId, shoppingAmounts[cartSlot], player);
                if(shoppingAmounts[cartSlot] > 0) shoppingItems[cartSlot] = offerId;
                else shoppingItems[cartSlot] = -1;
            }
        }
    }

    @Override
    public void handleGUIButtonPress(int guiID, EntityPlayer player){
        super.handleGUIButtonPress(guiID, player);
        if(guiID == 1) {
            for(int i = 0; i < shoppingItems.length; i++) {
                if(shoppingItems[i] >= 0) {
                    AmadronOffer offer = offers.get(shoppingItems[i]);
                    if(offer.getInput() instanceof ItemStack) {
                        ItemStack queryingItems = (ItemStack)offer.getInput();
                        int amount = queryingItems.stackSize * shoppingAmounts[i];
                        List<ItemStack> stacks = new ArrayList<ItemStack>();
                        while(amount > 0) {
                            ItemStack stack = queryingItems.copy();
                            stack.stackSize = Math.min(amount, stack.getMaxStackSize());
                            stacks.add(stack);
                            amount -= stack.stackSize;
                        }
                        ChunkPosition pos = ItemAmadronTablet.getItemProvidingLocation(player.getCurrentEquippedItem());
                        World world = null;
                        if(pos == null) {
                            pos = new ChunkPosition((int)player.posX, (int)player.posY, (int)player.posZ);
                            world = player.worldObj;
                        } else {
                            world = ItemAmadronTablet.getWorldForDimension(ItemAmadronTablet.getItemProvidingDimension(player.getCurrentEquippedItem()));
                        }
                        EntityDrone drone = (EntityDrone)PneumaticRegistry.getInstance().retrieveItemsAmazonStyle(world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, stacks.toArray(new ItemStack[stacks.size()]));
                        drone.setHandlingOffer(offer, shoppingAmounts[i], player.getCurrentEquippedItem());
                    } else {
                        FluidStack queryingFluid = ((FluidStack)offer.getInput()).copy();
                        queryingFluid.amount *= shoppingAmounts[i];

                        ChunkPosition pos = ItemAmadronTablet.getLiquidProvidingLocation(player.getCurrentEquippedItem());
                        if(pos != null) {
                            World world = ItemAmadronTablet.getWorldForDimension(ItemAmadronTablet.getLiquidProvidingDimension(player.getCurrentEquippedItem()));
                            EntityDrone drone = (EntityDrone)PneumaticRegistry.getInstance().retrieveFluidAmazonStyle(world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, queryingFluid);
                            drone.setHandlingOffer(offer, shoppingAmounts[i], player.getCurrentEquippedItem());
                        }
                    }
                }
            }
            Arrays.fill(shoppingAmounts, 0);
            Arrays.fill(shoppingItems, -1);
        } else if(guiID == 2) {
            player.openGui(PneumaticCraft.instance, CommonProxy.EnumGuiId.AMADRON_ADD_TRADE.ordinal(), player.worldObj, 0, 0, 0);
        }
    }

    public int capShoppingAmount(int offerId, int wantedAmount, EntityPlayer player){
        IInventory inv = ItemAmadronTablet.getItemProvider(player.getCurrentEquippedItem());
        IFluidHandler fluidHandler = ItemAmadronTablet.getLiquidProvider(player.getCurrentEquippedItem());
        return capShoppingAmount(offerId, wantedAmount, inv, fluidHandler);
    }

    public int capShoppingAmount(int offerId, int wantedAmount, IInventory inv, IFluidHandler fluidHandler){
        AmadronOffer offer = offers.get(offerId);
        if(offer.getInput() instanceof ItemStack) {
            if(inv != null) {
                ItemStack searchingItem = (ItemStack)offer.getInput();
                int count = 0;
                for(int i = 0; i < inv.getSizeInventory(); i++) {
                    if(inv.getStackInSlot(i) != null && inv.getStackInSlot(i).isItemEqual(searchingItem) && ItemStack.areItemStackTagsEqual(inv.getStackInSlot(i), searchingItem)) {
                        count += inv.getStackInSlot(i).stackSize;
                    }
                }
                int maxAmount = count / ((ItemStack)offer.getInput()).stackSize;
                if(wantedAmount > maxAmount) {
                    problemState = EnumProblemState.NOT_ENOUGH_ITEMS;
                    wantedAmount = maxAmount;
                }
            } else {
                wantedAmount = 0;
                problemState = EnumProblemState.NO_ITEM_PROVIDER;
            }
        } else {
            if(fluidHandler != null) {
                FluidStack searchingFluid = ((FluidStack)offer.getInput()).copy();
                searchingFluid.amount = Integer.MAX_VALUE;
                FluidStack extracted = fluidHandler.drain(ForgeDirection.UP, searchingFluid, false);
                int maxAmount = 0;
                if(extracted != null) maxAmount = extracted.amount / ((FluidStack)offer.getInput()).amount;
                if(wantedAmount > maxAmount) {
                    problemState = EnumProblemState.NOT_ENOUGH_FLUID;
                    wantedAmount = maxAmount;
                }
            } else {
                wantedAmount = 0;
                problemState = EnumProblemState.NO_FLUID_PROVIDER;
            }
        }
        if(offer.getOutput() instanceof ItemStack) {
            if(inv != null) {
                ItemStack providingItem = ((ItemStack)offer.getOutput()).copy();
                providingItem.stackSize *= wantedAmount;
                ItemStack remainder = IOHelper.insert(inv, providingItem.copy(), 0, true);
                if(remainder != null) {
                    int maxAmount = (providingItem.stackSize - remainder.stackSize) / ((ItemStack)offer.getOutput()).stackSize;
                    if(wantedAmount > maxAmount) {
                        wantedAmount = maxAmount;
                        problemState = EnumProblemState.NOT_ENOUGH_ITEM_SPACE;
                    }
                }
            } else {
                wantedAmount = 0;
                problemState = EnumProblemState.NO_ITEM_PROVIDER;
            }
        } else {
            if(fluidHandler != null) {
                FluidStack providingFluid = ((FluidStack)offer.getOutput()).copy();
                providingFluid.amount *= wantedAmount;
                int amountFilled = fluidHandler.fill(ForgeDirection.UP, providingFluid, false);
                int maxAmount = amountFilled / ((FluidStack)offer.getOutput()).amount;
                if(wantedAmount > maxAmount) {
                    wantedAmount = maxAmount;
                    problemState = EnumProblemState.NOT_ENOUGH_FLUID_SPACE;
                }
            } else {
                wantedAmount = 0;
                problemState = EnumProblemState.NO_FLUID_PROVIDER;
            }
        }
        return wantedAmount;
    }

    public int getCartSlot(int offerId){
        int freeSlot = -1;
        for(int i = 0; i < shoppingItems.length; i++) {
            if(shoppingItems[i] == offerId) {
                return i;
            } else if(freeSlot == -1 && shoppingItems[i] == -1) {
                freeSlot = i;
            }
        }
        return freeSlot;
    }

    public int getShoppingCartAmount(AmadronOffer offer){
        int offerId = offers.indexOf(offer);
        for(int i = 0; i < shoppingItems.length; i++) {
            if(shoppingItems[i] == offerId) {
                return shoppingAmounts[i];
            }
        }
        return 0;
    }

    @Override
    public void onContainerClosed(EntityPlayer player){
        super.onContainerClosed(player);
        if(!player.worldObj.isRemote && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Itemss.amadronTablet) {
            Map<AmadronOffer, Integer> shoppingCart = new HashMap<AmadronOffer, Integer>();
            for(int i = 0; i < shoppingItems.length; i++) {
                if(shoppingItems[i] >= 0) {
                    shoppingCart.put(offers.get(shoppingItems[i]), shoppingAmounts[i]);
                }
            }
            ItemAmadronTablet.setShoppingCart(player.getCurrentEquippedItem(), shoppingCart);
        }
    }
}
