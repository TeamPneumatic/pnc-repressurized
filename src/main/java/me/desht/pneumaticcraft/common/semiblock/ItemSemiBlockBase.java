package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.item.ItemPneumatic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSemiBlockBase extends ItemPneumatic implements ISemiBlockItem {
    public final String semiBlockId;

    public ItemSemiBlockBase(Item.Properties props, String semiBlockId) {
        super(props, semiBlockId);
        this.semiBlockId = semiBlockId;
    }

    public ItemSemiBlockBase(Item.Properties props, Class<? extends ISemiBlock> semiBlock) {
        this(props, SemiBlockManager.getKeyForSemiBlock(semiBlock));
    }

    @Override
    public ISemiBlock getSemiBlock(World world, BlockPos pos, ItemStack stack) {
        return SemiBlockManager.getSemiBlockForKey(semiBlockId);
    }

}
