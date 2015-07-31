package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntityAdvancedLiquidCompressor;
import pneumaticCraft.proxy.CommonProxy;

public class BlockAdvancedLiquidCompressor extends BlockPneumaticCraftModeled{

    protected BlockAdvancedLiquidCompressor(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityAdvancedLiquidCompressor.class;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    public int getGuiID(){
        return CommonProxy.GUI_ID_ADVANCED_LIQUID_COMPRESSOR;
    }

}
