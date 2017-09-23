package me.desht.pneumaticcraft.common.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotFullFluidContainer extends SlotItemHandler {

    SlotFullFluidContainer(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        FluidStack fluidStack = FluidUtil.getFluidContained(stack);
        return fluidStack != null && fluidStack.amount > 0;
    }
}
