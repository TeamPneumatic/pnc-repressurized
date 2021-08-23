package me.desht.pneumaticcraft.common.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotFluidContainer extends SlotItemHandler {
    private final int minFluid;

    SlotFluidContainer(IItemHandler handler, int index, int x, int y, int minFluid) {
        super(handler, index, x, y);

        this.minFluid = minFluid;
    }

    SlotFluidContainer(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);

        this.minFluid = 0;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (FluidUtil.getFluidHandler(stack).isPresent()) {
            return FluidUtil.getFluidContained(stack).map(fluidStack -> fluidStack.getAmount() >= minFluid).orElse(minFluid == 0);
        } else {
            return false;
        }
    }
}
