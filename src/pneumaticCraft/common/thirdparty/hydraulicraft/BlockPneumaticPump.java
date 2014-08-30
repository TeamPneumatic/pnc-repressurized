package pneumaticCraft.common.thirdparty.hydraulicraft;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.block.BlockPneumaticCraftModeled;

public class BlockPneumaticPump extends BlockPneumaticCraftModeled{

    protected BlockPneumaticPump(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityPneumaticPump.class;
    }

}
