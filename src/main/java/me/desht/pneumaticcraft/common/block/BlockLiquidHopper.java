package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;

public class BlockLiquidHopper extends BlockOmnidirectionalHopper {

    public BlockLiquidHopper() {
        super();
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityLiquidHopper.class;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

}
