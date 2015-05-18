package pneumaticCraft.common.ai;

import net.minecraft.entity.ai.EntityMoveHelper;
import pneumaticCraft.common.entity.living.EntityDroneBase;

public class DroneMoveHelper extends EntityMoveHelper{
    private final EntityDroneBase entity;
    private double x, y, z, speed;

    public DroneMoveHelper(EntityDroneBase par1EntityLiving){
        super(par1EntityLiving);
        entity = par1EntityLiving;
        x = entity.posX;
        y = entity.posY;
        z = entity.posZ;
    }

    @Override
    public void setMoveTo(double x, double y, double z, double speed){
        this.x = x;
        this.y = y + 0.5 - 0.17;
        this.z = z;
        this.speed = speed;
    }

    @Override
    public void onUpdateMoveHelper(){
        if(entity.isAccelerating()) {
            if(x - 0.1D > entity.posX) entity.motionX = speed;
            else if(x + 0.1D < entity.posX) entity.motionX = -speed;
            if(y - 0.1D > entity.posY) entity.motionY = speed;
            else if(y + 0.1D < entity.posY) entity.motionY = -speed;
            if(z - 0.1D > entity.posZ) entity.motionZ = speed;
            else if(z + 0.1D < entity.posZ) entity.motionZ = -speed;
        }
    }

}
