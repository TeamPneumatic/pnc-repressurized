package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

public class DroneGoToOwner extends Goal {
    private final EntityDrone drone;

    public DroneGoToOwner(EntityDrone drone) {
        this.drone = drone;
    }

    @Override
    public boolean canUse() {
        ServerPlayerEntity owner = getOnlineOwner();
        if (owner == null) return false;

        Vector3d lookVec = owner.getLookAngle().scale(2.0);
        double x = owner.getX() + lookVec.x;
        double z = owner.getZ() + lookVec.z;
        return drone.distanceToSqr(owner) > 6 && drone.getNavigation().moveTo(x, owner.getY(), z, drone.getDroneSpeed());
    }

    @Override
    public boolean canContinueToUse() {
        ServerPlayerEntity owner = getOnlineOwner();
        return owner != null && !drone.getNavigation().isDone() && drone.distanceToSqr(owner) > 6;
    }

    private ServerPlayerEntity getOnlineOwner() {
        if (drone.level.getServer() == null) return null;
        return drone.level.getServer().getPlayerList().getPlayer(drone.getOwnerUUID());
    }
}
