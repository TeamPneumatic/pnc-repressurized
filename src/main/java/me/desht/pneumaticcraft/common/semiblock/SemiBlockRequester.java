package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.network.GuiSynced;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;

import java.util.HashMap;
import java.util.Map;

//TODO AE2 dep @Optional.InterfaceList({@Interface(iface = "appeng.api.networking.IGridHost", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.IGridBlock", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.crafting.ICraftingProvider", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.crafting.ICraftingWatcherHost", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.storage.IStackWatcherHost", modid = ModIds.AE2), @Interface(iface = "pneumaticCraft.common.semiblock.MEInventoryExtension", modid = ModIds.AE2), @Interface(iface = "appeng.api.storage.ICellContainer", modid = ModIds.AE2), @Interface(iface = "appeng.api.networking.ticking.IGridTickable", modid = ModIds.AE2)})
public class SemiBlockRequester extends SemiBlockLogistics implements ISpecificRequester/*, IProvidingInventoryListener,
IGridHost, IGridBlock, ICraftingProvider, ICraftingWatcherHost, IStackWatcherHost, ICellContainer,
IGridTickable*/ {

    public static final String ID = "logistic_frame_requester";

    //AE2 integration
    @GuiSynced
    private boolean aeMode;
    private Object gridNode;
    private Object craftingGrid;
    private Object stackWatcher;
    private Object craftingWatcher;
    private final boolean needToCheckForInterface = true;
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

    /*  @Override
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
      public AECableType getCableConnectionType(EnumFacing arg0){
          return AECableType.NONE;
      }

      @Override
      @Optional.Method(modid = ModIds.AE2)
      public IGridNode getGridNode(EnumFacing d){
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
      }*/

}
