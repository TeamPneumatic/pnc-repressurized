package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;


//@Optional.InterfaceList({
//        @Interface(iface = "appeng.api.networking.IGridHost", modid = ModIds.AE2),
//        @Interface(iface = "appeng.api.networking.IGridBlock", modid = ModIds.AE2),
//        @Interface(iface = "appeng.api.networking.crafting.ICraftingProvider", modid = ModIds.AE2),
//        @Interface(iface = "appeng.api.networking.crafting.ICraftingWatcherHost", modid = ModIds.AE2),
//        @Interface(iface = "appeng.api.networking.storage.IStackWatcherHost", modid = ModIds.AE2),
//        @Interface(iface = "appeng.api.storage.ICellContainer", modid = ModIds.AE2),
//        @Interface(iface = "appeng.api.networking.ticking.IGridTickable", modid = ModIds.AE2),
//        @Interface(iface = "appeng.api.storage.IMEInventoryHandler", modid = ModIds.AE2, striprefs = true)
//})
public class SemiBlockRequester extends SemiBlockLogistics implements ISpecificRequester /*, IProvidingInventoryListener IGridHost, IGridBlock, ICraftingProvider, ICraftingWatcherHost, IStackWatcherHost, ICellContainer, IGridTickable, IMEInventoryHandler<IAEItemStack>*/ {

    public static final ResourceLocation ID = RL("logistics_frame_requester");

    @GuiSynced
    private int minItemOrderSize;
    @GuiSynced
    private int minFluidOrderSize;

    //AE2 integration
    @GuiSynced
    private boolean aeMode;
//    private Object gridNode;
//    private Object craftingGrid;
//    private Object stackWatcher;
//    private Object craftingWatcher;
//    private boolean needToCheckForInterface = true;
//    private final Set<TileEntityAndFace> providingInventories = new HashSet<>();

    @Override
    public int amountRequested(ItemStack stack) {
        final int totalRequestingAmount = getTotalRequestedAmount(stack);
        if (totalRequestingAmount > 0) {
            IOHelper.getInventoryForTE(getTileEntity(), getSide()).map(inv -> {
                int count = 0;
                for (int i = 0; i < inv.getSlots(); i++) {
                    ItemStack s = inv.getStackInSlot(i);
                    if (!s.isEmpty() && tryMatch(s, stack)) {
                        count += s.getCount();
                    }
                }
                count += getIncomingItems(stack);
                return Math.max(0, Math.min(stack.getCount(), totalRequestingAmount - count));
            }).orElse(0);
        }
        return 0;
    }

    private int getTotalRequestedAmount(ItemStack stack) {
        int requesting = 0;
        for (int i = 0; i < getFilters().getSlots(); i++) {
            ItemStack requestingStack = getFilters().getStackInSlot(i);
            if (!requestingStack.isEmpty() && tryMatch(stack, requestingStack)) {
                requesting += requestingStack.getCount();
            }
        }
        return requesting;
    }

