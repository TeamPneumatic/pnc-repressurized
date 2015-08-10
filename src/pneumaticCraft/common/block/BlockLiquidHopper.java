package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntityLiquidHopper;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockLiquidHopper extends BlockOmnidirectionalHopper{

    public BlockLiquidHopper(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityLiquidHopper.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.LIQUID_HOPPER;
    }
}
