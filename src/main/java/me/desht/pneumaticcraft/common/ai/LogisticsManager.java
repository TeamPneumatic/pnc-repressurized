package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.semiblock.IProvidingInventoryListener;
import me.desht.pneumaticcraft.common.semiblock.ISpecificProvider;
import me.desht.pneumaticcraft.common.semiblock.ISpecificRequester;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics.FluidStackWrapper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class LogisticsManager {

    private final List<SemiBlockLogistics>[] logistics = new List[4];

    public LogisticsManager() {
        for (int i = 0; i < logistics.length; i++) {
            logistics[i] = new ArrayList<>();
        }
    }

    void clearLogistics() {
        for (List<SemiBlockLogistics> list : logistics) {
            list.clear();
        }
    }

    public void addLogisticFrame(SemiBlockLogistics frame) {
        logistics[frame.getPriority()].add(frame);
    }

    public PriorityQueue<LogisticsTask> getTasks(Object holdingStack) {
        ItemStack item = holdingStack instanceof ItemStack ? (ItemStack) holdingStack : null;
        FluidStack fluid = holdingStack instanceof FluidStack ? (FluidStack) holdingStack : null;
        PriorityQueue<LogisticsTask> tasks = new PriorityQueue<LogisticsTask>();
        for (int priority = logistics.length - 1; priority >= 0; priority--) {
            for (SemiBlockLogistics requester : logistics[priority]) {
                for (int i = 0; i < priority; i++) {
                    for (SemiBlockLogistics provider : logistics[i]) {
                        if (provider.shouldProvideTo(priority)) {
                            if (item != null) {
                                int requestedAmount = getRequestedAmount(requester, item);
                                if (requestedAmount > 0) {
                                    ItemStack stack = item.copy();
                                    stack.setCount(requestedAmount);
                                    tasks.add(new LogisticsTask(provider, requester, stack));
                                    return tasks;
                                }
                            } else if (fluid != null) {
                                int requestedAmount = getRequestedAmount(requester, fluid);
                                if (requestedAmount > 0) {
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

    private void tryProvide(SemiBlockLogistics provider, SemiBlockLogistics requester, PriorityQueue<LogisticsTask> tasks) {
        IItemHandler providingInventory = IOHelper.getInventoryForTE(provider.getTileEntity());
        if (providingInventory != null) {
            if (requester instanceof IProvidingInventoryListener)
                ((IProvidingInventoryListener) requester).notify(provider.getTileEntity());
            for (int i = 0; i < providingInventory.getSlots(); i++) {
                ItemStack providingStack = providingInventory.getStackInSlot(i);
                if (!providingStack.isEmpty() && (!(provider instanceof ISpecificProvider) || ((ISpecificProvider) provider).canProvide(providingStack))) {
                    int requestedAmount = getRequestedAmount(requester, providingStack);
                    if (requestedAmount > 0) {
                        ItemStack stack = providingStack.copy();
                        stack.setCount(requestedAmount);
                        tasks.add(new LogisticsTask(provider, requester, stack));
                    }
                }
            }
        }

        for (EnumFacing d : EnumFacing.VALUES) {
            if (provider.getTileEntity().hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, d)) {
                IFluidHandler handler = provider.getTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, d);
                FluidStack providingStack = handler.drain(16000, false);
                boolean canDrain = Arrays.stream(handler.getTankProperties()).anyMatch(p -> p.canDrainFluidType(providingStack));
                if (providingStack != null &&
                        (!(provider instanceof ISpecificProvider) || ((ISpecificProvider) provider).canProvide(providingStack)) && canDrain) {
                    int requestedAmount = getRequestedAmount(requester, providingStack);
                    if (requestedAmount > 0) {
                        FluidStack stack = providingStack.copy();
                        stack.amount = requestedAmount;
                        tasks.add(new LogisticsTask(provider, requester, new FluidStackWrapper(stack)));
                    }
                }
            }
        }
    }

    public static int getRequestedAmount(SemiBlockLogistics requester, ItemStack providingStack) {
        TileEntity te = requester.getTileEntity();
        if (te == null) return 0;

        int requestedAmount = requester instanceof ISpecificRequester ? ((ISpecificRequester) requester).amountRequested(providingStack) : providingStack.getCount();
        if (requestedAmount == 0) return 0;
        providingStack = providingStack.copy();
        providingStack.setCount(requestedAmount);
        ItemStack remainder = providingStack.copy();
        remainder.grow(requester.getIncomingItems(providingStack));
        for (EnumFacing d : EnumFacing.VALUES) {
            remainder = IOHelper.insert(te, remainder, d, true);
            if (remainder.isEmpty()) break;
        }
        providingStack.shrink(remainder.getCount());
        if (providingStack.getCount() <= 0) return 0;
        return providingStack.getCount();
    }

    public static int getRequestedAmount(SemiBlockLogistics requester, FluidStack providingStack) {
        int requestedAmount = requester instanceof ISpecificRequester ? ((ISpecificRequester) requester).amountRequested(providingStack) : providingStack.amount;
        if (requestedAmount == 0) return 0;
        providingStack = providingStack.copy();
        providingStack.amount = requestedAmount;
        FluidStack remainder = providingStack.copy();
        remainder.amount += requester.getIncomingFluid(remainder.getFluid());
        TileEntity te = requester.getTileEntity();
        for (EnumFacing d : EnumFacing.VALUES) {
            if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, d)) {
                IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, d);
                int fluidFilled = handler.fill(remainder, false);
                if (fluidFilled > 0) {
                    remainder.amount -= fluidFilled;
                    break;
                }
            }
        }
        providingStack.amount -= remainder.amount;
        if (providingStack.amount <= 0) return 0;
        return providingStack.amount;
    }

    public static class LogisticsTask implements Comparable<LogisticsTask> {

        public final SemiBlockLogistics provider, requester;
        public final ItemStack transportingItem;
        public final FluidStackWrapper transportingFluid;

        public LogisticsTask(SemiBlockLogistics provider, SemiBlockLogistics requester, ItemStack transportingItem) {
            this.provider = provider;
            this.requester = requester;
            this.transportingItem = transportingItem;
            transportingFluid = null;
        }

        public LogisticsTask(SemiBlockLogistics provider, SemiBlockLogistics requester,
                             FluidStackWrapper transportingFluid) {
            this.provider = provider;
            this.requester = requester;
            this.transportingFluid = transportingFluid;
            transportingItem = ItemStack.EMPTY;
        }

        public void informRequester() {
            if (!transportingItem.isEmpty()) {
                requester.informIncomingStack(transportingItem);
            } else {
                requester.informIncomingStack(transportingFluid);
            }
        }

        public boolean isStillValid(Object stack) {
            if (!transportingItem.isEmpty() && stack instanceof ItemStack) {
                int requestedAmount = getRequestedAmount(requester, (ItemStack) stack);
                return requestedAmount == ((ItemStack) stack).getCount();
            } else if (transportingFluid != null && stack instanceof FluidStack) {
                int requestedAmount = getRequestedAmount(requester, (FluidStack) stack);
                return requestedAmount == ((FluidStack) stack).amount;
            } else {
                return false;
            }
        }

        @Override
        public int compareTo(LogisticsTask task) {
            int value = !transportingItem.isEmpty() ? transportingItem.getCount() * 100 : transportingFluid.stack.amount;
            int otherValue = !task.transportingItem.isEmpty() ? task.transportingItem.getCount() * 100 : task.transportingFluid.stack.amount;
            return otherValue - value;
        }

    }
}
