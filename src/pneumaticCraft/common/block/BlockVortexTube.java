package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.common.tileentity.TileEntityVortexTube;

public class BlockVortexTube extends BlockPneumaticCraftModeled{

    protected BlockVortexTube(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityVortexTube.class;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom(){
        return true;
    }

    @Override
    protected boolean rotateCustom(World world, int x, int y, int z, ForgeDirection side, int meta){
        if(meta == side.ordinal() || meta == side.getOpposite().ordinal()) {
            TileEntityVortexTube te = (TileEntityVortexTube)world.getTileEntity(x, y, z);
            te.rotateRoll(meta == side.ordinal() ? 1 : -1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack){
        super.onBlockPlacedBy(world, x, y, z, par5EntityLiving, par6ItemStack);
        TileEntityVortexTube te = (TileEntityVortexTube)world.getTileEntity(x, y, z);
        for(int i = 0; i < 4; i++) {
            te.rotateRoll(1);
            ForgeDirection d = te.getTubeDirection();
            IPneumaticMachine pneumaticMachine = ModInteractionUtils.getInstance().getMachine(world.getTileEntity(x + d.offsetX, y + d.offsetY, z + d.offsetZ));
            if(pneumaticMachine != null && pneumaticMachine.isConnectedTo(d.getOpposite())) break;
        }
    }
}
