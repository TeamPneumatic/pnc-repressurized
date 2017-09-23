package me.desht.pneumaticcraft.common.entity.projectile;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;

import java.util.List;

public class EntityVortex extends EntityThrowable {

    private int hitCounter = 0;
    private double oldMotionX;
    private double oldMotionY;
    private double oldMotionZ;

    public EntityVortex(World world) {
        super(world);
    }

    public EntityVortex(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityVortex(World world, double par2, double par4, double par6) {
        super(world, par2, par4, par6);
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        oldMotionX = motionX;
        oldMotionY = motionY;
        oldMotionZ = motionZ;
        super.onUpdate();
        //blowOtherEntities();
        motionX *= 0.95D;// equal to the potion effect friction. 0.95F
        motionY *= 0.95D;
        motionZ *= 0.95D;
        if (motionX * motionX + motionY * motionY + motionZ * motionZ < 0.1D) {
            setDead();
        }
        if (!world.isRemote) {
            BlockPos pos = new BlockPos(posX, posY, posZ);
            tryCutPlants(pos);
            for (EnumFacing dir : EnumFacing.VALUES) {
                tryCutPlants(pos.offset(dir));
            }
        }

    }

    private void tryCutPlants(BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof IPlantable || block instanceof BlockLeaves) {
            world.destroyBlock(pos, true);
        }
    }

    /*   private void blowOtherEntities(){
           List<Entity> list = getWorld().getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
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
    public float getGravityVelocity() {
        return 0;
    }

    @Override
    protected void onImpact(RayTraceResult objectPosition) {
        if (objectPosition.entityHit != null) {
            Entity entity = objectPosition.entityHit;
            entity.motionX += motionX;
            entity.motionY += motionY;
            entity.motionZ += motionZ;
            if (!entity.world.isRemote && entity instanceof IShearable) {
                IShearable shearable = (IShearable) entity;
                BlockPos pos = new BlockPos(posX, posY, posZ);
                if (shearable.isShearable(ItemStack.EMPTY, world, pos)) {
                    List<ItemStack> drops = shearable.onSheared(ItemStack.EMPTY, world, pos, 0);
                    for (ItemStack stack : drops) {
                        PneumaticCraftUtils.dropItemOnGround(stack, world, entity.posX, entity.posY, entity.posZ);
                    }
                }
            }

        } else {
            Block block = world.getBlockState(objectPosition.getBlockPos()).getBlock();
            if (block instanceof IPlantable || block instanceof BlockLeaves) {
                motionX = oldMotionX;
                motionY = oldMotionY;
                motionZ = oldMotionZ;
            } else {
                setDead();
            }
        }
        hitCounter++;
        if (hitCounter > 20) setDead();
    }
}
