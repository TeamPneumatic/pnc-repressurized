package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.item.ItemPneumatic;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCreativeCompressor;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntity;

public class BlockCreativeCompressor extends BlockPneumaticCraft implements ICustomItemBlock {

    public BlockCreativeCompressor() {
        super(Material.IRON, "creative_compressor");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityCreativeCompressor.class;
    }

    @Override
    public BlockItem getCustomItemBlock() {
        return new ItemBlockCreativeCompressor(this);
    }

    public static class ItemBlockCreativeCompressor extends BlockItem {
        ItemBlockCreativeCompressor(BlockCreativeCompressor blockCreativeCompressor) {
            super(blockCreativeCompressor, ItemPneumatic.DEFAULT_PROPS);
        }

        @Override
        public Rarity getRarity(ItemStack stack) {
            return Rarity.EPIC;
        }
    }
}
