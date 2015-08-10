package pneumaticCraft.common.thirdparty.ic2;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.block.BlockPneumaticCraftModeled;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockElectricCompressor extends BlockPneumaticCraftModeled{

    public BlockElectricCompressor(Material par2Material){
        super(par2Material);
    }

    @Override
    public boolean canRenderInPass(int pass){
        return true;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityElectricCompressor.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.ELECTRIC_COMPRESSOR;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom(){
        return false;
    }
}
