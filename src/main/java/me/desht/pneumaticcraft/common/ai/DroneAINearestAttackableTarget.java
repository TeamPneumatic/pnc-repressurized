/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.IEntityProvider;
import me.desht.pneumaticcraft.common.progwidgets.IMaxActions;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class DroneAINearestAttackableTarget extends TargetGoal {
    private final EntityDrone drone;
    private final ProgWidget widget;

    /**
     * Instance of EntityAINearestAttackableTargetSorter.
     */
    private final DistanceSorter distanceSorter;

    private LivingEntity targetEntity;

    public DroneAINearestAttackableTarget(EntityDrone drone, boolean checkSight, ProgWidget widget) {
        this(drone, checkSight, false, widget);
    }

    public DroneAINearestAttackableTarget(EntityDrone drone, boolean checkSight, boolean easyTargetsOnly,
                                          ProgWidget widget) {
        super(drone, checkSight, easyTargetsOnly);
        this.drone = drone;
        this.widget = widget;
        distanceSorter = new DistanceSorter(drone);
        setFlags(EnumSet.of(Flag.TARGET));
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean canUse() {
        if (drone.hasMinigun() && drone.getSlotForAmmo() < 0) {
            return false;
        }
        if (widget instanceof IMaxActions) {
            IMaxActions m = (IMaxActions) widget;
            if (m.useMaxActions() && drone.getAttackCount() >= m.getMaxActions()) {
                return false;
            }
        }

        List<Entity> list = ((IEntityProvider) widget).getValidEntities(drone.level);
        list.sort(distanceSorter);
        for (Entity entity : list) {
            if (entity.isAlive() && entity != mob && entity instanceof LivingEntity && !shouldIgnore(entity)) {
                targetEntity = (LivingEntity) entity;
                return true;
            }
        }
        return false;
    }

    private boolean shouldIgnore(Entity entity) {
        return entity.isSpectator() || entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void start() {
        mob.setTarget(targetEntity);
        super.start();
    }

    public static class DistanceSorter implements Comparator<Entity> {
        private final Entity entity;

        DistanceSorter(Entity entityIn) {
            this.entity = entityIn;
        }

        @Override
        public int compare(Entity e1, Entity e2) {
            return Double.compare(entity.distanceToSqr(e1), entity.distanceToSqr(e2));
        }
    }
}
