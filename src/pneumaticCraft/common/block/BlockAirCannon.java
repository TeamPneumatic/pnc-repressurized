package pneumaticCraft.common.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.common.tileentity.TileEntityAirCannon;
import pneumaticCraft.lib.BBConstants;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockAirCannon extends BlockPneumaticCraftModeled{

    public BlockAirCannon(Material par2Material){
        super(par2Material);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4){
        setBlockBounds(BBConstants.AIR_CANNON_MIN_POS_SIDE, 0F, BBConstants.AIR_CANNON_MIN_POS_SIDE, BBConstants.AIR_CANNON_MAX_POS_SIDE, BBConstants.AIR_CANNON_MAX_POS_TOP, BBConstants.AIR_CANNON_MAX_POS_SIDE);
    }

    @Override
    public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity){
        setBlockBounds(BBConstants.AIR_CANNON_MIN_POS_SIDE, BBConstants.AIR_CANNON_MIN_POS_SIDE, BBConstants.AIR_CANNON_MIN_POS_SIDE, BBConstants.AIR_CANNON_MAX_POS_SIDE, BBConstants.AIR_CANNON_MAX_POS_TOP, BBConstants.AIR_CANNON_MAX_POS_SIDE);
        super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.AIR_CANNON;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityAirCannon.class;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block){
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityAirCannon) {
            TileEntityAirCannon teAc = (TileEntityAirCannon)te;
            teAc.onNeighbourBlockChange(x, y, z, block);
        }
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack){
        super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLiving, par6ItemStack);
        TileEntity te = par1World.getTileEntity(par2, par3, par4);
        if(te instanceof TileEntityAirCannon) {
            TileEntityAirCannon teAc = (TileEntityAirCannon)te;
            teAc.onNeighbourBlockChange(par2, par3, par4, this);
        }
    }
}
