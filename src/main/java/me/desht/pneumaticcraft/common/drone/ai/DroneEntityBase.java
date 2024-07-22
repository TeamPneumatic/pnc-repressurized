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

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.drone.progwidgets.IEntityProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

public abstract class DroneEntityBase<W extends IEntityProvider, E extends Entity> extends Goal {
    protected final IDrone drone;
    protected final W progWidget;
    protected E targetedEntity;

    protected DroneEntityBase(IDrone drone, W progWidget) {
        this.drone = drone;
        setFlags(EnumSet.allOf(Flag.class)); // so it won't run along with other AI tasks.
        this.progWidget = progWidget;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean canUse() {
        List<Entity> pickableItems = progWidget.getValidEntities(drone.getDroneLevel());

        pickableItems.sort(new DistanceEntitySorter(drone));
        for (Entity ent : pickableItems) {
            if (ent != drone && isEntityValid(ent)) {
                if (drone.getPathNavigator().moveToEntity(ent)) {
                    //noinspection unchecked
                    targetedEntity = (E) ent;
                    return true;
                } else {
                    drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.general.debug.cantNavigate");
                }
            }
        }
        return false;

    }

    protected abstract boolean isEntityValid(Entity entity);

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean canContinueToUse() {
        if (!targetedEntity.isAlive()) return false;
        if (targetedEntity.position().distanceToSqr(drone.getDronePos()) < 2.25) {
            return doAction();
        }
        return !drone.getPathNavigator().hasNoPath();
    }

    protected abstract boolean doAction();
}
