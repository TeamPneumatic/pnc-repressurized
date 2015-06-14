package pneumaticCraft.common.semiblock;

import net.minecraft.item.ItemStack;

public interface ISpecificRequester{
    public int amountRequested(ItemStack stack);
}
