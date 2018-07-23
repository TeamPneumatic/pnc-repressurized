package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluxCompressor;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public class BlockFluxCompressor extends BlockPneumaticCraft {
    public BlockFluxCompressor() {
        super(Material.IRON, "flux_compressor");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityFluxCompressor.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.FLUX_COMPRESSOR;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }
}
