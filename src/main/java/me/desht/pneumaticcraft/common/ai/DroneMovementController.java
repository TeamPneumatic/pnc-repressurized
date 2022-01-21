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

import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.world.entity.ai.control.MoveControl;

public class DroneMovementController extends MoveControl {
    private final EntityDroneBase entity;
    private double x, y, z, speed;
    private int timeoutTimer;
    private int timeoutCounter;//counts the times the drone timed out.

    public DroneMovementController(EntityDroneBase par1EntityLiving) {
        super(par1EntityLiving);
        entity = par1EntityLiving;
        x = entity.getX();
        y = entity.getY();
        z = entity.getZ();
    }

    @Override
    public void setWantedPosition(double x, double y, double z, double speed) {
        double newY = y + 0.5 - 0.17;
        if (x != this.x || newY != this.y || z != this.z) {
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
    public void tick() {
        if (!(entity.getNavigation() instanceof EntityPathNavigateDrone)) {
            // this could be the case if the drone's path navigator has been replaced, e.g. if it's been picked
            // up by something, in which case just bail - nothing else to do here
            // https://github.com/TeamPneumatic/pnc-repressurized/issues/794
            return;
        }

        if (entity.isAccelerating()) {
            entity.setDeltaMovement(
                    Math.max(-speed, Math.min(speed, x - entity.getX())),
                    Math.max(-speed, Math.min(speed, y - entity.getY())),
                    Math.max(-speed, Math.min(speed, z - entity.getZ()))
            );

            EntityPathNavigateDrone navigator = (EntityPathNavigateDrone)entity.getNavigation();
            
            // When teleporting already, the drone stands still for a bit, so don't expect movement in this case.
            if (!navigator.isGoingToTeleport() && timeoutTimer++ > 40) {
                entity.getNavigation().stop();
                timeoutTimer = 0;
                timeoutCounter++;
                if (timeoutCounter > 1 && entity.isPathFinding()) {
                    // Teleport when after re-acquiring a new path, the drone still doesn't move.
                    navigator.teleport();
                }
            }
        }
    }

}
