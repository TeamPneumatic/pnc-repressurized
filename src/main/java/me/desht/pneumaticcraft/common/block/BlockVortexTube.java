package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityVortexTube;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public class BlockVortexTube extends BlockPneumaticCraftModeled {

    public BlockVortexTube() {
        super(Material.IRON, "vortex_tube");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityVortexTube.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }
}
