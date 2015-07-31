package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntityAirCompressor;
import pneumaticCraft.proxy.CommonProxy;

public class BlockAirCompressor extends BlockPneumaticCraftModeled{

    public BlockAirCompressor(Material par2Material){
        super(par2Material);
    }

    @Override
    public int getGuiID(){
        return CommonProxy.GUI_ID_AIR_COMPRESSOR;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityAirCompressor.class;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }
}
