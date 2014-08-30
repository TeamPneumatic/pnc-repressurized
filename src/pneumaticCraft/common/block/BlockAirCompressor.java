package pneumaticCraft.common.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.common.tileentity.TileEntityAirCompressor;
import pneumaticCraft.lib.BBConstants;
import pneumaticCraft.proxy.CommonProxy;

public class BlockAirCompressor extends BlockPneumaticCraftModeled{

    public BlockAirCompressor(Material par2Material){
        super(par2Material);
    }

    @Override
    protected int getGuiID(){
        return CommonProxy.GUI_ID_AIR_COMPRESSOR;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4){
        setBlockBounds(BBConstants.AIR_COMPRESSOR_MIN_POS, 0F, BBConstants.AIR_COMPRESSOR_MIN_POS, BBConstants.AIR_COMPRESSOR_MAX_POS, BBConstants.AIR_COMPRESSOR_MAX_POS_TOP, BBConstants.AIR_COMPRESSOR_MAX_POS);
    }

    @Override
    public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity){
        setBlockBounds(BBConstants.AIR_COMPRESSOR_MIN_POS, BBConstants.AIR_COMPRESSOR_MIN_POS, BBConstants.AIR_COMPRESSOR_MIN_POS, BBConstants.AIR_COMPRESSOR_MAX_POS, BBConstants.AIR_COMPRESSOR_MAX_POS_TOP, BBConstants.AIR_COMPRESSOR_MAX_POS);
        super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityAirCompressor.class;
    }

    @Override
    protected boolean isRotatable(){
        return true;
    }
}
