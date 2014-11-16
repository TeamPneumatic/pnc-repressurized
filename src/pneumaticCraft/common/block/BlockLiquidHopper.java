package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntityLiquidHopper;
import pneumaticCraft.proxy.CommonProxy;

public class BlockLiquidHopper extends BlockOmnidirectionalHopper{

    public BlockLiquidHopper(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityLiquidHopper.class;
    }

    @Override
    protected int getGuiID(){
        return CommonProxy.GUI_ID_LIQUID_HOPPER;
    }
}
