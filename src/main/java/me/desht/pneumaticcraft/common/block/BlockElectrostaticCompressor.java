package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityElectrostaticCompressor;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

public class BlockElectrostaticCompressor extends BlockPneumaticCraftModeled {
    BlockElectrostaticCompressor() {
        super(Material.IRON, "electrostatic_compressor");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityElectrostaticCompressor.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.ELECTROSTATIC_COMPRESSOR;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return true;
    }
}
