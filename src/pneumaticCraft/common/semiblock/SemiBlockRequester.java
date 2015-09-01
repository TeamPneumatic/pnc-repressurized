package pneumaticCraft.common.semiblock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.util.IOHelper;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.ModIds;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnection;
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
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.Optional.Interface;

@Optional.InterfaceList({@Interface(iface = "appeng.api.networking.IGridHost", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.IGridBlock", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.crafting.ICraftingProvider", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.crafting.ICraftingWatcherHost", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.storage.IStackWatcherHost", modid = ModIds.AE2), @Interface(iface = "pneumaticCraft.common.semiblock.MEInventoryExtension", modid = ModIds.AE2), @Interface(iface = "appeng.api.storage.ICellContainer", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.ticking.IGridTickable", modid = ModIds.AE2)})
public class SemiBlockRequester extends SemiBlockLogistics implements ISpecificRequester, IProvidingInventoryListener,
        IGridHost, IGridBlock, ICraftingProvider, ICraftingWatcherHost, IStackWatcherHost, ICellContainer,
        IGridTickable{

    public static final String ID = "logisticFrameRequester";

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
    public int getColor(){
        return 0xFF0000FF;
    }

    @Override
    public int amountRequested(ItemStack stack){
        int totalRequestingAmount = getTotalRequestedAmount(stack);
        if(totalRequestingAmount > 0) {
            IInventory inv = IOHelper.getInventoryForTE(getTileEntity());
            int count = 0;
            if(inv != null) {
                for(int i = 0; i < inv.getSizeInventory(); i++) {
                    ItemStack s = inv.getStackInSlot(i);
                    if(s != null && isItemEqual(s, stack)) {
                        count += s.stackSize;
                    }
                }
                count += getIncomingItems(stack);
                int requested = Math.max(0, Math.min(stack.stackSize, totalRequestingAmount - count));
                return requested;
            }
        }
        return 0;
    }

    private int getTotalRequestedAmount(ItemStack stack){
        int requesting = 0;
        for(int i = 0; i < getFilters().getSizeInventory(); i++) {
            ItemStack requestingStack = getFilters().getStackInSlot(i);
            if(requestingStack != null && isItemEqual(stack, requestingStack)) {
                requesting += requestingStack.stackSize;
            }
        }
        return requesting;
    }

    @Override
    public int amountRequested(FluidStack stack){
        int totalRequestingAmount = getTotalRequestedAmount(stack);
        if(totalRequestingAmount > 0) {
            TileEntity te = getTileEntity();
            if(te instanceof IFluidHandler) {

                int count = 0;

                for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                    FluidTankInfo[] infos = ((IFluidHandler)te).getTankInfo(d);
                    if(infos != null) {
                        for(FluidTankInfo info : infos) {
                            if(info.fluid != null && info.fluid.getFluid() == stack.getFluid()) {
                                count += info.fluid.amount;
                            }
                        }
                        if(count > 0) break;
                    }
                }

                count += getIncomingFluid(stack.getFluid());
                int requested = Math.max(0, Math.min(stack.amount, totalRequestingAmount - count));
                return requested;
            }

        }
        return 0;
    }

    private int getTotalRequestedAmount(FluidStack stack){
        int requesting = 0;
        for(int i = 0; i < 9; i++) {
            FluidStack requestingStack = getTankFilter(i).getFluid();
            if(requestingStack != null && requestingStack.getFluid() == stack.getFluid()) {
                requesting += requestingStack.amount;
            }
        }
        return requesting;
    }

    @Override
    public int getPriority(){
        return 3;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.LOGISTICS_REQUESTER;
    }

    @Override
    public boolean canFilterStack(){
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
                if(Loader.isModLoaded(ModIds.AE2) && !world.isRemote && aeMode && gridNode == null) {
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
        return getTileEntity() != null && AEApi.instance().definitions().blocks().iface().maybeBlock().get() == getTileEntity().getBlockType();
    }

    @Optional.Method(modid = ModIds.AE2)
    private boolean checkForInterface(){
        if(isPlacedOnInterface()) {
            TileEntity te = getTileEntity();
            if(te instanceof IGridHost) {
                if(((IGridHost)te).getGridNode(null) == null) return true;
                if(getGridNode(null) == null) return true;
                try {
                    AEApi.instance().createGridConnection(getGridNode(null), ((IGridHost)te).getGridNode(null));
                } catch(FailedConnection e) {
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
    public AECableType getCableConnectionType(ForgeDirection arg0){
        return AECableType.NONE;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IGridNode getGridNode(ForgeDirection d){
        if(gridNode == null) {
            gridNode = AEApi.instance().createGridNode(this);
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
    public EnumSet<ForgeDirection> getConnectableSides(){
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
        return AEColor.Transparent;
    }

    @Override
    public double getIdlePowerUsage(){
        return 1;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public DimensionalCoord getLocation(){
        return new DimensionalCoord(world, getX(), getY(), getZ());
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IGridHost getMachine(){
        return this;
    }

    @Override
    public ItemStack getMachineRepresentation(){
        return new ItemStack(Itemss.logisticsFrameRequester);
    }

    @Override
    public void gridChanged(){}

    @Override
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
        ItemStack stack = aeStack.getItemStack().copy();
        int freeSlot = -1;
        for(int i = 0; i < getFilters().getSizeInventory(); i++) {
            ItemStack s = getFilters().getStackInSlot(i);
            if(s != null) {
                if(stack.isItemEqual(s)) {
                    s.stackSize = stack.stackSize;
                    if(s.stackSize == 0) getFilters().setInventorySlotContents(i, null);
                    return;
                }
            } else if(freeSlot == -1) {
                freeSlot = i;
            }
        }
        if(freeSlot >= 0) {
            getFilters().setInventorySlotContents(freeSlot, stack.copy());
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
    public void onStackChange(IItemList arg0, IAEStack arg1, IAEStack arg2, BaseActionSource arg3, StorageChannel arg4){
        if(craftingGrid != null) {
            ICraftingGrid grid = (ICraftingGrid)craftingGrid;
            for(int i = 0; i < getFilters().getSizeInventory(); i++) {
                ItemStack s = getFilters().getStackInSlot(i);
                if(s != null) {
                    if(!grid.isRequesting(AEApi.instance().storage().createItemStack(s))) {
                        getFilters().setInventorySlotContents(i, null);
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
        if(sWatcher != null) sWatcher.clear();
        if(cWatcher != null) cWatcher.clear();
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
            IInventory inv = IOHelper.getInventoryForTE(te);
            if(inv != null) {
                for(int i = 0; i < inv.getSizeInventory(); i++) {
                    ItemStack stack = inv.getStackInSlot(i);
                    if(stack != null) stacks.add(AEApi.instance().storage().createItemStack(stack));
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
    public List<IMEInventoryHandler> getCellArray(StorageChannel channel){
        if(channel == StorageChannel.ITEMS) {
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

}
