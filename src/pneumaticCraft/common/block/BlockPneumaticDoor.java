package pneumaticCraft.common.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
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
import pneumaticCraft.common.tileentity.TileEntityPneumaticDoor;
import pneumaticCraft.common.tileentity.TileEntityPneumaticDoorBase;
import pneumaticCraft.lib.PneumaticValues;

public class BlockPneumaticDoor extends BlockPneumaticCraftModeled{
    public boolean isTrackingPlayerEye;//will be true when the Pneumatic Door Base is determining if it should open the door dependant
                                       //on the player watched block.

    public BlockPneumaticDoor(Material par2Material){
        super(par2Material);

    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z){
        if(isTrackingPlayerEye) {
            setBlockBounds(0, 0, 0, 1, 1, 1);
        } else {
            float xMin = 0;
            float zMin = 0;
            float xMax = 1;
            float zMax = 1;
            TileEntity te = blockAccess.getTileEntity(x, y, z);
            int meta = blockAccess.getBlockMetadata(x, y, z);
            if(te instanceof TileEntityPneumaticDoor) {
                TileEntityPneumaticDoor door = (TileEntityPneumaticDoor)te;
                float cosinus = 13 / 16F - (float)Math.sin(Math.toRadians(door.rotation)) * 13 / 16F;
                float sinus = 13 / 16F - (float)Math.cos(Math.toRadians(door.rotation)) * 13 / 16F;
                if(door.rightGoing) {
                    switch(ForgeDirection.getOrientation(meta % 6)){
                        case NORTH:
                            zMin = cosinus;
                            xMax = 1 - sinus;
                            break;
                        case WEST:
                            xMin = cosinus;
                            zMin = sinus;
                            break;
                        case SOUTH:
                            zMax = 1 - cosinus;
                            xMin = sinus;
                            break;
                        case EAST:
                            xMax = 1 - cosinus;
                            zMax = 1 - sinus;
                            break;

                    }
                } else {
                    switch(ForgeDirection.getOrientation(meta % 6)){
                        case NORTH:
                            zMin = cosinus;
                            xMin = sinus;
                            break;
                        case WEST:
                            xMin = cosinus;
                            zMax = 1 - sinus;
                            break;
                        case SOUTH:
                            zMax = 1 - cosinus;
                            xMax = 1 - sinus;
                            break;
                        case EAST:
                            xMax = 1 - cosinus;
                            zMin = sinus;
                            break;

                    }
                }
            }
            setBlockBounds(xMin, meta < 6 ? 0 : -1, zMin, xMax, meta < 6 ? 2 : 1, zMax);
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
        return TileEntityPneumaticDoor.class;
    }

    /**
     * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
     */
    @Override
    public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4){
        return !super.canPlaceBlockAt(par1World, par2, par3, par4) ? false : par1World.isAirBlock(par2, par3 + 1, par4);
    }

    @Override
    public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack){
        super.onBlockPlacedBy(par1World, par2, par3, par4, par5EntityLiving, par6ItemStack);
        int l = par1World.getBlockMetadata(par2, par3, par4);
        par1World.setBlock(par2, par3 + 1, par4, this, l + 6, 3);
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, int x, int y, int z, ForgeDirection face){
        int meta = world.getBlockMetadata(x, y, z);
        if(meta < 6) {
            super.rotateBlock(world, player, x, y, z, face);
            world.setBlockMetadataWithNotify(x, y + 1, z, world.getBlockMetadata(x, y, z) + 6, 3);
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntityPneumaticDoor) {
                ((TileEntityPneumaticDoor)te).rightGoing = true;
                ((TileEntityPneumaticDoor)te).setRotation(0);
                TileEntity topDoor = world.getTileEntity(x, y + 1, z);
                if(topDoor instanceof TileEntityPneumaticDoor) {
                    ((TileEntityPneumaticDoor)topDoor).sendDescriptionPacket();
                }
            }
        } else {
            return rotateBlock(world, player, x, y - 1, z, face);
        }
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9){
        TileEntityPneumaticDoorBase doorBase = getDoorBase(world, x, y, z);
        if(!world.isRemote && doorBase != null && doorBase.redstoneMode == 2 && doorBase.getPressure(ForgeDirection.UNKNOWN) >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
            doorBase.setOpening(!doorBase.isOpening());
            doorBase.setNeighborOpening(doorBase.isOpening());
            return true;
        }
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta){
        if(meta >= 6) {
            if(world.getBlock(x, y - 1, z) == Blockss.pneumaticDoor) dropBlockAsItem(world, x, y - 1, z, new ItemStack(Blockss.pneumaticDoor));
            world.setBlockToAir(x, y - 1, z);
        } else {
            world.setBlockToAir(x, y + 1, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random random){
        return meta >= 6 ? 0 : 1;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block){
        boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
        if(!powered) {
            int meta = world.getBlockMetadata(x, y, z);
            if(meta < 6) {
                powered = world.isBlockIndirectlyGettingPowered(x, y + 1, z);
            } else {
                powered = world.isBlockIndirectlyGettingPowered(x, y - 1, z);
            }
        }
        TileEntityPneumaticDoorBase doorBase = getDoorBase(world, x, y, z);
        if(!world.isRemote && doorBase != null && doorBase.getPressure(ForgeDirection.UNKNOWN) >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
            if(powered != doorBase.wasPowered) {
                doorBase.wasPowered = powered;
                doorBase.setOpening(powered);
            }
        }
    }

    private TileEntityPneumaticDoorBase getDoorBase(World world, int x, int y, int z){
        if(world.getBlock(x, y, z) != this) return null;
        int meta = world.getBlockMetadata(x, y, z);
        if(meta < 6) {
            return getDoorBase(world, x, y + 1, z);
        } else {
            ForgeDirection dir = ForgeDirection.getOrientation(meta % 6);
            TileEntity te1 = world.getTileEntity(x + dir.getRotation(ForgeDirection.UP).offsetX, y, z + dir.getRotation(ForgeDirection.UP).offsetZ);
            if(te1 instanceof TileEntityPneumaticDoorBase) {
                TileEntityPneumaticDoorBase door = (TileEntityPneumaticDoorBase)te1;
                if(door.orientation == dir.getRotation(ForgeDirection.DOWN)) {
                    return door;
                }
            }
            TileEntity te2 = world.getTileEntity(x + dir.getRotation(ForgeDirection.DOWN).offsetX, y, z + dir.getRotation(ForgeDirection.DOWN).offsetZ);
            if(te2 instanceof TileEntityPneumaticDoorBase) {
                TileEntityPneumaticDoorBase door = (TileEntityPneumaticDoorBase)te2;
                if(door.orientation == dir.getRotation(ForgeDirection.UP)) {
                    return door;
                }
            }
            return null;
        }
    }
}
