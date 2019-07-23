package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.itemblock.ItemBlockLiquidHopper;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import net.minecraft.item.BlockItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;

public class BlockLiquidHopper extends BlockOmnidirectionalHopper implements ICustomItemBlock {

    public BlockLiquidHopper() {
        super("liquid_hopper");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityLiquidHopper.class;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public BlockItem getCustomItemBlock() {
        return new ItemBlockLiquidHopper(this);
    }
}
