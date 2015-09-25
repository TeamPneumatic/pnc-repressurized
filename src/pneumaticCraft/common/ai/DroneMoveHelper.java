package pneumaticCraft.common.ai;

import net.minecraft.entity.ai.EntityMoveHelper;
import pneumaticCraft.common.entity.living.EntityDroneBase;

public class DroneMoveHelper extends EntityMoveHelper{
    private final EntityDroneBase entity;
    private double x, y, z, speed;
    private int timeoutTimer;
    private int timeoutCounter;//counts the times the drone timed out.

    public DroneMoveHelper(EntityDroneBase par1EntityLiving){
        super(par1EntityLiving);
        entity = par1EntityLiving;
        x = entity.posX;
        y = entity.posY;
        z = entity.posZ;
    }

    @Override
    public void setMoveTo(double x, double y, double z, double speed){

        double newY = y + 0.5 - 0.17;
        if(x != this.x || newY != this.y || z != this.z) {
            this.x = x;
            this.y = newY;
            this.z = z;
            timeoutTimer = 0;
        } else {
            timeoutCounter = 0;
        }
        this.speed = speed;
    }

    @Override
    public void onUpdateMoveHelper(){
        if(entity.isAccelerating()) {
            entity.motionX = Math.max(-speed, Math.min(speed, x - entity.posX));
            entity.motionY = Math.max(-speed, Math.min(speed, y - entity.posY));
            entity.motionZ = Math.max(-speed, Math.min(speed, z - entity.posZ));

            if(timeoutTimer++ > 40) {
                entity.getNavigator().clearPathEntity();
                timeoutTimer = 0;
                timeoutCounter++;
                if(timeoutCounter > 1 && entity.hasPath()) { //Teleport when after re-acquiring a new path, the drone still doesn't move.
                    ((EntityPathNavigateDrone)entity.getNavigator()).teleport();
                }
            }
        }
    }

}
