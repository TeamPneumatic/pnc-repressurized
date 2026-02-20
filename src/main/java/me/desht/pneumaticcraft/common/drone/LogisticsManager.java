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

package me.desht.pneumaticcraft.common.drone;

import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractLogisticsFrameEntity;
import me.desht.pneumaticcraft.common.semiblock.IProvidingInventoryListener;
import me.desht.pneumaticcraft.common.semiblock.IProvidingInventoryListener.BlockEntityAndFace;
import me.desht.pneumaticcraft.common.semiblock.ISpecificProvider;
import me.desht.pneumaticcraft.common.semiblock.ISpecificRequester;
import me.desht.pneumaticcraft.common.util.CountedFluidStacks;
import me.desht.pneumaticcraft.common.util.CountedItemStacks;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.IntStream;

public class LogisticsManager {
    /**
     * 4 priority levels:
     * <ul>
     * <li>Active/passive provider: 0</li>
     * <li>Default Storage: 1</li>
     * <li>Storage: 2</li>
     * <li>Requester: 3</li>
     * </ul>
     */
    private static final int N_PRIORITIES = 4;

    private final List<List<AbstractLogisticsFrameEntity>> logistics = new ArrayList<>();

    private boolean droneAccess;

    public LogisticsManager() {
        for (int i = 0; i < N_PRIORITIES; i++) {
            logistics.add(new ArrayList<>());
        }
    }

    public void addLogisticFrame(AbstractLogisticsFrameEntity frame) {
        logistics.get(frame.getPriority()).add(frame);
    }

    public PriorityQueue<LogisticsTask> getTasks(Object holdingStack, boolean droneAccess) {
        this.droneAccess = droneAccess;

        ItemStack item = holdingStack instanceof ItemStack is ? is : ItemStack.EMPTY;
        FluidStack fluid = holdingStack instanceof FluidStack fs ? fs : FluidStack.EMPTY;

        PriorityQueue<LogisticsTask> tasks = new PriorityQueue<>();
        for (int priority = logistics.size() - 1; priority >= 0; priority--) {
            for (int requesterId = 0; requesterId < logistics.get(priority).size(); requesterId++) {
                AbstractLogisticsFrameEntity requester = logistics.get(priority).get(requesterId);
                if (droneAccess && requester.isObstructed(PathComputationType.AIR)) {
                    continue;
                }
                for (int i = 0; i < priority; i++) {
                    for (int providerId = 0; providerId < logistics.get(i).size(); providerId++) {
                        AbstractLogisticsFrameEntity provider = logistics.get(i).get(providerId);
                        if (droneAccess && provider.isObstructed(PathComputationType.AIR)) {
                            continue;
                        }
                        if (provider.shouldProvideTo(priority)) {
                            if (!item.isEmpty()) {
                                int requestedAmount = getRequestedAmount(requester, item, false);
                                if (requestedAmount > 0) {
                                    tasks.add(new LogisticsTask(provider, requester, item.copyWithCount(requestedAmount)));
                                    return tasks;
                                }
                            } else if (!fluid.isEmpty()) {
                                int requestedAmount = getRequestedAmount(requester, fluid, false);
                                if (requestedAmount > 0) {
                                    tasks.add(new LogisticsTask(provider, requester, fluid.copyWithAmount(requestedAmount)));
                                    return tasks;
                                }
                            }
                            // it could be that the drone is carrying some item or fluid it can't drop off right now
                            // however it might still be able to transfer the other resource type (i.e. transfer items if
                            // it's holding a fluid, and vice versa)
                            if (provider.getCachedTileEntity() != null) {
                                if (item.isEmpty()) {
                                    tryProvideItems(provider, requester, tasks);
                                }
                                if (fluid.isEmpty()) {
                                    tryProvideFluids(provider, requester, tasks);
                                }
                            }

                            // if we provided something to the requester, move the requester and provider to last in their
                            // lists so another frame gets selected first, effectively round-robin.
                            if (!tasks.isEmpty()) {
                                if (logistics.get(priority).size() > 1)
                                    logistics.get(priority).add(logistics.get(priority).remove(requesterId));
                                if (logistics.get(i).size() > 1)
                                    logistics.get(i).add(logistics.get(i).remove(providerId));
                                return tasks;
                            }
                        }
                    }
                }
            }
        }

        return tasks;
    }

