package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.item.ItemPneumatic;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCreativeCompressor;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BlockCreativeCompressor extends BlockPneumaticCraft implements ICustomItemBlock {

    public BlockCreativeCompressor() {
        super("creative_compressor");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityCreativeCompressor.class;
    }

    @Override
    public BlockItem getCustomItemBlock() {
        return new ItemBlockCreativeCompressor(this);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return ALMOST_FULL_SHAPE;
    }

    public static class ItemBlockCreativeCompressor extends BlockItem {
        ItemBlockCreativeCompressor(BlockCreativeCompressor blockCreativeCompressor) {
            super(blockCreativeCompressor, ItemPneumatic.defaultProps());
        }

        @Override
        public Rarity getRarity(ItemStack stack) {
            return Rarity.EPIC;
        }
    }
}
