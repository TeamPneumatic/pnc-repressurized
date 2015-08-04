package pneumaticCraft.common.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.tileentity.TileEntityUVLightBox;
import pneumaticCraft.lib.BBConstants;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockUVLightBox extends BlockPneumaticCraftModeled{

    public BlockUVLightBox(Material par2Material){
        super(par2Material);

    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z){
        ForgeDirection facing = ForgeDirection.getOrientation(blockAccess.getBlockMetadata(x, y, z));
        if(facing == ForgeDirection.NORTH || facing == ForgeDirection.SOUTH) {
            setBlockBounds(BBConstants.UV_LIGHT_BOX_LENGTH_MIN, 0, BBConstants.UV_LIGHT_BOX_WIDTH_MIN, 1 - BBConstants.UV_LIGHT_BOX_LENGTH_MIN, BBConstants.UV_LIGHT_BOX_TOP_MAX, 1 - BBConstants.UV_LIGHT_BOX_WIDTH_MIN);
        } else {
            setBlockBounds(BBConstants.UV_LIGHT_BOX_WIDTH_MIN, 0, BBConstants.UV_LIGHT_BOX_LENGTH_MIN, 1 - BBConstants.UV_LIGHT_BOX_WIDTH_MIN, BBConstants.UV_LIGHT_BOX_TOP_MAX, 1 - BBConstants.UV_LIGHT_BOX_LENGTH_MIN);
        }
    }

    @Override
    public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity){
        setBlockBoundsBasedOnState(world, i, j, k);
        super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityUVLightBox.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.UV_LIGHT_BOX;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z){
        Block block = world.getBlock(x, y, z);
        if(block != null && block != this) {
            return block.getLightValue(world, x, y, z);
        }
        TileEntity te = world.getTileEntity(x, y, z);
        if(te != null && te instanceof TileEntityUVLightBox) {
            return ((TileEntityUVLightBox)te).getLightLevel();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    /**
     * Returns true if the block is emitting direct/strong redstone power on the
     * specified side. Args: World, X, Y, Z, side. Note that the side is
     * reversed - eg it is 1 (up) when checking the bottom of the block.
     */
    @Override
    public int isProvidingStrongPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5){
        return 0;
    }

    /**
     * Returns true if the block is emitting indirect/weak redstone power on the
     * specified side. If isBlockNormalCube returns true, standard redstone
     * propagation rules will apply instead and this will not be called. Args:
     * World, X, Y, Z, side. Note that the side is reversed - eg it is 1 (up)
     * when checking the bottom of the block.
     */
    @Override
    public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5){

        TileEntity te = par1IBlockAccess.getTileEntity(par2, par3, par4);
        if(te instanceof TileEntityUVLightBox) {
            TileEntityUVLightBox teLb = (TileEntityUVLightBox)te;
            return teLb.shouldEmitRedstone() ? 15 : 0;
        }

        return 0;
    }

    @Override
    public boolean canProvidePower(){
        return true;
    }
}