    private void tryProvideItems(AbstractLogisticsFrameEntity provider, AbstractLogisticsFrameEntity requester, PriorityQueue<LogisticsTask> tasks) {
        IOHelper.getInventoryForBlock(provider.getCachedTileEntity(), provider.getSide()).ifPresent(providerHandler -> {
            if (requester instanceof IProvidingInventoryListener prov) {
                prov.notify(new BlockEntityAndFace(provider.getCachedTileEntity(), provider.getSide()));
            }
            CountedItemStacks counted;
            int keepStock = 0;
            if (provider instanceof ISpecificProvider sp && sp.getKeepItemsStocked() > 0) {
                counted = new CountedItemStacks(providerHandler);
                keepStock = sp.getKeepItemsStocked();
            } else {
                counted = null;
            }

            for (int i = 0; i < providerHandler.getSlots(); i++) {
                ItemStack providedStack = providerHandler.extractItem(i, 64, true);
                if (!providedStack.isEmpty() && (!(provider instanceof ISpecificProvider sp) || sp.canProvide(providedStack))) {
                    int requested = getRequestedAmount(requester, providedStack, true);
                    if (requested > 0) {
                        if (counted != null) {
                            int remaining = counted.getInt(providedStack) - requested - keepStock;
                            if (remaining < 0) {
                                // reduce amount if needed so stock levels are honoured
                                requested += remaining;
                            }
                            counted.adjust(providedStack, -requested);
                        }
                        tasks.add(new LogisticsTask(provider, requester, providedStack.copyWithCount(requested)));
                        if (droneAccess) {
                            // a logistics drone just handles the first applicable task, so we can bail here
                            // - but logistics modules process all applicable tasks!
                            return;
                        }
                    }
                }
            }
        });
    }

    private static void tryProvideFluids(AbstractLogisticsFrameEntity provider, AbstractLogisticsFrameEntity requester, PriorityQueue<LogisticsTask> tasks) {
        IOHelper.getFluidHandlerForBlock(provider.getCachedTileEntity(), provider.getSide()).ifPresent(providerHandler -> {
            CountedFluidStacks counted;
            int keepStock = 0;
            if (provider instanceof ISpecificProvider sp && sp.getKeepFluidStocked() > 0) {
                counted = new CountedFluidStacks(providerHandler);
                keepStock = sp.getKeepFluidStocked();
            } else {
                counted = null;
            }

            FluidStack providingStack = providerHandler.drain(16000, IFluidHandler.FluidAction.SIMULATE);
            if (!providingStack.isEmpty()) {
                boolean canDrain = IntStream.range(0, providerHandler.getTanks()).anyMatch(i -> providerHandler.isFluidValid(i, providingStack));
                if (canDrain && (!(provider instanceof ISpecificProvider sp) || sp.canProvide(providingStack))) {
                    int requested = getRequestedAmount(requester, providingStack, true);
                    if (requested > 0) {
                        if (counted != null) {
                            int remaining = counted.getInt(providingStack) - requested - keepStock;
                            if (remaining < 0) {
                                // reduce amount if needed so stock levels are honoured
                                requested += remaining;
                            }
                            counted.adjust(providingStack, -requested);
                        }
                        tasks.add(new LogisticsTask(provider, requester, providingStack.copyWithAmount(requested)));
                    }
                }
            }
        });
    }

