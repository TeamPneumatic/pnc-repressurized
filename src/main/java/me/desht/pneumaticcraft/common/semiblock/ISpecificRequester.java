package me.desht.pneumaticcraft.common.semiblock;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface ISpecificRequester {
    int amountRequested(ItemStack stack);

    int amountRequested(FluidStack stack);
}
