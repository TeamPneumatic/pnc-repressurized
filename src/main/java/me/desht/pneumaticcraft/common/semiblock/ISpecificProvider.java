package me.desht.pneumaticcraft.common.semiblock;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface ISpecificProvider {

    boolean canProvide(ItemStack providingStack);

    boolean canProvide(FluidStack providingStack);

}
