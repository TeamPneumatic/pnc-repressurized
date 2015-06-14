package pneumaticCraft.common.semiblock;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemSemiBlockBase extends Item implements ISemiBlockItem{
    public final String semiBlockId;

    public ItemSemiBlockBase(String semiBlockId){
        this.semiBlockId = semiBlockId;
        setUnlocalizedName(semiBlockId);
    }

    public ItemSemiBlockBase(Class<? extends ISemiBlock> semiBlock){
        this(SemiBlockManager.getKeyForSemiBlock(semiBlock));
    }

    @Override
    public ISemiBlock getSemiBlock(World world, int x, int y, int z, ItemStack stack){
        return SemiBlockManager.getSemiBlockForKey(semiBlockId);
    }

}
