package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.common.item.ItemPneumatic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSemiBlockBase extends ItemPneumatic implements ISemiBlockItem {
    public ItemSemiBlockBase(String semiBlockId) {
        super(semiBlockId);
    }

    @Override
    public ISemiBlock getSemiBlock(World world, BlockPos pos, ItemStack stack) {
        return SemiBlockManager.getSemiBlockForKey(getRegistryName());
    }

}