    @Override
    public int amountRequested(FluidStack stack) {
        int totalRequestingAmount = getTotalRequestedAmount(stack);
        if (totalRequestingAmount > 0) {
            TileEntity te = getTileEntity();
            int count = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, getSide()).map(handler -> {
                int c = 0;
                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack contents = handler.getFluidInTank(i);
                    if (contents.getFluid() == stack.getFluid()) {
                        c += contents.getAmount();
                    }
                }
                return c;
            }).orElse(0);
            count += getIncomingFluid(stack.getFluid());
            return Math.max(0, Math.min(stack.getAmount(), totalRequestingAmount - count));
        }
        return 0;
    }

    private int getTotalRequestedAmount(FluidStack stack) {
        int requesting = 0;
        for (int i = 0; i < 9; i++) {
            FluidStack requestingStack = getTankFilter(i).getFluid();
            if (requestingStack != null && requestingStack.getFluid() == stack.getFluid()) {
                requesting += requestingStack.getAmount();
            }
        }
        return requesting;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public boolean canFilterStack() {
        return true;
    }

    @Override
    public boolean supportsBlacklisting() {
        return false;
    }

    public void setMinItemOrderSize(int minItems) {
        this.minItemOrderSize = minItems;
    }

    public void setMinFluidOrderSize(int minFluid) {
        this.minFluidOrderSize = minFluid;
    }

    public int getMinItemOrderSize() {
        return minItemOrderSize;
    }

    public int getMinFluidOrderSize() {
        return minFluidOrderSize;
    }

    @Override
    protected boolean shouldSaveNBT() {
        return /*aeMode ||*/ shouldWriteOrderSizeNBT() || super.shouldSaveNBT();
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
//        tag.putBoolean("aeMode", aeMode);
        tag.putInt(NBT_MIN_ITEMS, getMinItemOrderSize());
        tag.putInt(NBT_MIN_FLUID, getMinFluidOrderSize());
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
//        aeMode = tag.getBoolean("aeMode");
        setMinItemOrderSize(tag.getInt(NBT_MIN_ITEMS));
        setMinFluidOrderSize(tag.getInt(NBT_MIN_FLUID));
    }

    private boolean shouldWriteOrderSizeNBT() { return getMinFluidOrderSize() != 1 || getMinItemOrderSize() != 1; }

    /*
     ************* Applied Energistics 2 Integration **********************
     */

//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void update() {
//        super.update();
//
//        if (!world.isRemote) {
//            if (needToCheckForInterface) {
//                if (Loader.isModLoaded(ModIds.AE2) && aeMode && gridNode == null) {
//                    needToCheckForInterface = checkForInterface();
//                } else {
//                    needToCheckForInterface = false;
//                }
//            }
//        }
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void handleGUIButtonPress(int guiID, PlayerEntity player) {
//        if (guiID == 1) {
//            aeMode = !aeMode;
//            needToCheckForInterface = aeMode;
//            if (!aeMode && gridNode != null) {
//                disconnectFromInterface();
//            }
//        }
//        super.handleGUIButtonPress(guiID, player);
//    }
//
    public boolean isIntegrationEnabled() {
        return aeMode;
    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void invalidate() {
//        super.invalidate();
//        if (gridNode != null) {
//            disconnectFromInterface();
//        }
//    }
//
//    @Optional.Method(modid = ModIds.AE2)
    public boolean isPlacedOnInterface() {
        return false;
//        return AEApi.instance().definitions().blocks().iface().maybeEntity().map(e -> e.isInstance(getTileEntity())).orElse(false);
    }
//
//    @Optional.Method(modid = ModIds.AE2)
//    private boolean checkForInterface() {
//        if (isPlacedOnInterface()) {
//            TileEntity te = getTileEntity();
//            if (te instanceof IGridHost) {
//                if (((IGridHost) te).getGridNode(null) == null) return true;
//                if (getGridNode(null) == null) return true;
//                try {
//                    AEApi.instance().grid().createGridConnection(((IGridHost) te).getGridNode(null), getGridNode(null));
//                } catch (FailedConnectionException e) {
//                    Log.error("Couldn't connect to an ME Interface!");
//                    e.printStackTrace();
//                }
//            }
//        }
//        return false;
//    }
//
//    @Optional.Method(modid = ModIds.AE2)
//    private void disconnectFromInterface() {
//        ((IGridNode) gridNode).destroy();
//        gridNode = null;
//    }
//
//    //IGridHost
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public AECableType getCableConnectionType(AEPartLocation arg0) {
//        return AECableType.NONE;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public IGridNode getGridNode(AEPartLocation d) {
//        if (gridNode == null) {
//            gridNode = AEApi.instance().grid().createGridNode(this);
//        }
//        return (IGridNode) gridNode;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void securityBreak() {
//        drop();
//    }
//
//    //IGridBlock
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public EnumSet<Direction> getConnectableSides() {
//        return null;//Shouldn't be called as isWorldAccessible is false.
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public EnumSet<GridFlags> getFlags() {
//        return EnumSet.noneOf(GridFlags.class);
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public AEColor getGridColor() {
//        return AEColor.TRANSPARENT;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public double getIdlePowerUsage() {
//        return 1;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public DimensionalCoord getLocation() {
//        return new DimensionalCoord(world, getPos());
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public IGridHost getMachine() {
//        return this;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public ItemStack getMachineRepresentation() {
//        return new ItemStack(ModItems.LOGISTICS_FRAME_REQUESTER);
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void gridChanged() {
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public boolean isWorldAccessible() {
//        return false;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void onGridNotification(GridNotification arg0) {
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void setNetworkStatus(IGrid arg0, int arg1) {
//    }
//
//    //ICraftingProvider
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public boolean isBusy() {
//        return true;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public boolean pushPattern(ICraftingPatternDetails details, CraftingInventory inventoryCrafting) {
//        return false;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void provideCrafting(ICraftingProviderHelper helper) {
//        updateProvidingItems(helper);
//    }
//
//    //ICraftingWatcherHost
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void onRequestChange(ICraftingGrid grid, IAEItemStack aeStack) {
//        craftingGrid = grid;
//        int freeSlot = -1;
//        for (int i = 0; i < getFilters().getSlots(); i++) {
//            ItemStack filterStack = getFilters().getStackInSlot(i);
//            if (!filterStack.isEmpty()) {
//                if (aeStack.isSameType(filterStack)) {
//                    filterStack.setCount((int) grid.requesting(aeStack));
//                    return;
//                }
//            } else if (freeSlot == -1) {
//                freeSlot = i;
//            }
//        }
//        if (freeSlot >= 0) {
//            // no item in the requester frame's filter: add it!
//            getFilters().setStackInSlot(freeSlot, aeStack.createItemStack());
//        }
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void updateWatcher(ICraftingWatcher watcher) {
//        craftingWatcher = watcher;
//        updateProvidingItems();
//    }
//
//    //IStackWatcherHost
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void onStackChange(IItemList arg0, IAEStack arg1, IAEStack arg2, IActionSource arg3, IStorageChannel arg4) {
//        if (craftingGrid != null) {
//            ICraftingGrid grid = (ICraftingGrid) craftingGrid;
//            for (int i = 0; i < getFilters().getSlots(); i++) {
//                ItemStack s = getFilters().getStackInSlot(i);
//                if (!s.isEmpty()) {
//                    if (!grid.isRequesting(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(s))) {
//                        getFilters().setStackInSlot(i, ItemStack.EMPTY);
//                        notifyNetworkOfCraftingChange();
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void updateWatcher(IStackWatcher watcher) {
//        stackWatcher = watcher;
//        updateProvidingItems();
//    }
//
//    @Optional.Method(modid = ModIds.AE2)
//    private void updateProvidingItems() {
//        updateProvidingItems(null);
//    }
//
//    @Optional.Method(modid = ModIds.AE2)
//    private void notifyNetworkOfCraftingChange() {
//        if (gridNode != null) {
//            IGrid grid = ((IGridNode) gridNode).getGrid();
//            if (grid != null) grid.postEvent(new MENetworkCraftingPatternChange(this, (IGridNode) gridNode));
//        }
//    }
//
//    @Optional.Method(modid = ModIds.AE2)
//    private void updateProvidingItems(ICraftingProviderHelper cHelper) {
//        IStackWatcher sWatcher = (IStackWatcher) stackWatcher;
//        ICraftingWatcher cWatcher = (ICraftingWatcher) craftingWatcher;
//        if (sWatcher != null) sWatcher.reset();
//        if (cWatcher != null) cWatcher.reset();
//        // watch any items that are in providing inventories
//        for (IAEItemStack stack : getProvidingItems()) {
//            if (sWatcher != null) sWatcher.add(stack);
//            if (cWatcher != null) cWatcher.add(stack);
//            if (cHelper != null) cHelper.setEmitable(stack);
//        }
//        // and also watch any items that are in this requester's filter
//        for (int i = 0; i < getFilters().getSlots(); i++) {
//            ItemStack stack = getFilters().getStackInSlot(i);
//            if (!stack.isEmpty()) {
//                IAEItemStack iaeStack = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stack);
//                if (sWatcher != null) sWatcher.add(iaeStack);
//                if (cWatcher != null) cWatcher.add(iaeStack);
//                if (cHelper != null) cHelper.setEmitable(iaeStack);
//            }
//        }
//    }
//
//    @Override
//    public void notify(TileEntityAndFace teAndFace) {
//        if (gridNode != null) providingInventories.add(teAndFace);
//    }
//
//    @Optional.Method(modid = ModIds.AE2)
//    private List<IAEItemStack> getProvidingItems() {
//        List<IAEItemStack> stacks = new ArrayList<>();
//        Iterator<TileEntityAndFace> iter = providingInventories.iterator();
//        while (iter.hasNext()) {
//            TileEntityAndFace teFace = iter.next();
//            if (isLogisticsTEInvalid(teFace.getTileEntity())) {
//                iter.remove();
//            } else {
//                IItemHandler inv = IOHelper.getInventoryForTE(teFace.getTileEntity(), teFace.getFace());
//                if (inv != null) {
//                    for (int i = 0; i < inv.getSlots(); i++) {
//                        ItemStack stack = inv.getStackInSlot(i);
//                        if (!stack.isEmpty())
//                            stacks.add(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stack));
//                    }
//                } else {
//                    // should be covered up by the isLogisticsTEInvalid() call above but just in case...
//                    iter.remove();
//                }
//            }
//        }
//        return stacks;
//    }
//
//    private boolean isLogisticsTEInvalid(TileEntity te) {
//        if (te.isInvalid()) return true;
//        SemiBlockLogistics sb = SemiBlockManager.getInstance(world).getSemiBlock(SemiBlockLogistics.class, world, te.getPos());
//        return sb == null || !sb.shouldProvideTo(this.getPriority());
//    }

    //ICellContainer

//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public IGridNode getActionableNode() {
//        return getGridNode(null);
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public List<IMEInventoryHandler> getCellArray(IStorageChannel channel) {
//        if (channel == AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
//            return Collections.singletonList(this);
//        } else {
//            return new ArrayList<>();
//        }
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void saveChanges(@Nullable ICellInventory<?> cellInventory) {
//
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public void blinkCell(int arg0) {
//    }
//
//    //IGridTickable
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public TickingRequest getTickingRequest(IGridNode node) {
//        return new TickingRequest(120, 120, false, false);
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public TickRateModulation tickingRequest(IGridNode arg0, int arg1) {
//        notifyNetworkOfCraftingChange();
//        if (gridNode != null) {
//            // Doing it on interval, as doing it right after  AEApi.instance().createGridConnection doesn't seem to work..
//            getGridNode(null).getGrid().postEvent(new MENetworkCellArrayUpdate());
//        }
//        return TickRateModulation.SAME;
//    }
//
//    //IMEInventoryHandler
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public IAEItemStack extractItems(IAEItemStack arg0, Actionable arg1, IActionSource arg2) {
//        return null;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> arg0) {
//        for (IAEItemStack stack : getProvidingItems()) {
//            stack.setCountRequestable(stack.getStackSize());
//            arg0.addRequestable(stack);
//        }
//        return arg0;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public IStorageChannel getChannel() {
//        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public IAEItemStack injectItems(IAEItemStack arg0, Actionable arg1, IActionSource arg2) {
//        return arg0;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public boolean canAccept(IAEItemStack arg0) {
//        return false;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public AccessRestriction getAccess() {
//        return AccessRestriction.READ;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public int getSlot() {
//        return 0;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public boolean isPrioritized(IAEItemStack arg0) {
//        return false;
//    }
//
//    @Override
//    @Optional.Method(modid = ModIds.AE2)
//    public boolean validForPass(int arg0) {
//        return true;
//    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return null;
    }
}
