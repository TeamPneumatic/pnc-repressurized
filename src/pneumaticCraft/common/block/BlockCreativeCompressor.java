package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntityCreativeCompressor;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockCreativeCompressor extends BlockPneumaticCraftModeled{

    protected BlockCreativeCompressor(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityCreativeCompressor.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.CREATIVE_COMPRESSOR;
    }

}
