package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.List;

public abstract class DroneEntityBase<W extends IEntityProvider, E extends Entity> extends Goal {
    protected final IDroneBase drone;
    protected final W progWidget;
    protected E targetedEntity;

    protected DroneEntityBase(IDroneBase drone, W progWidget) {
        this.drone = drone;
        setMutexFlags(EnumSet.allOf(Flag.class)); // so it won't run along with other AI tasks.
        this.progWidget = progWidget;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        List<Entity> pickableItems = progWidget.getValidEntities(drone.world());

        pickableItems.sort(new DistanceEntitySorter(drone));
        for (Entity ent : pickableItems) {
            if (ent != drone && isEntityValid(ent)) {
                if (drone.getPathNavigator().moveToEntity(ent)) {
                    //noinspection unchecked
                    targetedEntity = (E) ent;
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
    public boolean shouldContinueExecuting() {
        if (!targetedEntity.isAlive()) return false;
        if (new Vec3d(targetedEntity.posX, targetedEntity.posY, targetedEntity.posZ).squareDistanceTo(drone.getDronePos()) < 2.25) {
            return doAction();
        }
        return !drone.getPathNavigator().hasNoPath();
    }

    protected abstract boolean doAction();
}
