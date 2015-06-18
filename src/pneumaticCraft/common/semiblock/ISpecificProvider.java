package pneumaticCraft.common.semiblock;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface ISpecificProvider{

    public boolean canProvide(ItemStack providingStack);

    public boolean canProvide(FluidStack providingStack);

}
