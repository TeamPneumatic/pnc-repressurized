package pneumaticCraft.common.entity.projectile;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class EntityVortex extends EntityThrowable{

    private int hitCounter = 0;
    private double oldMotionX;
    private double oldMotionY;
    private double oldMotionZ;

    public EntityVortex(World par1World){
        super(par1World);
    }

    public EntityVortex(World par1World, EntityLivingBase par2EntityLiving){
        super(par1World, par2EntityLiving);
    }

    public EntityVortex(World par1World, double par2, double par4, double par6){
        super(par1World, par2, par4, par6);
    }

    @Override
    protected void entityInit(){}

    @Override
    public void onUpdate(){
        oldMotionX = motionX;
        oldMotionY = motionY;
        oldMotionZ = motionZ;
        super.onUpdate();
        //blowOtherEntities();
        motionX *= 0.95D;// equal to the potion effect friction. 0.95F
        motionY *= 0.95D;
        motionZ *= 0.95D;
        if(motionX * motionX + motionY * motionY + motionZ * motionZ < 0.1D) {
            setDead();
        }
        if(!worldObj.isRemote) {
            int blockX = (int)Math.floor(posX);
            int blockY = (int)Math.floor(posY);
            int blockZ = (int)Math.floor(posZ);
            for(int i = 0; i < 7; i++) { // to 7 so the middle block will also trigger (with UNKNOWN direction)
                Block block = worldObj.getBlock(blockX + ForgeDirection.getOrientation(i).offsetX, blockY + ForgeDirection.getOrientation(i).offsetY, blockZ + ForgeDirection.getOrientation(i).offsetZ);
                if(block instanceof IPlantable || block instanceof BlockLeaves) {
                    worldObj.func_147480_a(blockX + ForgeDirection.getOrientation(i).offsetX, blockY + ForgeDirection.getOrientation(i).offsetY, blockZ + ForgeDirection.getOrientation(i).offsetZ, true);
                }
            }
        }

    }

    /*   private void blowOtherEntities(){
           List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
           for(Entity e : list) {
               if(e != getThrower() || ticksExisted >= 5) {
                   if(e instanceof EntityPlayer || e instanceof EntityItem) {
                       e.motionX += motionX;
                       e.motionY += motionY;
                       e.motionZ += motionZ;
                   }
               }
           }
       }*/

    @Override
    public float getGravityVelocity(){
        return 0;
    }

    @Override
    protected void onImpact(MovingObjectPosition objectPosition){
        if(objectPosition.entityHit != null) {
            Entity entity = objectPosition.entityHit;
            entity.motionX += motionX;
            entity.motionY += motionY;
            entity.motionZ += motionZ;
            if(!entity.worldObj.isRemote && entity instanceof IShearable) {
                IShearable shearable = (IShearable)entity;
                int x = (int)Math.floor(posX);
                int y = (int)Math.floor(posY);
                int z = (int)Math.floor(posZ);
                if(shearable.isShearable(null, worldObj, x, y, z)) {
                    List<ItemStack> drops = shearable.onSheared(null, worldObj, x, y, z, 0);
                    for(ItemStack stack : drops) {
                        PneumaticCraftUtils.dropItemOnGround(stack, worldObj, entity.posX, entity.posY, entity.posZ);
                    }
                }
            }

        } else {
            Block block = worldObj.getBlock(objectPosition.blockX, objectPosition.blockY, objectPosition.blockZ);
            if(block instanceof IPlantable || block instanceof BlockLeaves) {
                motionX = oldMotionX;
                motionY = oldMotionY;
                motionZ = oldMotionZ;
            } else {
                setDead();
            }
        }
        hitCounter++;
        if(hitCounter > 20) setDead();
    }
}
