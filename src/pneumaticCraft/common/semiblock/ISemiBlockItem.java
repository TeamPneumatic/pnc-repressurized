package pneumaticCraft.common.semiblock;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface ISemiBlockItem{
    public ISemiBlock getSemiBlock(World world, int x, int y, int z, ItemStack stack);
}
