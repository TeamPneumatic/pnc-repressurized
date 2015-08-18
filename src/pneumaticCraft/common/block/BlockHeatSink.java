package pneumaticCraft.common.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.tileentity.TileEntityHeatSink;
import pneumaticCraft.lib.BBConstants;

public class BlockHeatSink extends BlockPneumaticCraftModeled{

    protected BlockHeatSink(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityHeatSink.class;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int par2, int par3, int par4){
        ForgeDirection dir = ForgeDirection.getOrientation(blockAccess.getBlockMetadata(par2, par3, par4));
        setBlockBounds(dir.offsetX <= 0 ? 0 : 1F - BBConstants.HEAT_SINK_THICKNESS, dir.offsetY <= 0 ? 0 : 1F - BBConstants.HEAT_SINK_THICKNESS, dir.offsetZ <= 0 ? 0 : 1F - BBConstants.HEAT_SINK_THICKNESS, dir.offsetX >= 0 ? 1 : BBConstants.HEAT_SINK_THICKNESS, dir.offsetY >= 0 ? 1 : BBConstants.HEAT_SINK_THICKNESS, dir.offsetZ >= 0 ? 1 : BBConstants.HEAT_SINK_THICKNESS);
    }

    @Override
    public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity){
        setBlockBoundsBasedOnState(world, i, j, k);
        super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
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
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity){
        TileEntityHeatSink heatSink = (TileEntityHeatSink)world.getTileEntity(x, y, z);
        if(heatSink.getHeatExchangerLogic(ForgeDirection.UNKNOWN).getTemperature() > 323) {
            entity.setFire(3);
        }
    }
}
