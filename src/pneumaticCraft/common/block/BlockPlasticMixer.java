package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntityPlasticMixer;
import pneumaticCraft.proxy.CommonProxy;

public class BlockPlasticMixer extends BlockPneumaticCraftModeled{

    protected BlockPlasticMixer(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityPlasticMixer.class;
    }

    @Override
    protected int getGuiID(){
        return CommonProxy.GUI_ID_PLASTIC_MIXER;
    }
}
