package pneumaticCraft.common.thirdparty.computercraft;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.block.BlockPneumaticCraftModeled;

public class BlockDroneInterface extends BlockPneumaticCraftModeled{

    protected BlockDroneInterface(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityDroneInterface.class;
    }
}
