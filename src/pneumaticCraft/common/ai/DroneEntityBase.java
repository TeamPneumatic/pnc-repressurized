package pneumaticCraft.common.ai;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;

public abstract class DroneEntityBase<Widget extends IProgWidget, E extends Entity> extends EntityAIBase{
    protected final EntityDrone drone;
    private final double speed;
    protected final Widget widget;
    protected E targetedEntity;

    public DroneEntityBase(EntityDrone drone, double speed, Widget widget){
        this.drone = drone;
        this.speed = speed;
        setMutexBits(63);//binary 111111, so it won't run along with other AI tasks.
        this.widget = widget;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute(){
        List<Entity> pickableItems = ProgWidgetAreaItemBase.getEntitiesInArea(drone.worldObj, widget);

        Collections.sort(pickableItems, new EntityAINearestAttackableTarget.Sorter(drone));
        for(Entity ent : pickableItems) {
            if(ent != drone && isEntityValid(ent)) {
                if(drone.getNavigator().tryMoveToEntityLiving(ent, speed) || ((EntityPathNavigateDrone)drone.getNavigator()).isGoingToTeleport()) {
                    targetedEntity = (E)ent;
                    return true;
                }
            }
        }
        return false; // 

    }

    protected abstract boolean isEntityValid(Entity entity);

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting(){
        if(targetedEntity.isDead) return false;
        if(targetedEntity.getDistanceToEntity(drone) < 1.5) {
            return doAction();
        }
        return !drone.getNavigator().noPath();
    }

    protected abstract boolean doAction();
}
