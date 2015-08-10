package pneumaticCraft.common.thirdparty.cofh;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.block.BlockPneumaticCraftModeled;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockFluxCompressor extends BlockPneumaticCraftModeled{

    protected BlockFluxCompressor(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityFluxCompressor.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.FLUX_COMPRESSOR;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }
}
