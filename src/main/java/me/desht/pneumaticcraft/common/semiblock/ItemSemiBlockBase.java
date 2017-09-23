package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSemiBlockBase extends Item implements ISemiBlockItem {
    public final String semiBlockId;

    public ItemSemiBlockBase(String semiBlockId) {
        this.semiBlockId = semiBlockId;
        setRegistryName(semiBlockId);
        setUnlocalizedName(semiBlockId);
        setCreativeTab(PneumaticCraftRepressurized.tabPneumaticCraft);
    }

    public ItemSemiBlockBase(Class<? extends ISemiBlock> semiBlock) {
        this(SemiBlockManager.getKeyForSemiBlock(semiBlock));
    }

    @Override
    public ISemiBlock getSemiBlock(World world, BlockPos pos, ItemStack stack) {
        return SemiBlockManager.getSemiBlockForKey(semiBlockId);
    }

}
