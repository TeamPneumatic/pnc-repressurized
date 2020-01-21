package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsRequester;
import me.desht.pneumaticcraft.common.semiblock.IProvidingInventoryListener;
import me.desht.pneumaticcraft.common.semiblock.IProvidingInventoryListener.TileEntityAndFace;
import me.desht.pneumaticcraft.common.semiblock.ISpecificProvider;
import me.desht.pneumaticcraft.common.semiblock.ISpecificRequester;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.IntStream;

public class LogisticsManager {
    private static final int N_PRIORITIES = 4;

    private final List<List<EntityLogisticsFrame>> logistics = new ArrayList<>();

    public LogisticsManager() {
        for (int i = 0; i < N_PRIORITIES; i++) {
            logistics.add(new ArrayList<>());
        }
    }

    public void addLogisticFrame(EntityLogisticsFrame frame) {
        logistics.get(frame.getPriority()).add(frame);
    }

    public PriorityQueue<LogisticsTask> getTasks(Object holdingStack) {
        ItemStack item = holdingStack instanceof ItemStack ? (ItemStack) holdingStack : ItemStack.EMPTY;
        FluidStack fluid = holdingStack instanceof FluidStack ? (FluidStack) holdingStack : FluidStack.EMPTY;
        PriorityQueue<LogisticsTask> tasks = new PriorityQueue<>();
        for (int priority = logistics.size() - 1; priority >= 0; priority--) {
            for (EntityLogisticsFrame requester : logistics.get(priority)) {
                for (int i = 0; i < priority; i++) {
                    for (EntityLogisticsFrame provider : logistics.get(i)) {
                        if (provider.shouldProvideTo(priority)) {
                            if (!item.isEmpty()) {
                                int requestedAmount = getRequestedAmount(requester, item);
                                if (requestedAmount > 0) {
                                    ItemStack stack = item.copy();
                                    stack.setCount(requestedAmount);
                                    tasks.add(new LogisticsTask(provider, requester, stack));
                                    return tasks;
                                }
                            } else if (!fluid.isEmpty()) {
                                int requestedAmount = getRequestedAmount(requester, fluid);
                                if (requestedAmount > 0) {
                                    fluid = fluid.copy();
                                    fluid.setAmount(requestedAmount);
                                    tasks.add(new LogisticsTask(provider, requester, fluid));
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

    private void tryProvide(EntityLogisticsFrame provider, EntityLogisticsFrame requester, PriorityQueue<LogisticsTask> tasks) {
        if (provider.getCachedTileEntity() == null) return;

        IOHelper.getInventoryForTE(provider.getCachedTileEntity(), provider.getFacing()).ifPresent(providingInventory -> {
            if (requester instanceof IProvidingInventoryListener)
                ((IProvidingInventoryListener) requester).notify(new TileEntityAndFace(provider.getCachedTileEntity(), provider.getFacing()));
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
        });

        provider.getCachedTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, provider.getFacing()).ifPresent(fluidHandler -> {
            FluidStack providingStack = fluidHandler.drain(16000, IFluidHandler.FluidAction.SIMULATE);
            if (!providingStack.isEmpty()) {
                boolean canDrain = IntStream.range(0, fluidHandler.getTanks()).anyMatch(i -> fluidHandler.isFluidValid(i, providingStack));
                if (canDrain &&
                        (!(provider instanceof ISpecificProvider) || ((ISpecificProvider) provider).canProvide(providingStack))) {
                    int requestedAmount = getRequestedAmount(requester, providingStack);
                    if (requestedAmount > 0) {
                        FluidStack stack = providingStack.copy();
                        stack.setAmount(requestedAmount);
                        tasks.add(new LogisticsTask(provider, requester, stack));
                    }
                }
            }
        });
    }

    private static int getRequestedAmount(EntityLogisticsFrame requester, ItemStack providingStack) {
        TileEntity te = requester.getCachedTileEntity();
        if (te == null) return 0;

        int requestedAmount = requester instanceof ISpecificRequester ? ((ISpecificRequester) requester).amountRequested(providingStack) : providingStack.getMaxStackSize();
        int minOrderSize = requester instanceof EntityLogisticsRequester ? ((EntityLogisticsRequester) requester).getMinItemOrderSize() : 1;
        if (requestedAmount < minOrderSize) return 0;
        providingStack = providingStack.copy();
        providingStack.setCount(requestedAmount);
        ItemStack remainder = providingStack.copy();
        remainder.grow(requester.getIncomingItems(providingStack));
        remainder = IOHelper.insert(te, remainder, requester.getFacing(), true);
        providingStack.shrink(remainder.getCount());
        if (providingStack.getCount() <= 0) return 0;
        return providingStack.getCount();
    }

    private static int getRequestedAmount(EntityLogisticsFrame requester, FluidStack providingStack) {
        int requestedAmount = requester instanceof ISpecificRequester ? ((ISpecificRequester) requester).amountRequested(providingStack) : providingStack.getAmount();
        int minOrderSize = requester instanceof EntityLogisticsRequester ? ((EntityLogisticsRequester) requester).getMinFluidOrderSize() : 1;
        if (requestedAmount < minOrderSize) return 0;
        providingStack = providingStack.copy();
        providingStack.setAmount(requestedAmount);
        FluidStack remainder = providingStack.copy();
        remainder.grow(requester.getIncomingFluid(remainder.getFluid()));
        if (requester.getCachedTileEntity() == null) return 0;
        requester.getCachedTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, requester.getFacing()).ifPresent(fluidHandler -> {
            int fluidFilled = fluidHandler.fill(remainder, IFluidHandler.FluidAction.SIMULATE);
            if (fluidFilled > 0) {
                remainder.shrink(fluidFilled);
            }
        });
        providingStack.shrink(remainder.getAmount());
        return providingStack.getAmount();
    }

    public static class LogisticsTask implements Comparable<LogisticsTask> {
        public final EntityLogisticsFrame provider, requester;
        public final ItemStack transportingItem;
        public final FluidStack transportingFluid;

        LogisticsTask(EntityLogisticsFrame provider, EntityLogisticsFrame requester, @Nonnull ItemStack transportingItem) {
            this.provider = provider;
            this.requester = requester;
            this.transportingItem = transportingItem;
            this.transportingFluid = FluidStack.EMPTY;
        }

        LogisticsTask(EntityLogisticsFrame provider, EntityLogisticsFrame requester,
                      FluidStack transportingFluid) {
            this.provider = provider;
            this.requester = requester;
            this.transportingItem = ItemStack.EMPTY;
            this.transportingFluid = transportingFluid;
        }

        void informRequester() {
            if (!transportingItem.isEmpty()) {
                requester.informIncomingStack(transportingItem);
            } else {
                requester.informIncomingStack(transportingFluid);
            }
        }

        public boolean isStillValid(Object stack) {
            if (stack instanceof ItemStack) {
                if (!transportingItem.isEmpty()) {
                    int requestedAmount = getRequestedAmount(requester, (ItemStack) stack);
                    return requestedAmount == ((ItemStack) stack).getCount();
                }
            } else if (stack instanceof FluidStack) {
                if (!transportingFluid.isEmpty()) {
                    int requestedAmount = getRequestedAmount(requester, (FluidStack) stack);
                    return requestedAmount == ((FluidStack) stack).getAmount();
                }
            } else {
                throw new IllegalArgumentException("arg must be ItemStack or FluidStack! " + stack);
            }
            return false;
        }

        @Override
        public int compareTo(LogisticsTask task) {
            int value = !transportingItem.isEmpty() ? transportingItem.getCount() * 100 : transportingFluid.getAmount();
            int otherValue = !task.transportingItem.isEmpty() ? task.transportingItem.getCount() * 100 : task.transportingFluid.getAmount();
            return otherValue - value;
        }

    }
}
