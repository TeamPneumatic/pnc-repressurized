package me.desht.pneumaticcraft.common.capabilities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import java.util.function.Predicate;

public class PNCFluidHandlerItemStack extends FluidHandlerItemStack {
    private final Predicate<Fluid> fluidPredicate;

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public PNCFluidHandlerItemStack(ItemStack container, int capacity, Predicate<Fluid> fluidPredicate) {
        super(container, capacity);
        this.fluidPredicate = fluidPredicate;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return fluidPredicate.test(stack.getFluid());
    }

    @Override
    protected void setFluid(FluidStack fluid) {
        if (fluidPredicate.test(fluid.getFluid())) {
            super.setFluid(fluid);
        }
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return fluidPredicate.test(fluid.getFluid());
    }

    @Override
    public boolean canDrainFluidType(FluidStack fluid) {
        return fluidPredicate.test(fluid.getFluid());
    }
}
