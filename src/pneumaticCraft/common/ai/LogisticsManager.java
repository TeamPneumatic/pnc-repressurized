package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import pneumaticCraft.common.semiblock.IProvidingInventoryListener;
import pneumaticCraft.common.semiblock.ISpecificProvider;
import pneumaticCraft.common.semiblock.ISpecificRequester;
import pneumaticCraft.common.semiblock.SemiBlockLogistics;
import pneumaticCraft.common.semiblock.SemiBlockLogistics.FluidStackWrapper;
import pneumaticCraft.common.util.IOHelper;

public class LogisticsManager{

    private final List<SemiBlockLogistics>[] logistics = new List[4];

    public LogisticsManager(){
        for(int i = 0; i < logistics.length; i++) {
            logistics[i] = new ArrayList<SemiBlockLogistics>();
        }
    }

    public void clearLogistics(){
        for(List<SemiBlockLogistics> list : logistics) {
            list.clear();
        }
    }

    public void addLogisticFrame(SemiBlockLogistics frame){
        logistics[frame.getPriority()].add(frame);
    }

    public PriorityQueue<LogisticsTask> getTasks(Object holdingStack){
        ItemStack item = holdingStack instanceof ItemStack ? (ItemStack)holdingStack : null;
        FluidStack fluid = holdingStack instanceof FluidStack ? (FluidStack)holdingStack : null;
        PriorityQueue<LogisticsTask> tasks = new PriorityQueue<LogisticsTask>();
        for(int priority = logistics.length - 1; priority >= 0; priority--) {
            for(SemiBlockLogistics requester : logistics[priority]) {
                for(int i = 0; i < priority; i++) {
                    for(SemiBlockLogistics provider : logistics[i]) {
                        if(provider.shouldProvideTo(priority)) {
                            if(item != null) {
                                int requestedAmount = getRequestedAmount(requester, item);
                                if(requestedAmount > 0) {
                                    ItemStack stack = item.copy();
                                    stack.stackSize = requestedAmount;
                                    tasks.add(new LogisticsTask(provider, requester, stack));
                                    return tasks;
                                }
                            } else if(fluid != null) {
                                int requestedAmount = getRequestedAmount(requester, fluid);
                                if(requestedAmount > 0) {
                                    fluid = fluid.copy();
                                    fluid.amount = requestedAmount;
                                    tasks.add(new LogisticsTask(provider, requester, new FluidStackWrapper(fluid)));
                                    return tasks;
                                }
                            } else {
                                tryProvide(provider, requester, tasks);
                            }
                        }
                    }
                }
            }
        }
        return tasks;
    }

    //curAI = new DroneAILiquidImport(drone, new FakeWidgetLogistics(provider.getPos(), providingStack));
    // transportingFluid = new FluidStackWrapper(providingStack);

