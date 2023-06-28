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

import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class DroneGoToOwner extends Goal {
    private final DroneEntity drone;

    public DroneGoToOwner(DroneEntity drone) {
        this.drone = drone;
    }

    @Override
    public boolean canUse() {
        ServerPlayer owner = getOnlineOwner();
        if (owner == null) return false;

        Vec3 lookVec = owner.getLookAngle().scale(2.0);
        double x = owner.getX() + lookVec.x;
        double z = owner.getZ() + lookVec.z;
        return drone.distanceToSqr(owner) > 6 && drone.getNavigation().moveTo(x, owner.getY(), z, drone.getDroneSpeed());
    }

    @Override
    public boolean canContinueToUse() {
        ServerPlayer owner = getOnlineOwner();
        return owner != null && !drone.getNavigation().isDone() && drone.distanceToSqr(owner) > 6;
    }

    private ServerPlayer getOnlineOwner() {
        if (drone.level().getServer() == null) return null;
        return drone.level().getServer().getPlayerList().getPlayer(drone.getOwnerUUID());
    }
}
