/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.semiblock.IProvidingInventoryListener;
import me.desht.pneumaticcraft.common.semiblock.IProvidingInventoryListener.TileEntityAndFace;
import me.desht.pneumaticcraft.common.semiblock.ISpecificProvider;
import me.desht.pneumaticcraft.common.semiblock.ISpecificRequester;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

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

    public PriorityQueue<LogisticsTask> getTasks(Object holdingStack, boolean droneAccess) {
        ItemStack item = holdingStack instanceof ItemStack ? (ItemStack) holdingStack : ItemStack.EMPTY;
        FluidStack fluid = holdingStack instanceof FluidStack ? (FluidStack) holdingStack : FluidStack.EMPTY;
        PriorityQueue<LogisticsTask> tasks = new PriorityQueue<>();
        for (int priority = logistics.size() - 1; priority >= 0; priority--) {
            for (EntityLogisticsFrame requester : logistics.get(priority)) {
                if (droneAccess && requester.isObstructed(PathComputationType.AIR)) continue;
                for (int i = 0; i < priority; i++) {
                    for (EntityLogisticsFrame provider : logistics.get(i)) {
                        if (droneAccess && provider.isObstructed(PathComputationType.AIR)) continue;
                        if (provider.shouldProvideTo(priority)) {
                            if (!item.isEmpty()) {
                                int requestedAmount = getRequestedAmount(requester, item, false);
                                if (requestedAmount > 0) {
                                    ItemStack stack = item.copy();
                                    stack.setCount(requestedAmount);
                                    tasks.add(new LogisticsTask(provider, requester, stack));
                                    return tasks;
                                }
                            } else if (!fluid.isEmpty()) {
                                int requestedAmount = getRequestedAmount(requester, fluid, false);
                                if (requestedAmount > 0) {
                                    fluid = fluid.copy();
                                    fluid.setAmount(requestedAmount);
                                    tasks.add(new LogisticsTask(provider, requester, fluid));
                                    return tasks;
                                }
                            }
                            // it could be that the drone is carrying some item or fluid it can't drop off right now
                            // however it might still be able to transfer the other resource type (i.e. transfer items if
                            // it's holding a fluid, and vice versa)
                            tryProvide(provider, requester, tasks, item.isEmpty(), fluid.isEmpty());
                        }
                    }
                }
            }
        }
        return tasks;
    }

    private void tryProvide(EntityLogisticsFrame provider, EntityLogisticsFrame requester, PriorityQueue<LogisticsTask> tasks, boolean tryItems, boolean tryFluids) {
        if (provider.getCachedTileEntity() == null) return;

        if (tryItems) {
            provider.getCachedTileEntity().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, provider.getSide()).ifPresent(itemHandler -> {
                if (requester instanceof IProvidingInventoryListener)
                    ((IProvidingInventoryListener) requester).notify(new TileEntityAndFace(provider.getCachedTileEntity(), provider.getSide()));
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack providingStack = itemHandler.extractItem(i, 64, true);
                    if (!providingStack.isEmpty() && (!(provider instanceof ISpecificProvider) || ((ISpecificProvider) provider).canProvide(providingStack))) {
                        int requestedAmount = getRequestedAmount(requester, providingStack, true);
                        if (requestedAmount > 0) {
                            ItemStack stack = providingStack.copy();
                            stack.setCount(requestedAmount);
                            tasks.add(new LogisticsTask(provider, requester, stack));
                        }
                    }
                }
            });
        }

        if (tryFluids) {
            provider.getCachedTileEntity().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, provider.getSide()).ifPresent(fluidHandler -> {
                FluidStack providingStack = fluidHandler.drain(16000, IFluidHandler.FluidAction.SIMULATE);
                if (!providingStack.isEmpty()) {
                    boolean canDrain = IntStream.range(0, fluidHandler.getTanks()).anyMatch(i -> fluidHandler.isFluidValid(i, providingStack));
                    if (canDrain &&
                            (!(provider instanceof ISpecificProvider) || ((ISpecificProvider) provider).canProvide(providingStack))) {
                        int requestedAmount = getRequestedAmount(requester, providingStack, true);
                        if (requestedAmount > 0) {
                            FluidStack stack = providingStack.copy();
                            stack.setAmount(requestedAmount);
                            tasks.add(new LogisticsTask(provider, requester, stack));
                        }
                    }
                }
            });
        }
    }

    private static int getRequestedAmount(EntityLogisticsFrame requester, ItemStack providingStack, boolean honourMin) {
        BlockEntity te = requester.getCachedTileEntity();
        if (te == null) return 0;

        int requestedAmount = requester instanceof ISpecificRequester ? ((ISpecificRequester) requester).amountRequested(providingStack) : providingStack.getMaxStackSize();
        int minOrderSize = honourMin && requester instanceof ISpecificRequester ? ((ISpecificRequester) requester).getMinItemOrderSize() : 1;

        if (requestedAmount < minOrderSize) return 0;
        providingStack = providingStack.copy();
        if (requestedAmount < providingStack.getCount()) providingStack.setCount(requestedAmount);
        ItemStack remainder = providingStack.copy();
        remainder.grow(requester.getIncomingItems(providingStack));
        remainder = IOHelper.insert(te, remainder, requester.getSide(), true);
        providingStack.shrink(remainder.getCount());
        return providingStack.getCount() < minOrderSize ? 0 : Math.max(providingStack.getCount(), 0);
    }

    private static int getRequestedAmount(EntityLogisticsFrame requester, FluidStack providingStack, boolean honourMin) {
        BlockEntity te = requester.getCachedTileEntity();
        if (te == null) return 0;

        int requestedAmount = requester instanceof ISpecificRequester ? ((ISpecificRequester) requester).amountRequested(providingStack) : providingStack.getAmount();
        int minOrderSize = honourMin && requester instanceof ISpecificRequester ? ((ISpecificRequester) requester).getMinFluidOrderSize() : 1;

        if (requestedAmount < minOrderSize) return 0;
        providingStack = providingStack.copy();
        if (requestedAmount < providingStack.getAmount()) providingStack.setAmount(requestedAmount);
        FluidStack remainder = providingStack.copy();
        remainder.grow(requester.getIncomingFluid(remainder.getFluid()));
        te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, requester.getSide()).ifPresent(fluidHandler -> {
            int fluidFilled = fluidHandler.fill(remainder, IFluidHandler.FluidAction.SIMULATE);
            if (fluidFilled > 0) {
                remainder.shrink(fluidFilled);
            }
        });
        providingStack.shrink(remainder.getAmount());
        return providingStack.getAmount() < minOrderSize ? 0 : providingStack.getAmount();
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
                    int requestedAmount = getRequestedAmount(requester, (ItemStack) stack, false);
                    return requestedAmount == ((ItemStack) stack).getCount();
                }
            } else if (stack instanceof FluidStack) {
                if (!transportingFluid.isEmpty()) {
                    int requestedAmount = getRequestedAmount(requester, (FluidStack) stack, false);
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
