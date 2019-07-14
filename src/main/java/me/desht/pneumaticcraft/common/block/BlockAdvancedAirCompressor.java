package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAdvancedAirCompressor;
import net.minecraft.tileentity.TileEntity;

public class BlockAdvancedAirCompressor extends BlockAirCompressor {

    public BlockAdvancedAirCompressor() {
        super("advanced_air_compressor");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAdvancedAirCompressor.class;
    }

}
