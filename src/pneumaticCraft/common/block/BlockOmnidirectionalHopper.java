package pneumaticCraft.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.tileentity.TileEntityOmnidirectionalHopper;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class BlockOmnidirectionalHopper extends BlockPneumaticCraftModeled{

    public BlockOmnidirectionalHopper(Material par2Material){
        super(par2Material);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityOmnidirectionalHopper.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.OMNIDIRECTIONAL_HOPPER;
    }

    /**
     * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z, side, hitX, hitY, hitZ, block metadata
     */
    @Override
    public int onBlockPlaced(World par1World, int par2, int par3, int par4, int par5, float par6, float par7, float par8, int par9){
        return Facing.oppositeSide[par5];
    }

    @Override
    public void onBlockPlacedBy(World world, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack){
        ((TileEntityOmnidirectionalHopper)world.getTileEntity(par2, par3, par4)).setDirection(PneumaticCraftUtils.getDirectionFacing(par5EntityLiving, true).getOpposite());
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
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityOmnidirectionalHopper) {
            TileEntityOmnidirectionalHopper teOh = (TileEntityOmnidirectionalHopper)te;
            if(player != null && player.isSneaking()) {
                int newMeta = (world.getBlockMetadata(x, y, z) + 1) % 6;
                if(newMeta == teOh.getDirection().ordinal()) newMeta = (newMeta + 1) % 6;
                world.setBlockMetadataWithNotify(x, y, z, newMeta, 3);
            } else {
                int newRotation = (teOh.getDirection().ordinal() + 1) % 6;
                if(newRotation == world.getBlockMetadata(x, y, z)) newRotation = (newRotation + 1) % 6;
                teOh.setDirection(ForgeDirection.getOrientation(newRotation));
            }
            return true;
        }
        return false;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 origin, Vec3 direction){
        TileEntity te = world.getTileEntity(x, y, z);
        if(te instanceof TileEntityOmnidirectionalHopper) {
            ForgeDirection o = ((TileEntityOmnidirectionalHopper)te).getDirection();
            boolean isColliding = false;
            setBlockBounds(o.offsetX == 1 ? 10 / 16F : 0, o.offsetY == 1 ? 10 / 16F : 0, o.offsetZ == 1 ? 10 / 16F : 0, o.offsetX == -1 ? 6 / 16F : 1, o.offsetY == -1 ? 6 / 16F : 1, o.offsetZ == -1 ? 6 / 16F : 1);
            if(super.collisionRayTrace(world, x, y, z, origin, direction) != null) isColliding = true;
            setBlockBounds(4 / 16F, 4 / 16F, 4 / 16F, 12 / 16F, 12 / 16F, 12 / 16F);
            if(super.collisionRayTrace(world, x, y, z, origin, direction) != null) isColliding = true;
            setBlockBounds(0, 0, 0, 1, 1, 1);
            return isColliding ? super.collisionRayTrace(world, x, y, z, origin, direction) : null;
        }
        return null;
    }
}
