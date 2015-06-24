package pneumaticCraft.common.semiblock;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface ISpecificRequester{
    public int amountRequested(ItemStack stack);

    public int amountRequested(FluidStack stack);
}
