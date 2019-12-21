package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IEntityProvider;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TargetGoal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class DroneAINearestAttackableTarget extends TargetGoal {
    private final EntityDrone drone;
    private final ProgWidget widget;

    /**
     * Instance of EntityAINearestAttackableTargetSorter.
     */
    private final Sorter theNearestAttackableTargetSorter;

    private LivingEntity targetEntity;

    public DroneAINearestAttackableTarget(EntityDrone drone, int par3, boolean checkSight, ProgWidget widget) {
        this(drone, checkSight, false, widget);
    }

    public DroneAINearestAttackableTarget(EntityDrone drone, boolean checkSight, boolean easyTargetsOnly,
                                          ProgWidget widget) {
        super(drone, checkSight, easyTargetsOnly);
        this.drone = drone;
        this.widget = widget;
        theNearestAttackableTargetSorter = new Sorter(drone);
        setMutexFlags(EnumSet.of(Flag.TARGET));
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        if (drone.hasMinigun() && drone.getSlotForAmmo() < 0) {
            return false;
        }
        List<Entity> list = ((IEntityProvider) widget).getValidEntities(drone.world);
        list.sort(theNearestAttackableTargetSorter);
        for (Entity entity : list) {
            if (entity != goalOwner && entity instanceof LivingEntity) {
                targetEntity = (LivingEntity) entity;
                return true;
            }
        }
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        goalOwner.setAttackTarget(targetEntity);
        super.startExecuting();
    }

    // lifted from 1.12.2
    public static class Sorter implements Comparator<Entity> {
        private final Entity entity;

        Sorter(Entity entityIn) {
            this.entity = entityIn;
        }

        public int compare(Entity e1, Entity e2) {
            double d0 = this.entity.getDistanceSq(e1);
            double d1 = this.entity.getDistanceSq(e2);

            if (d0 < d1) {
                return -1;
            } else {
                return d0 > d1 ? 1 : 0;
            }
        }
    }
}
