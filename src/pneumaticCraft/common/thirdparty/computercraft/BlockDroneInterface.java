package pneumaticCraft.common.thirdparty.computercraft;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.block.BlockPneumaticCraftModeled;

public class BlockDroneInterface extends BlockPneumaticCraftModeled{

    protected BlockDroneInterface(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityDroneInterface.class;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side){
        return side == ForgeDirection.DOWN;
    }

}
