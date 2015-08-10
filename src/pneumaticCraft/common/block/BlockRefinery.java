package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pneumaticCraft.common.tileentity.TileEntityRefinery;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockRefinery extends BlockPneumaticCraftModeled{

    public BlockRefinery(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityRefinery.class;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
        TileEntityRefinery refinery = (TileEntityRefinery)world.getTileEntity(x, y, z);
        refinery = refinery.getMasterRefinery();
        return super.onBlockActivated(world, x, refinery.yCoord, z, player, par6, par7, par8, par9);
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.REFINERY;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }
}
