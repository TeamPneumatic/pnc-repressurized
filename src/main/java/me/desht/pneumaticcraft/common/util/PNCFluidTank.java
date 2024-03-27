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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * Much like the default FluidTank implementation in Forge, but also passes the previous fluid &amp; amount in its
 * onContentsChanged() method.
 */
public class PNCFluidTank implements IFluidHandler, IFluidTank {
    protected Predicate<FluidStack> validator;
    @Nonnull
    protected FluidStack fluid = FluidStack.EMPTY;
    protected int capacity;

    public PNCFluidTank(int capacity) {
        this(capacity, e -> true);
    }

    public PNCFluidTank(int capacity, Predicate<FluidStack> validator) {
        this.capacity = capacity;
        this.validator = validator;
    }

    public PNCFluidTank setCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public PNCFluidTank setValidator(Predicate<FluidStack> validator) {
        if (validator != null) {
            this.validator = validator;
        }
        return this;
    }

    public boolean isFluidValid(FluidStack stack) {
        return validator.test(stack);
    }

    public int getCapacity() {
        return capacity;
    }

    @Nonnull
    public FluidStack getFluid() {
        return fluid;
    }

    public int getFluidAmount() {
        return fluid.getAmount();
    }

    public PNCFluidTank readFromNBT(CompoundTag nbt) {
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
        setFluid(fluid);
        return this;
    }

    public CompoundTag writeToNBT(CompoundTag nbt) {
        fluid.writeToNBT(nbt);
        return nbt;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(resource)) {
            return 0;
        }
        if (action.simulate()) {
            if (fluid.isEmpty()) {
                return Math.min(capacity, resource.getAmount());
            }
            if (!fluid.isFluidEqual(resource)) {
                return 0;
            }
            return Math.min(capacity - fluid.getAmount(), resource.getAmount());
        }
        Fluid prevFluid = fluid.getFluid();
        int prevAmount = fluid.getAmount();
        if (fluid.isEmpty()) {
            fluid = new FluidStack(resource, Math.min(capacity, resource.getAmount()));
            onContentsChanged(prevFluid, prevAmount);
            return fluid.getAmount();
        }
        if (!fluid.isFluidEqual(resource)) {
            return 0;
        }
        int filled = capacity - fluid.getAmount();

        if (resource.getAmount() < filled) {
            fluid.grow(resource.getAmount());
            filled = resource.getAmount();
        } else {
            fluid.setAmount(capacity);
        }
        if (filled > 0) {
            onContentsChanged(prevFluid, prevAmount);
        }
        return filled;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !resource.isFluidEqual(fluid)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        int drained = maxDrain;
        if (fluid.getAmount() < drained) {
            drained = fluid.getAmount();
        }
        FluidStack stack = new FluidStack(fluid, drained);
        if (action.execute() && drained > 0) {
            Fluid prevFluid = fluid.getFluid();
            int prevAmount = fluid.getAmount();
            fluid.shrink(drained);
            onContentsChanged(prevFluid, prevAmount);
        }
        return stack;
    }

    protected void onContentsChanged(Fluid prevFluid, int prevAmount) {
        // do nothing - override in subclasses
    }

    public void setFluid(FluidStack stack) {
        Fluid prevFluid = fluid.getFluid();
        int prevAmount = fluid.getAmount();
        this.fluid = stack;
        onContentsChanged(prevFluid, prevAmount);
    }

    public boolean isEmpty() {
        return fluid.isEmpty();
    }

    public int getSpace() {
        return Math.max(0, capacity - fluid.getAmount());
    }
}
