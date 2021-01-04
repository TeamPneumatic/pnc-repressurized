package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidCompressor;
import net.minecraft.tileentity.TileEntity;

public class BlockLiquidCompressor extends BlockPneumaticCraft {
    public BlockLiquidCompressor() {
        super(ModBlocks.defaultProps().notSolid());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityLiquidCompressor.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }
}
