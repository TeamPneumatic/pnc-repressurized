package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCompressor;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

public class BlockAirCompressor extends BlockPneumaticCraftModeled {

    public static final PropertyBool ON = PropertyBool.create("on");

    BlockAirCompressor() {
        super(Material.IRON, "air_compressor");
    }

    BlockAirCompressor(String name) { super(Material.IRON, name); }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ROTATION, ON);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return super.getMetaFromState(state) + (state.getValue(ON) ? 6 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(ON, meta >= 6);
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.AIR_COMPRESSOR;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAirCompressor.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }
}
