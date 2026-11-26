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
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * A single fluid tank.
 */
public class PNCFluidTank extends FluidStacksResourceHandler {
    protected Predicate<FluidStack> validator;

    public PNCFluidTank(int capacity) {
        this(capacity, e -> true);
    }

    public PNCFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(1, capacity);
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
    public boolean isValid(int index, FluidResource resource) {
        return resource.test(this::isFluidValid);
    }

    public boolean isFluidValid(FluidStack stack) {
        return validator.test(stack);
    }

    public int getCapacity() {
        return capacity;
    }

    /**
     * {@return the fluid in the tank; this is the stored stack itself, not a copy, so don't modify it}
     */
    @Nonnull
    public FluidStack getFluid() {
        return stacks.getFirst();
    }

    public int getFluidAmount() {
        return getFluid().getAmount();
    }

    public PNCFluidTank readFromNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        FluidStack fluid = FluidStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).result().orElse(FluidStack.EMPTY);
        setFluid(fluid);
        return this;
    }

    public Tag writeToNBT(HolderLookup.Provider provider) {
        return FluidStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), getFluid()).getOrThrow();
    }

    // Same format as the legacy FluidTank, so existing saved tanks still load

    @Override
    public void serialize(ValueOutput output) {
        if (!getFluid().isEmpty()) {
            output.store("Fluid", FluidStack.CODEC, getFluid());
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        stacks.set(0, input.read("Fluid", FluidStack.CODEC).orElse(FluidStack.EMPTY));
    }

    @Override
    protected final void onContentsChanged(int index, FluidStack previousContents) {
        onContentsChanged(previousContents);
    }

    protected void onContentsChanged(FluidStack prevStack) {
        // do nothing - override in subclasses
    }

    public void setFluid(FluidStack stack) {
        set(0, FluidResource.of(stack), stack.getAmount());
    }

    public boolean isEmpty() {
        return getFluid().isEmpty();
    }

    public int getSpace() {
        return Math.max(0, capacity - getFluidAmount());
    }

    public SimpleFluidContent getContent() {
        return SimpleFluidContent.copyOf(getFluid());
    }

    public void loadFromContent(SimpleFluidContent contents) {
        stacks.set(0, contents.copy());
    }
}
