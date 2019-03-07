package me.desht.pneumaticcraft.common.semiblock;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface ISpecificRequester {
    String NBT_MIN_ITEMS = "minItems";
    String NBT_MIN_FLUID = "minFluid";

    int amountRequested(ItemStack stack);

    int amountRequested(FluidStack stack);
}
