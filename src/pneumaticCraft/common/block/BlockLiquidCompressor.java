package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntityLiquidCompressor;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockLiquidCompressor extends BlockPneumaticCraftModeled{

    protected BlockLiquidCompressor(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityLiquidCompressor.class;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.LIQUID_COMPRESSOR;
    }

}
