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

package me.desht.pneumaticcraft.common.drone.ai;

import me.desht.pneumaticcraft.common.drone.progwidgets.IEntityProvider;
import me.desht.pneumaticcraft.common.drone.progwidgets.IMaxActions;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidget;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class DroneAINearestAttackableTarget extends TargetGoal {
    private final DroneEntity drone;
    private final ProgWidget widget;
    private final DistanceSorter distanceSorter;

    private LivingEntity targetEntity;

    public DroneAINearestAttackableTarget(DroneEntity drone, boolean mustSee, ProgWidget widget) {
        super(drone, mustSee, false);
        this.drone = drone;
        this.widget = widget;
        this.distanceSorter = new DistanceSorter(drone);
        setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (drone.hasMinigun() && drone.getSlotForAmmo() < 0) {
            return false;
        }
        if (widget instanceof IMaxActions m && m.useMaxActions() && drone.getAttackCount() >= m.getMaxActions()) {
            return false;
        }
        if (!(widget instanceof IEntityProvider provider)) {
            return false;
        }

        List<Entity> list = provider.getValidEntities(drone.level());
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
        return entity.isSpectator()
                || entity instanceof Player && ((Player) entity).isCreative()
                || mustSee && !mob.getSensing().hasLineOfSight(entity);
    }

    @Override
    public void start() {
        mob.setTarget(targetEntity);
        super.start();
    }

    private record DistanceSorter(Entity entity) implements Comparator<Entity> {
        @Override
        public int compare(Entity e1, Entity e2) {
            return Double.compare(entity.distanceToSqr(e1), entity.distanceToSqr(e2));
        }
    }
}