    private void tryProvide(SemiBlockLogistics provider, SemiBlockLogistics requester, PriorityQueue<LogisticsTask> tasks){
        IInventory providingInventory = IOHelper.getInventoryForTE(provider.getTileEntity());
        if(providingInventory != null) {
            if(requester instanceof IProvidingInventoryListener) ((IProvidingInventoryListener)requester).notify(provider.getTileEntity());
            for(int i = 0; i < providingInventory.getSizeInventory(); i++) {
                ItemStack providingStack = providingInventory.getStackInSlot(i);
                if(providingStack != null && (!(provider instanceof ISpecificProvider) || ((ISpecificProvider)provider).canProvide(providingStack)) && IOHelper.canExtractItemFromInventory(providingInventory, providingStack, i, 0)) {
                    int requestedAmount = getRequestedAmount(requester, providingStack);
                    if(requestedAmount > 0) {
                        ItemStack stack = providingStack.copy();
                        stack.stackSize = requestedAmount;
                        tasks.add(new LogisticsTask(provider, requester, stack));
                    }
                }
            }
        }
        if(provider.getTileEntity() instanceof IFluidHandler) {
            IFluidHandler providingTank = (IFluidHandler)provider.getTileEntity();
            for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                FluidStack providingStack = providingTank.drain(d, 16000, false);
                if(providingStack != null && (!(provider instanceof ISpecificProvider) || ((ISpecificProvider)provider).canProvide(providingStack)) && providingTank.canDrain(d, providingStack.getFluid())) {
                    int requestedAmount = getRequestedAmount(requester, providingStack);
                    if(requestedAmount > 0) {
                        FluidStack stack = providingStack.copy();
                        stack.amount = requestedAmount;
                        tasks.add(new LogisticsTask(provider, requester, new FluidStackWrapper(stack)));
                    }
                }
            }
        }
    }

    public static int getRequestedAmount(SemiBlockLogistics requester, ItemStack providingStack){
        TileEntity te = requester.getTileEntity();
        if(!(te instanceof IInventory)) return 0;
        int requestedAmount = requester instanceof ISpecificRequester ? ((ISpecificRequester)requester).amountRequested(providingStack) : providingStack.stackSize;
        if(requestedAmount == 0) return 0;
        providingStack = providingStack.copy();
        providingStack.stackSize = requestedAmount;
        ItemStack remainder = providingStack.copy();
        remainder.stackSize += requester.getIncomingItems(providingStack);
        for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            remainder = IOHelper.insert(te, remainder, d, true);
            if(remainder == null) break;
        }
        if(remainder != null) providingStack.stackSize -= remainder.stackSize;
        if(providingStack.stackSize <= 0) return 0;
        return providingStack.stackSize;
    }

    public static int getRequestedAmount(SemiBlockLogistics requester, FluidStack providingStack){
        int requestedAmount = requester instanceof ISpecificRequester ? ((ISpecificRequester)requester).amountRequested(providingStack) : providingStack.amount;
        if(requestedAmount == 0) return 0;
        providingStack = providingStack.copy();
        providingStack.amount = requestedAmount;
        FluidStack remainder = providingStack.copy();
        remainder.amount += requester.getIncomingFluid(remainder.getFluid());
        TileEntity te = requester.getTileEntity();
        if(te instanceof IFluidHandler) {
            IFluidHandler fluidHandler = (IFluidHandler)te;
            for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                int fluidFilled = fluidHandler.fill(d, remainder, false);
                if(fluidFilled > 0) {
                    remainder.amount -= fluidFilled;
                    break;
                }
            }
        }
        providingStack.amount -= remainder.amount;
        if(providingStack.amount <= 0) return 0;
        return providingStack.amount;
    }

    public static class LogisticsTask implements Comparable<LogisticsTask>{

        public final SemiBlockLogistics provider, requester;
        public final ItemStack transportingItem;
        public final FluidStackWrapper transportingFluid;

        public LogisticsTask(SemiBlockLogistics provider, SemiBlockLogistics requester, ItemStack transportingItem){
            this.provider = provider;
            this.requester = requester;
            this.transportingItem = transportingItem;
            transportingFluid = null;
        }

        public LogisticsTask(SemiBlockLogistics provider, SemiBlockLogistics requester,
                FluidStackWrapper transportingFluid){
            this.provider = provider;
            this.requester = requester;
            this.transportingFluid = transportingFluid;
            transportingItem = null;
        }

        public void informRequester(){
            if(transportingItem != null) {
                requester.informIncomingStack(transportingItem);
            } else {
                requester.informIncomingStack(transportingFluid);
            }
        }

        public boolean isStillValid(Object stack){
            if(transportingItem != null && stack instanceof ItemStack) {
                int requestedAmount = getRequestedAmount(requester, (ItemStack)stack);
                return requestedAmount == ((ItemStack)stack).stackSize;
            } else if(transportingFluid != null && stack instanceof FluidStack) {
                int requestedAmount = getRequestedAmount(requester, (FluidStack)stack);
                return requestedAmount == ((FluidStack)stack).amount;
            } else {
                return false;
            }
        }

        @Override
        public int compareTo(LogisticsTask task){
            int value = transportingItem != null ? transportingItem.stackSize * 100 : transportingFluid.stack.amount;
            int otherValue = task.transportingItem != null ? task.transportingItem.stackSize * 100 : task.transportingFluid.stack.amount;
            return otherValue - value;
        }

    }
}
