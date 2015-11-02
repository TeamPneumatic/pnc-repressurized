package pneumaticCraft.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.render.block.RenderElevatorFrame;
import pneumaticCraft.common.tileentity.TileEntityElevatorBase;
import pneumaticCraft.common.tileentity.TileEntityElevatorFrame;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockElevatorFrame extends BlockPneumaticCraftModeled{

    public BlockElevatorFrame(Material par2Material){
        super(par2Material);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z){
        super.onBlockAdded(world, x, y, z);
        TileEntityElevatorBase elevatorBase = getElevatorTE(world, x, y, z);
        if(elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass(){
        return TileEntityElevatorFrame.class;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 origin, Vec3 direction){
        if(world.isRemote) {
            ItemStack playerStack = PneumaticCraft.proxy.getPlayer().getCurrentEquippedItem();
            if(playerStack != null && playerStack.getItem() == Item.getItemFromBlock(this)) {
                return super.collisionRayTrace(world, x, y, z, origin, direction);
            }
        }
        boolean frameXPos = world.getBlock(x + 1, y, z) == Blockss.elevatorFrame;
        boolean frameXNeg = world.getBlock(x - 1, y, z) == Blockss.elevatorFrame;
        boolean frameZPos = world.getBlock(x, y, z + 1) == Blockss.elevatorFrame;
        boolean frameZNeg = world.getBlock(x, y, z - 1) == Blockss.elevatorFrame;

        boolean isColliding = false;

        if(!frameXNeg && !frameZNeg) {
            setBlockBounds(0, 0, 0, 2 / 16F, 1, 2 / 16F);
            if(super.collisionRayTrace(world, x, y, z, origin, direction) != null) isColliding = true;
        }
        if(!frameXNeg && !frameZPos) {
            setBlockBounds(0, 0, 14 / 16F, 2 / 16F, 1, 1);
            if(super.collisionRayTrace(world, x, y, z, origin, direction) != null) isColliding = true;
        }
        if(!frameXPos && !frameZPos) {
            setBlockBounds(14 / 16F, 0, 14 / 16F, 1, 1, 1);
            if(super.collisionRayTrace(world, x, y, z, origin, direction) != null) isColliding = true;
        }
        if(!frameXPos && !frameZNeg) {
            setBlockBounds(14 / 16F, 0, 0, 1, 1, 2 / 16F);
            if(super.collisionRayTrace(world, x, y, z, origin, direction) != null) isColliding = true;
        }

        setBlockBounds(0, 0, 0, 1, 1, 1);
        return isColliding ? super.collisionRayTrace(world, x, y, z, origin, direction) : null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType(){
        return PneumaticCraft.proxy.getRenderIdForRenderer(RenderElevatorFrame.class);
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this
     * box can change after the pool has been cleared to be reused)
     */
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4){
        float blockHeight = getElevatorBlockHeight(par1World, par2, par3, par4);
        if(blockHeight > 0F) {
            // this.setBlockBounds(0, 0, 0, 1, blockHeight, 1);
            // return super.getCollisionBoundingBoxFromPool(par1World, par2,
            // par3, par4);
            return AxisAlignedBB.getBoundingBox(par2, par3, par4, par2 + 1, par3 + blockHeight, par4 + 1);
        } else {
            return null;
        }
        // return null;
    }

    /**
     * Triggered whenever an entity collides with this block (enters into the block). Args: world, x, y, z, entity
     */
    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity){
        //  float blockHeight = getElevatorBlockHeight(world, x, y, z);
        //   if(blockHeight > 0) {
        // if(entity.posY < y + blockHeight) {
        //     entity.setPosition(entity.posX, y + blockHeight + 2, entity.posZ);
        TileEntityElevatorBase te = getElevatorTE(world, x, y, z);
        if(te != null && te.oldExtension != te.extension) {
            entity.setPosition(entity.posX, te.yCoord + te.extension + (double)entity.yOffset + entity.ySize + 1, entity.posZ);
        }
        entity.fallDistance = 0;
        //}
        //   }
    }

    public static TileEntityElevatorBase getElevatorTE(IBlockAccess world, int x, int y, int z){
        int i = 0;
        while(true) {
            i--;
            if(world.getBlock(x, y + i, z) == Blockss.elevatorBase) break;
            if(world.getBlock(x, y + i, z) != Blockss.elevatorFrame || y <= 0) return null;
        }
        return (TileEntityElevatorBase)world.getTileEntity(x, y + i, z);
    }

    private float getElevatorBlockHeight(World world, int x, int y, int z){
        TileEntityElevatorBase te = getElevatorTE(world, x, y, z);
        if(te == null) return 0F;
        float blockHeight = te.extension - (y - te.yCoord) + 1;
        // System.out.println("blockHeight (" + x + ", " + y + ", " + z + "): " + blockHeight);
        // + blockHeight);
        if(blockHeight < 0F) return 0F;
        if(blockHeight > 1F) return 1F;
        return blockHeight;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block){
        int blockMeta = world.getBlockMetadata(x, y, z);
        if(blockMeta == 0 && world.getBlock(x, y - 1, z) == Blockss.elevatorBase) {
            world.setBlockMetadataWithNotify(x, y, z, 1, 2);
        } else if(blockMeta == 1) {
            world.setBlockMetadataWithNotify(x, y, z, 0, 2);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta){
        TileEntityElevatorBase elevatorBase = getElevatorTE(world, x, y, z);
        if(elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
