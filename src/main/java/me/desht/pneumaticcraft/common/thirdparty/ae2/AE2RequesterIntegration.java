package me.desht.pneumaticcraft.common.thirdparty.ae2;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.*;
import appeng.api.networking.crafting.*;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellContainer;
import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsRequester;
import me.desht.pneumaticcraft.common.semiblock.IProvidingInventoryListener.TileEntityAndFace;
import me.desht.pneumaticcraft.common.semiblock.SemiblockTracker;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AE2RequesterIntegration implements IGridBlock, IGridHost, ICraftingProvider, ICraftingWatcherHost,
        IStackWatcherHost, ICellContainer, IGridTickable, IMEInventoryHandler<IAEItemStack>
{
    private final EntityLogisticsRequester logisticsRequester;
    private IGridNode gridNode;
    private ICraftingGrid craftingGrid;
    private IStackWatcher stackWatcher;
    private ICraftingWatcher craftingWatcher;
    private boolean needToCheckForInterface = true;
    private final Set<TileEntityAndFace> providingInventories = new HashSet<>();

    public AE2RequesterIntegration(EntityLogisticsRequester logisticsRequester) {
        this.logisticsRequester = logisticsRequester;
    }

    public void maybeAddTE(TileEntityAndFace teAndFace) {
        if (gridNode != null) {
            providingInventories.add(teAndFace);
        }
    }

    public void maybeCheckForInterface() {
        if (needToCheckForInterface) {
            if (logisticsRequester.isAE2enabled() && gridNode == null) {
                needToCheckForInterface = checkForInterface();
            } else {
                needToCheckForInterface = false;
            }
        }
    }

    public boolean isPlacedOnInterface() {
        return AE2PNCAddon.api.definitions().blocks().iface().maybeEntity()
                .map(e -> e.isInstance(logisticsRequester.getCachedTileEntity()))
                .orElse(false);
    }

    public void shutdown() {
        if (gridNode != null) {
            gridNode.destroy();
            gridNode = null;
        }
    }

    public void setEnabled(boolean ae2enabled) {
        needToCheckForInterface = ae2enabled;
        if (!ae2enabled && gridNode != null) {
            shutdown();
        }
    }

    private boolean checkForInterface() {
        if (isPlacedOnInterface()) {
            TileEntity te = logisticsRequester.getCachedTileEntity();
            if (te instanceof IGridHost) {
                if (((IGridHost) te).getGridNode(null) == null) return true;
                if (getGridNode(null) == null) return true;
                try {
                    AE2PNCAddon.api.grid().createGridConnection(((IGridHost) te).getGridNode(null), getGridNode(null));
                } catch (FailedConnectionException e) {
                    Log.error("Couldn't connect to an ME Interface!");
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public AECableType getCableConnectionType(AEPartLocation arg0) {
        return AECableType.NONE;
    }

    @Override
    public void securityBreak() {
        logisticsRequester.remove();
    }

    public IGridNode getGridNode(AEPartLocation d) {
        if (gridNode == null) {
            gridNode = AE2PNCAddon.api.grid().createGridNode(this);
        }
        return gridNode;
    }

    @Override
    public double getIdlePowerUsage() {
        return 1;
    }

    @Nonnull
    @Override
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.noneOf(GridFlags.class);
    }

    @Override
    public boolean isWorldAccessible() {
        return false;
    }

    @Nonnull
    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(logisticsRequester.getWorld(), logisticsRequester.getBlockPos());
    }

    @Nonnull
    @Override
    public AEColor getGridColor() {
        return AEColor.TRANSPARENT;
    }

    @Override
    public void onGridNotification(@Nonnull GridNotification gridNotification) {
    }

//    @Override
//    public void setNetworkStatus(IGrid iGrid, int i) {
//    }

    @Nonnull
    @Override
    public EnumSet<Direction> getConnectableSides() {
        return EnumSet.noneOf(Direction.class); //Shouldn't be called as isWorldAccessible is false.
    }

    @Nonnull
    @Override
    public IGridHost getMachine() {
        return this;
    }

    @Override
    public void gridChanged() {
    }

    @Nonnull
    @Override
    public ItemStack getMachineRepresentation() {
        return new ItemStack(ModItems.LOGISTICS_FRAME_REQUESTER.get());
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper iCraftingProviderHelper) {
        updateProvidingItems(iCraftingProviderHelper);
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails iCraftingPatternDetails, CraftingInventory craftingInventory) {
        return false;
    }

    @Override
    public boolean isBusy() {
        return true;
    }

    @Override
    public void updateWatcher(ICraftingWatcher watcher) {
        craftingWatcher = watcher;
        updateProvidingItems(null);
    }

    @Override
    public void onRequestChange(ICraftingGrid grid, IAEItemStack aeStack) {
        craftingGrid = grid;
        int freeSlot = -1;
        for (int i = 0; i < getFilters().getSlots(); i++) {
            ItemStack filterStack = getFilters().getStackInSlot(i);
            if (!filterStack.isEmpty()) {
                if (aeStack.isSameType(filterStack)) {
                    filterStack.setCount((int) grid.requesting(aeStack));
                    return;
                }
            } else if (freeSlot == -1) {
                freeSlot = i;
            }
        }
        if (freeSlot >= 0) {
            // no item in the requester frame's filter: add it!
            getFilters().setStackInSlot(freeSlot, aeStack.createItemStack());
        }
    }

    @Nullable
    @Override
    public IGridNode getActionableNode() {
        return getGridNode(null);
    }

    @Override
    public void updateWatcher(IStackWatcher watcher) {
        stackWatcher = watcher;
        updateProvidingItems(null);
    }

    @Override
    public void onStackChange(IItemList<?> iItemList, IAEStack<?> iaeStack, IAEStack<?> iaeStack1, IActionSource iActionSource, IStorageChannel<?> iStorageChannel) {
        if (craftingGrid != null) {
            for (int i = 0; i < getFilters().getSlots(); i++) {
                ItemStack s = getFilters().getStackInSlot(i);
                if (!s.isEmpty()) {
                    if (!craftingGrid.isRequesting(AE2PNCAddon.api.storage().getStorageChannel(IItemStorageChannel.class).createStack(s))) {
                        getFilters().setStackInSlot(i, ItemStack.EMPTY);
                        notifyNetworkOfCraftingChange();
                    }
                }
            }
        }
    }

    private IItemHandlerModifiable getFilters() {
        return logisticsRequester.getItemFilterHandler();
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull IGridNode iGridNode) {
        return new TickingRequest(120, 120, false, false);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull IGridNode iGridNode, int i) {
        notifyNetworkOfCraftingChange();
        if (gridNode != null) {
            // Doing it on interval, as doing it right after  AE2.api.createGridConnection doesn't seem to work..
            getGridNode(null).getGrid().postEvent(new MENetworkCellArrayUpdate());
        }
        return TickRateModulation.SAME;
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ;
    }

    @Override
    public boolean isPrioritized(IAEItemStack iaeItemStack) {
        return false;
    }

    @Override
    public boolean canAccept(IAEItemStack iaeItemStack) {
        return false;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return true;
    }

    @Override
    public IAEItemStack injectItems(IAEItemStack iaeItemStack, Actionable actionable, IActionSource iActionSource) {
        return iaeItemStack;
    }

    @Override
    public IAEItemStack extractItems(IAEItemStack iaeItemStack, Actionable actionable, IActionSource iActionSource) {
        return null;
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> iItemList) {
        for (IAEItemStack stack : getProvidingItems(true)) {
            stack.setCountRequestable(stack.getStackSize());
            iItemList.addRequestable(stack);
        }
        return iItemList;
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return AE2PNCAddon.api.storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public void blinkCell(int i) {
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(IStorageChannel<?> channel) {
        if (channel == AE2PNCAddon.api.storage().getStorageChannel(IItemStorageChannel.class)) {
            return Collections.singletonList(this);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public void saveChanges(@Nullable ICellInventory<?> iCellInventory) {
    }

    private void updateProvidingItems(ICraftingProviderHelper cHelper) {
        if (stackWatcher != null) stackWatcher.reset();
        if (craftingWatcher != null) craftingWatcher.reset();

        // watch any items that are in providing inventories
        for (IAEItemStack stack : getProvidingItems(false)) {
            if (stackWatcher != null) stackWatcher.add(stack);
            if (craftingWatcher != null) craftingWatcher.add(stack);
            if (cHelper != null) cHelper.setEmitable(stack);
        }
        // and also watch any items that are in this requester's filter
        for (int i = 0; i < logisticsRequester.getItemFilterHandler().getSlots(); i++) {
            ItemStack stack = logisticsRequester.getItemFilterHandler().getStackInSlot(i);
            if (!stack.isEmpty()) {
                IAEItemStack iaeStack = AE2PNCAddon.api.storage().getStorageChannel(IItemStorageChannel.class).createStack(stack);
                if (stackWatcher != null) stackWatcher.add(iaeStack);
                if (craftingWatcher != null) craftingWatcher.add(iaeStack);
                if (cHelper != null) cHelper.setEmitable(iaeStack);
            }
        }
    }

    private List<IAEItemStack> getProvidingItems(boolean initialList) {
        List<IAEItemStack> stacks = new ArrayList<>();
        Iterator<TileEntityAndFace> iter = providingInventories.iterator();
        while (iter.hasNext()) {
            TileEntityAndFace teFace = iter.next();
            boolean ok = false;
            if (isLogisticsTEvalid(teFace.getTileEntity())) {
                ok = IOHelper.getInventoryForTE(teFace.getTileEntity(), teFace.getFace()).map(inv -> {
                    for (int i = 0; i < inv.getSlots(); i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            IAEItemStack aeStack = AE2PNCAddon.api.storage().getStorageChannel(IItemStorageChannel.class).createStack(stack);
                            if (aeStack != null) {
                                stacks.add(initialList ? aeStack : aeStack.setStackSize(0).setCountRequestable(stack.getCount()));
                            }
                        }
                    }
                    return true;
                }).orElse(false);
            }
            if (!ok) iter.remove();
        }
        return stacks;
    }

    private boolean isLogisticsTEvalid(TileEntity te) {
        if (te.isRemoved()) return true;
        ISemiBlock frame = SemiblockTracker.getInstance().getSemiblock(logisticsRequester.getWorld(), logisticsRequester.getBlockPos());
        return frame instanceof EntityLogisticsFrame && ((EntityLogisticsFrame) frame).shouldProvideTo(logisticsRequester.getPriority());
    }

    private void notifyNetworkOfCraftingChange() {
        if (gridNode != null) {
            gridNode.getGrid().postEvent(new MENetworkCraftingPatternChange(this, gridNode));
        }
    }
}
