package pneumaticCraft.common.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.tileentity.TileEntityAphorismTile;
import pneumaticCraft.lib.BBConstants;
import pneumaticCraft.lib.Textures;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockAphorismTile extends BlockPneumaticCraft{
    public BlockAphorismTile(Material par2Material){
        super(par2Material);
        setBlockTextureName(Textures.BLOCK_APHORISM_TILE);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityAphorismTile.class;
    }

    @Override
    public boolean renderAsNormalBlock(){
        return false;
    }

    @Override
    public boolean isOpaqueCube(){
        return false;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int par2, int par3, int par4){
        ForgeDirection dir = ForgeDirection.getOrientation(blockAccess.getBlockMetadata(par2, par3, par4));
        setBlockBounds(dir.offsetX <= 0 ? 0 : 1F - BBConstants.APHORISM_TILE_THICKNESS, dir.offsetY <= 0 ? 0 : 1F - BBConstants.APHORISM_TILE_THICKNESS, dir.offsetZ <= 0 ? 0 : 1F - BBConstants.APHORISM_TILE_THICKNESS, dir.offsetX >= 0 ? 1 : BBConstants.APHORISM_TILE_THICKNESS, dir.offsetY >= 0 ? 1 : BBConstants.APHORISM_TILE_THICKNESS, dir.offsetZ >= 0 ? 1 : BBConstants.APHORISM_TILE_THICKNESS);
    }

    @Override
    public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity){
        setBlockBoundsBasedOnState(world, i, j, k);
        super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
    }

    @Override
    public void setBlockBoundsForItemRender(){
        setBlockBounds(0, 0, 0.5F - BBConstants.APHORISM_TILE_THICKNESS / 2, 1, 1, 0.5F + BBConstants.APHORISM_TILE_THICKNESS / 2);
    }

    /**
     * Called when the block is placed in the world.
     */
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack iStack){
        super.onBlockPlacedBy(world, x, y, z, entityLiving, iStack);
        int meta = world.getBlockMetadata(x, y, z);
        if(meta < 2) {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntityAphorismTile) {
                ((TileEntityAphorismTile)te).textRotation = (((int)entityLiving.rotationYaw + 45) / 90 + 2) % 4;
            }
        }
        if(world.isRemote && entityLiving instanceof EntityPlayer) {
            ((EntityPlayer)entityLiving).openGui(PneumaticCraft.instance, EnumGuiId.APHORISM_TILE.ordinal(), world, x, y, z);
        }
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
    public boolean rotateBlock(World world, EntityPlayer player, int x, int y, int z, ForgeDirection face){
        if(player.isSneaking()) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if(tile instanceof TileEntityAphorismTile) {
                TileEntityAphorismTile teAt = (TileEntityAphorismTile)tile;
                if(++teAt.textRotation > 3) teAt.textRotation = 0;
                return true;
            } else {
                return false;
            }
        } else {
            return super.rotateBlock(world, player, x, y, z, face);
        }
    }

    @Override
    protected boolean rotateForgeWay(){
        return false;
    }
}
