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

package me.desht.pneumaticcraft.common.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.Nullable;

/**
 * TODO 26.1 transfer: temporary bridge so PNC code written against {@link IFluidHandlerItem} keeps working
 * on top of the transfer API. The item lives in a one-slot handler, so the item access can exchange it
 * (e.g. a filled bucket becomes an empty bucket), and {@link #getContainer()} returns the result.
 * Remove once callers use {@code ResourceHandler<FluidResource>} directly.
 */
public final class LegacyFluidHandlerItem implements IFluidHandlerItem {
    private final ItemStacksResourceHandler slot;
    private final IFluidHandler delegate;

    private LegacyFluidHandlerItem(ItemStacksResourceHandler slot, IFluidHandler delegate) {
        this.slot = slot;
        this.delegate = delegate;
    }

    @Nullable
    public static IFluidHandlerItem of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ItemStacksResourceHandler slot = new ItemStacksResourceHandler(NonNullList.of(ItemStack.EMPTY, stack.copy()));
        ResourceHandler<FluidResource> handler = ItemAccess.forHandlerIndex(slot, 0).getCapability(Capabilities.Fluid.ITEM);
        return handler == null ? null : new LegacyFluidHandlerItem(slot, IFluidHandler.of(handler));
    }

    @Override
    public ItemStack getContainer() {
        return slot.getResource(0).toStack(slot.getAmountAsInt(0));
    }

    @Override
    public int getTanks() {
        return delegate.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return delegate.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return delegate.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return delegate.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return delegate.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return delegate.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return delegate.drain(maxDrain, action);
    }
}
