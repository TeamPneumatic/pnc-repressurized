package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import pneumaticCraft.proxy.CommonProxy;

public class BlockThermopneumaticProcessingPlant extends BlockPneumaticCraftModeled{

    public BlockThermopneumaticProcessingPlant(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityThermopneumaticProcessingPlant.class;
    }

    @Override
    protected int getGuiID(){
        return CommonProxy.GUI_ID_THERMOPNEUMATIC_PROCESSING_PLANT;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }
}
