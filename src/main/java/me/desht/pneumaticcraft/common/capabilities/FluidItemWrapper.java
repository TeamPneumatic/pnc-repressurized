package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.common.tileentity.ISerializableTanks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidItemWrapper implements ICapabilityProvider {
    private final ItemStack stack;
    private final String tankName;
    private final int capacity;

    public FluidItemWrapper(ItemStack stack, String tankName, int capacity) {
        this.stack = stack;
        this.tankName = tankName;
        this.capacity = capacity;
    }

    @Nonnull
    @Override
    public LazyOptional<? extends IFluidHandlerItem> getCapability(@Nonnull Capability capability, @Nullable Direction facing) {
        return LazyOptional.of(Handler::new);
    }

    class Handler implements IFluidHandlerItem {
        private final FluidTank fluidTank;

        Handler() {
            fluidTank = ISerializableTanks.deserializeTank(stack, tankName, capacity);
        }

        @Nonnull
        @Override
        public ItemStack getContainer() {
            return stack;
        }

        @Override
        public int getTanks() {
            return fluidTank == null ? 0 : fluidTank.getTanks();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return fluidTank == null ? FluidStack.EMPTY : fluidTank.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return fluidTank == null ? 0 : fluidTank.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return fluidTank != null && fluidTank.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction doFill) {
            if (fluidTank == null) return 0;
            int filled = fluidTank.fill(resource, doFill);
            if (filled > 0 && doFill == FluidAction.EXECUTE) {
                ISerializableTanks.serializeTank(fluidTank, stack, tankName);
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            if (fluidTank == null) return null;
            FluidStack drained = fluidTank.drain(resource, doDrain);
            if (!drained.isEmpty() && doDrain == FluidAction.EXECUTE) {
                ISerializableTanks.serializeTank(fluidTank, stack, tankName);
            }
            return drained;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            if (fluidTank == null) return null;
            FluidStack drained = fluidTank.drain(maxDrain, doDrain);
            if (!drained.isEmpty() && doDrain == FluidAction.EXECUTE) {
                ISerializableTanks.serializeTank(fluidTank, stack, tankName);
            }
            return drained;
        }
    }
}
