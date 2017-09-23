package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityCreativeCompressor;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public class BlockCreativeCompressor extends BlockPneumaticCraftModeled {

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
}
