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

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
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
    protected FluidStack fluidStack = FluidStack.EMPTY;
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

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return validator.test(stack);
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Nonnull
    public FluidStack getFluid() {
        return fluidStack;
    }

    @Override
    public int getFluidAmount() {
        return fluidStack.getAmount();
    }

    public PNCFluidTank readFromNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        FluidStack fluid = FluidStack.parseOptional(provider, nbt);
        setFluid(fluid);
        return this;
    }

    public Tag writeToNBT(HolderLookup.Provider provider) {
        return fluidStack.saveOptional(provider);
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
            if (fluidStack.isEmpty()) {
                return Math.min(capacity, resource.getAmount());
            }
            if (!FluidStack.isSameFluidSameComponents(fluidStack, resource)) {
                return 0;
            }
            return Math.min(capacity - fluidStack.getAmount(), resource.getAmount());
        }
        Fluid prevFluid = fluidStack.getFluid();
        int prevAmount = fluidStack.getAmount();
        if (fluidStack.isEmpty()) {
            fluidStack = new FluidStack(resource.getFluid(), Math.min(capacity, resource.getAmount()));
            onContentsChanged(prevFluid, prevAmount);
            return fluidStack.getAmount();
        }
        if (!FluidStack.isSameFluidSameComponents(fluidStack, resource)) {
            return 0;
        }
        int filled = capacity - fluidStack.getAmount();

        if (resource.getAmount() < filled) {
            fluidStack.grow(resource.getAmount());
            filled = resource.getAmount();
        } else {
            fluidStack.setAmount(capacity);
        }
        if (filled > 0) {
            onContentsChanged(prevFluid, prevAmount);
        }
        return filled;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, fluidStack)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        int drained = maxDrain;
        if (fluidStack.getAmount() < drained) {
            drained = fluidStack.getAmount();
        }
        FluidStack stack = new FluidStack(fluidStack.getFluid(), drained);
        if (action.execute() && drained > 0) {
            Fluid prevFluid = fluidStack.getFluid();
            int prevAmount = fluidStack.getAmount();
            fluidStack.shrink(drained);
            onContentsChanged(prevFluid, prevAmount);
        }
        return stack;
    }

    protected void onContentsChanged(Fluid prevFluid, int prevAmount) {
        // do nothing - override in subclasses
    }

    public void setFluid(FluidStack stack) {
        Fluid prevFluid = fluidStack.getFluid();
        int prevAmount = fluidStack.getAmount();
        this.fluidStack = stack;
        onContentsChanged(prevFluid, prevAmount);
    }

    public boolean isEmpty() {
        return fluidStack.isEmpty();
    }

    public int getSpace() {
        return Math.max(0, capacity - fluidStack.getAmount());
    }

    public SimpleFluidContent getContent() {
        return SimpleFluidContent.copyOf(fluidStack);
    }

    public void loadFromContent(SimpleFluidContent contents) {
        fluidStack = contents.copy();
    }
}
