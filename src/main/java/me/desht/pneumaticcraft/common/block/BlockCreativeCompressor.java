package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCreativeCompressor;
import net.minecraft.block.material.Material;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class BlockCreativeCompressor extends BlockPneumaticCraftModeled implements ICustomItemBlock {

    BlockCreativeCompressor() {
        super(Material.IRON, "creative_compressor");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityCreativeCompressor.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.CREATIVE_COMPRESSOR;
    }

    @Override
    public boolean getTickRandomly() {
        return super.getTickRandomly();
    }

    @Override
    public ItemBlock getCustomItemBlock() {
        return new ItemBlockCreativeCompressor(this);
    }

    public static class ItemBlockCreativeCompressor extends ItemBlock {
        ItemBlockCreativeCompressor(BlockCreativeCompressor blockCreativeCompressor) {
            super(blockCreativeCompressor);
        }

        @Override
        public EnumRarity getRarity(ItemStack stack) {
            return EnumRarity.EPIC;
        }
    }
}
