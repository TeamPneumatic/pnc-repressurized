package me.desht.pneumaticcraft.common.semiblock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherHost;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.items.IItemHandler;


@Optional.InterfaceList({@Interface(iface = "appeng.api.networking.IGridHost", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.IGridBlock", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.crafting.ICraftingProvider", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.crafting.ICraftingWatcherHost", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.storage.IStackWatcherHost", modid = ModIds.AE2), @Interface(iface = "pneumaticCraft.common.semiblock.MEInventoryExtension", modid = ModIds.AE2), @Interface(iface = "appeng.api.storage.ICellContainer", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.ticking.IGridTickable", modid = ModIds.AE2), @Interface(iface = "appeng.api.storage.IMEInventoryHandler", modid = ModIds.AE2, striprefs = true)})
public class SemiBlockRequester extends SemiBlockLogistics implements ISpecificRequester, IProvidingInventoryListener, IGridHost, IGridBlock, ICraftingProvider, ICraftingWatcherHost, IStackWatcherHost, ICellContainer, IGridTickable, IMEInventoryHandler<IAEItemStack> {

    public static final String ID = "logistic_frame_requester";

    //AE2 integration
    @GuiSynced
    private boolean aeMode;
    private Object gridNode;
    private Object craftingGrid;
    private Object stackWatcher;
    private Object craftingWatcher;
    private boolean needToCheckForInterface = true;
    private final Map<TileEntity, Integer> providingInventories = new HashMap<TileEntity, Integer>();

    @Override
    public int getColor() {
        return 0xFF0000FF;
    }

    @Override
    public int amountRequested(ItemStack stack) {
        int totalRequestingAmount = getTotalRequestedAmount(stack);
        if (totalRequestingAmount > 0) {
            IItemHandler inv = IOHelper.getInventoryForTE(getTileEntity());
            int count = 0;
            if (inv != null) {
                for (int i = 0; i < inv.getSlots(); i++) {
                    ItemStack s = inv.getStackInSlot(i);
                    if (!s.isEmpty() && isItemEqual(s, stack)) {
                        count += s.getCount();
                    }
                }
                count += getIncomingItems(stack);
                return Math.max(0, Math.min(stack.getCount(), totalRequestingAmount - count));
            }
        }
        return 0;
    }

    private int getTotalRequestedAmount(ItemStack stack) {
        int requesting = 0;
        for (int i = 0; i < getFilters().getSlots(); i++) {
            ItemStack requestingStack = getFilters().getStackInSlot(i);
            if (!requestingStack.isEmpty() && isItemEqual(stack, requestingStack)) {
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
            int count = 0;
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
                    IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
                    for (IFluidTankProperties properties : handler.getTankProperties()) {
                        FluidStack contents = properties.getContents();
                        if (contents != null && contents.getFluid() == stack.getFluid()) {
                            count += contents.amount;
                        }
                    }
                    if (count > 0) break;
                }
            }
            if (count == 0) return 0;
            count += getIncomingFluid(stack.getFluid());
            return Math.max(0, Math.min(stack.amount, totalRequestingAmount - count));
        }
        return 0;
    }

    private int getTotalRequestedAmount(FluidStack stack) {
        int requesting = 0;
        for (int i = 0; i < 9; i++) {
            FluidStack requestingStack = getTankFilter(i).getFluid();
            if (requestingStack != null && requestingStack.getFluid() == stack.getFluid()) {
                requesting += requestingStack.amount;
            }
        }
        return requesting;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.LOGISTICS_REQUESTER;
    }

    @Override
    public boolean canFilterStack() {
        return true;
    }

    /*
     ****************************************** Applied Energistics 2 Integration ***************************************************************
     */

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void update(){
        super.update();

        if(!world.isRemote) {
            if(needToCheckForInterface) {
                if(Loader.isModLoaded(ModIds.AE2) && aeMode && gridNode == null) {
                    needToCheckForInterface = checkForInterface();
                } else {
                	needToCheckForInterface = false;
                }
            }

            Iterator<Map.Entry<TileEntity, Integer>> iterator = providingInventories.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<TileEntity, Integer> entry = iterator.next();
                if(entry.getValue() == 0 || entry.getKey().isInvalid()) {
                    iterator.remove();
                } else {
                    entry.setValue(entry.getValue() - 1);
                }
            }
        }
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void handleGUIButtonPress(int guiID, EntityPlayer player){
        if(guiID == 1) {
            aeMode = !aeMode;
            needToCheckForInterface = aeMode;
            if(!aeMode && gridNode != null) {
                disconnectFromInterface();
            }
        }
        super.handleGUIButtonPress(guiID, player);
    }

    public boolean isIntegrationEnabled(){
        return aeMode;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("aeMode", aeMode);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        aeMode = tag.getBoolean("aeMode");
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void invalidate(){
        super.invalidate();
        if(gridNode != null) {
            disconnectFromInterface();
        }
    }

    @Optional.Method(modid = ModIds.AE2)
    public boolean isPlacedOnInterface(){
        return AEApi.instance().definitions().blocks().iface().maybeEntity().map(e -> e.isInstance(getTileEntity())).orElse(false);
    }

    @Optional.Method(modid = ModIds.AE2)
    private boolean checkForInterface(){
        if(isPlacedOnInterface()) {
            TileEntity te = getTileEntity();
            if(te instanceof IGridHost) {
                if(((IGridHost)te).getGridNode(null) == null) return true;
                if(getGridNode(null) == null) return true;
                try {
                    AEApi.instance().grid().createGridConnection(((IGridHost)te).getGridNode(null), getGridNode(null));
                } catch(FailedConnectionException e) {
                    Log.error("Couldn't connect to an ME Interface!");
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Optional.Method(modid = ModIds.AE2)
    private void disconnectFromInterface(){
        ((IGridNode)gridNode).destroy();
        gridNode = null;
    }

    //IGridHost
    @Override
    @Optional.Method(modid = ModIds.AE2)
    public AECableType getCableConnectionType(AEPartLocation arg0){
        return AECableType.NONE;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IGridNode getGridNode(AEPartLocation d){
        if(gridNode == null) {
            gridNode = AEApi.instance().grid().createGridNode(this);
        }
        return (IGridNode)gridNode;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void securityBreak(){
        drop();
    }

    //IGridBlock
    @Override
    @Optional.Method(modid = ModIds.AE2)
    public EnumSet<EnumFacing> getConnectableSides(){
        return null;//Shouldn't be called as isWorldAccessible is false.
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public EnumSet<GridFlags> getFlags(){
        return EnumSet.noneOf(GridFlags.class);
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public AEColor getGridColor(){
        return AEColor.TRANSPARENT;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public double getIdlePowerUsage(){
        return 1;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public DimensionalCoord getLocation(){
        return new DimensionalCoord(world, getPos());
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IGridHost getMachine(){
        return this;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public ItemStack getMachineRepresentation(){
        return new ItemStack(Itemss.LOGISTICS_FRAME_REQUESTER);
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void gridChanged(){}

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public boolean isWorldAccessible(){
        return false;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void onGridNotification(GridNotification arg0){}

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void setNetworkStatus(IGrid arg0, int arg1){}

    //ICraftingProvider

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public boolean isBusy(){
        return true;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting inventoryCrafting){
        return false;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void provideCrafting(ICraftingProviderHelper helper){
        updateProvidingItems(helper);
    }

    //ICraftingWatcherHost

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void onRequestChange(ICraftingGrid grid, IAEItemStack aeStack){
        craftingGrid = grid;
        ItemStack stack = aeStack.createItemStack();
        int freeSlot = -1;
        for(int i = 0; i < getFilters().getSlots(); i++) {
            ItemStack s = getFilters().getStackInSlot(i);
            if(!s.isEmpty()) {
                if(stack.isItemEqual(s)) {
                    s.setCount( stack.getCount() );
                    if(s.getCount() == 0) getFilters().setStackInSlot(i, ItemStack.EMPTY);
                    return;
                }
            } else if(freeSlot == -1) {
                freeSlot = i;
            }
        }
        if(freeSlot >= 0) {
            getFilters().setStackInSlot(freeSlot, stack.copy());
        }
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void updateWatcher(ICraftingWatcher watcher){
        craftingWatcher = watcher;
        updateProvidingItems();
    }

    //IStackWatcherHost
    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void onStackChange(IItemList arg0, IAEStack arg1, IAEStack arg2, IActionSource arg3, IStorageChannel arg4){
        if(craftingGrid != null) {
            ICraftingGrid grid = (ICraftingGrid)craftingGrid;
            for(int i = 0; i < getFilters().getSlots(); i++) {
                ItemStack s = getFilters().getStackInSlot(i);
                if(!s.isEmpty()) {
                    if(!grid.isRequesting(AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createStack(s))) {
                        getFilters().setStackInSlot(i, ItemStack.EMPTY);
                        notifyNetworkOfCraftingChange();
                    }
                }
            }
        }
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void updateWatcher(IStackWatcher watcher){
        stackWatcher = watcher;
        updateProvidingItems();
    }

    @Optional.Method(modid = ModIds.AE2)
    private void updateProvidingItems(){
        updateProvidingItems(null);
    }

    @Optional.Method(modid = ModIds.AE2)
    private void notifyNetworkOfCraftingChange(){
        if(gridNode != null) {
            IGrid grid = ((IGridNode)gridNode).getGrid();
            if(grid != null) grid.postEvent(new MENetworkCraftingPatternChange(this, (IGridNode)gridNode));
        }
    }

    @Optional.Method(modid = ModIds.AE2)
    private void updateProvidingItems(ICraftingProviderHelper cHelper){
        IStackWatcher sWatcher = (IStackWatcher)stackWatcher;
        ICraftingWatcher cWatcher = (ICraftingWatcher)craftingWatcher;
        if(sWatcher != null) sWatcher.reset();
        if(cWatcher != null) cWatcher.reset();
        for(IAEItemStack stack : getProvidingItems()) {
            if(sWatcher != null) sWatcher.add(stack);
            if(cWatcher != null) cWatcher.add(stack);
            if(cHelper != null) cHelper.setEmitable(stack);
        }
    }

    @Override
    public void notify(TileEntity te){
        if(gridNode != null) providingInventories.put(te, 40);
    }

    @Optional.Method(modid = ModIds.AE2)
    public List<IAEItemStack> getProvidingItems(){
        List<IAEItemStack> stacks = new ArrayList<IAEItemStack>();
        for(TileEntity te : providingInventories.keySet()) {
            IItemHandler inv = IOHelper.getInventoryForTE(te);
            if(inv != null) {
                for(int i = 0; i < inv.getSlots(); i++) {
                    ItemStack stack = inv.getStackInSlot(i);
                    if(!stack.isEmpty()) stacks.add(AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createStack(stack));
                }
            }
        }
        return stacks;
    }

    //ICellContainer

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IGridNode getActionableNode(){
        return getGridNode(null);
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public List<IMEInventoryHandler> getCellArray(IStorageChannel channel){
        if(channel == AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class )) {
            return Arrays.asList((IMEInventoryHandler)this);
        } else {
            return new ArrayList<IMEInventoryHandler>();
        }
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void saveChanges(IMEInventory arg0){
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public void blinkCell(int arg0){
    }

    //IGridTickable
    @Override
    @Optional.Method(modid = ModIds.AE2)
    public TickingRequest getTickingRequest(IGridNode node){
        return new TickingRequest(120, 120, false, false);
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public TickRateModulation tickingRequest(IGridNode arg0, int arg1){
        notifyNetworkOfCraftingChange();
        if(gridNode != null) {
            getGridNode(null).getGrid().postEvent(new MENetworkCellArrayUpdate());//Doing it on interval, as doing it right after  AEApi.instance().createGridConnection doesn't seem to work..
        }
        return TickRateModulation.SAME;
    }

    //IMEInventoryHandler

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IAEItemStack extractItems(IAEItemStack arg0, Actionable arg1, IActionSource arg2){
        return null;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> arg0){
        for(IAEItemStack stack : getProvidingItems()) {
            stack.setCountRequestable(stack.getStackSize());
            arg0.addRequestable(stack);
        }
        return arg0;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IStorageChannel getChannel(){
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IAEItemStack injectItems(IAEItemStack arg0, Actionable arg1, IActionSource arg2){
        return arg0;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public boolean canAccept(IAEItemStack arg0){
        return false;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public AccessRestriction getAccess(){
        return AccessRestriction.READ;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public int getSlot(){
        return 0;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public boolean isPrioritized(IAEItemStack arg0){
        return false;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public boolean validForPass(int arg0){
        return true;
    }
}
