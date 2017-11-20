package me.desht.pneumaticcraft.common.thirdparty.ic2;

import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftModeled;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public class BlockElectricCompressor extends BlockPneumaticCraftModeled {
    protected BlockElectricCompressor() {
        super(Material.IRON, "electric_compressor");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityElectricCompressor.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.ELECTRIC_COMPRESSOR;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return false;
    }
}
