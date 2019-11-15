package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAdvancedLiquidCompressor;
import net.minecraft.tileentity.TileEntity;

public class BlockAdvancedLiquidCompressor extends BlockPneumaticCraft {

    public BlockAdvancedLiquidCompressor() {
        super("advanced_liquid_compressor");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAdvancedLiquidCompressor.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }
}
