package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockThermopneumaticProcessingPlant extends BlockPneumaticCraftModeled{

    public BlockThermopneumaticProcessingPlant(Material par2Material){
        super(par2Material);
        setBlockBounds(0, 0, 0, 1, 10 / 16F, 1);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        setBlockBounds(0, 0, 0, 1, 12 / 16F, 1);
        return TileEntityThermopneumaticProcessingPlant.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.THERMOPNEUMATIC_PROCESSING_PLANT;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }
}
