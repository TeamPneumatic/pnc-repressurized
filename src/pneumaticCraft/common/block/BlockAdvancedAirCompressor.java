package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntityAdvancedAirCompressor;
import pneumaticCraft.proxy.CommonProxy;

public class BlockAdvancedAirCompressor extends BlockAirCompressor{

    public BlockAdvancedAirCompressor(Material par2Material){
        super(par2Material);
    }

    @Override
    public int getGuiID(){
        return CommonProxy.GUI_ID_ADVANCED_AIR_COMPRESSOR;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityAdvancedAirCompressor.class;
    }

}
