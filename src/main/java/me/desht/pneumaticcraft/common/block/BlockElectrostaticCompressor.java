package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityElectrostaticCompressor;
import net.minecraft.tileentity.TileEntity;

public class BlockElectrostaticCompressor extends BlockPneumaticCraft {
    public BlockElectrostaticCompressor(Properties props) {
        super(props);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityElectrostaticCompressor.class;
    }
}