    private static int getRequestedAmount(AbstractLogisticsFrameEntity requester, ItemStack providedStack, boolean honourMin) {
        int requestedAmount = requester instanceof ISpecificRequester sr ? sr.amountRequested(providedStack) : providedStack.getCount();
        int minOrderSize = honourMin && requester instanceof ISpecificRequester sr ? sr.getMinItemOrderSize() : 1;

        if (requestedAmount >= minOrderSize) {
            ItemStack toProvide = providedStack.copyWithCount(Math.min(providedStack.getCount(), requestedAmount));
            ItemStack remainder = toProvide.copyWithCount(toProvide.getCount() + requester.getIncomingItems(toProvide));
            ItemStack excess = IOHelper.insert(requester.getCachedTileEntity(), remainder, requester.getSide(), true);
            toProvide.shrink(excess.getCount());
            if (toProvide.getCount() >= minOrderSize) {
                return Math.max(toProvide.getCount(), 0);
            }
        }
        return 0;
    }

    private static int getRequestedAmount(AbstractLogisticsFrameEntity requester, FluidStack providedStack, boolean honourMin) {
        int requestedAmount = requester instanceof ISpecificRequester sr ? sr.amountRequested(providedStack) : providedStack.getAmount();
        int minOrderSize = honourMin && requester instanceof ISpecificRequester sr ? sr.getMinFluidOrderSize() : 1;

        if (requestedAmount >= minOrderSize) {
            FluidStack toProvide = providedStack.copyWithAmount(Math.min(providedStack.getAmount(), requestedAmount));
            FluidStack remainder = toProvide.copyWithAmount(toProvide.getAmount() + requester.getIncomingFluid(toProvide.getFluid()));
            IOHelper.getFluidHandlerForBlock(requester.getCachedTileEntity(), requester.getSide()).ifPresent(fluidHandler -> {
                int fluidFilled = fluidHandler.fill(remainder, IFluidHandler.FluidAction.SIMULATE);
                if (fluidFilled > 0) {
                    remainder.shrink(fluidFilled);
                }
            });
            toProvide.shrink(remainder.getAmount());
            if (toProvide.getAmount() >= minOrderSize) {
                return Math.max(toProvide.getAmount(), 0);
            }
        }

        return 0;
    }

    public static class LogisticsTask implements Comparable<LogisticsTask> {
        public final AbstractLogisticsFrameEntity provider, requester;
        public final Either<ItemStack,FluidStack> resource;

        LogisticsTask(AbstractLogisticsFrameEntity provider, AbstractLogisticsFrameEntity requester, @Nonnull ItemStack transportingItem) {
            this.provider = provider;
            this.requester = requester;
            this.resource = Either.left(transportingItem);
        }

        LogisticsTask(AbstractLogisticsFrameEntity provider, AbstractLogisticsFrameEntity requester,
                      FluidStack transportingFluid) {
            this.provider = provider;
            this.requester = requester;
            this.resource = Either.right(transportingFluid);
        }

        public void informRequester() {
            resource.ifLeft(requester::informIncomingStack).ifRight(requester::informIncomingStack);
        }

        public ItemStack itemStack() {
            return resource.left().orElse(ItemStack.EMPTY);
        }

        public FluidStack fluidStack() {
            return resource.right().orElse(FluidStack.EMPTY);
        }

        public boolean isStillValid(Object stack) {
            if (stack instanceof ItemStack is) {
                if (!itemStack().isEmpty()) {
                    int requestedAmount = getRequestedAmount(requester, is, false);
                    return requestedAmount == is.getCount();
                }
            } else if (stack instanceof FluidStack fs) {
                if (!fluidStack().isEmpty()) {
                    int requestedAmount = getRequestedAmount(requester, fs, false);
                    return requestedAmount == fs.getAmount();
                }
            } else {
                throw new IllegalArgumentException("arg must be ItemStack or FluidStack! " + stack);
            }
            return false;
        }

        @Override
        public int compareTo(LogisticsTask otherTask) {
            int value = resource.map(stack -> stack.getCount() * 100, FluidStack::getAmount);
            int otherValue = otherTask.resource.map(stack -> stack.getCount() * 100, FluidStack::getAmount);
            return otherValue - value;
        }

    }
}
