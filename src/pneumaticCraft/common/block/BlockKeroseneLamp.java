package pneumaticCraft.common.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.tileentity.TileEntityKeroseneLamp;
import pneumaticCraft.lib.BBConstants;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockKeroseneLamp extends BlockAirCompressor{

    public BlockKeroseneLamp(Material par2Material){
        super(par2Material);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z){
        ForgeDirection facing = ForgeDirection.getOrientation(blockAccess.getBlockMetadata(x, y, z));
        if(facing == ForgeDirection.NORTH || facing == ForgeDirection.SOUTH) {
            setBlockBounds(BBConstants.KEROSENE_LAMP_LENGTH_MIN, 0, BBConstants.KEROSENE_LAMP_WIDTH_MIN, 1 - BBConstants.KEROSENE_LAMP_LENGTH_MIN, BBConstants.KEROSENE_LAMP_TOP_MAX, 1 - BBConstants.KEROSENE_LAMP_WIDTH_MIN);
        } else {
            setBlockBounds(BBConstants.KEROSENE_LAMP_WIDTH_MIN, 0, BBConstants.KEROSENE_LAMP_LENGTH_MIN, 1 - BBConstants.KEROSENE_LAMP_WIDTH_MIN, BBConstants.KEROSENE_LAMP_TOP_MAX, 1 - BBConstants.KEROSENE_LAMP_LENGTH_MIN);
        }
    }

    @Override
    public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity){
        setBlockBoundsBasedOnState(world, i, j, k);
        super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.KEROSENE_LAMP;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityKeroseneLamp.class;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z){
        TileEntityKeroseneLamp lamp = (TileEntityKeroseneLamp)world.getTileEntity(x, y, z);
        return lamp.getRange() > 0 ? 15 : 0;
    }

}
