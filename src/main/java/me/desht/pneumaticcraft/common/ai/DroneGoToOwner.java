package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class DroneGoToOwner extends Goal {
    private final EntityDrone drone;

    public DroneGoToOwner(EntityDrone drone) {
        this.drone = drone;
    }

    @Override
    public boolean shouldExecute() {
        ServerPlayerEntity owner = getOnlineOwner();
        if (owner == null) return false;

        Vec3d lookVec = owner.getLookVec().scale(2.0);
        double x = owner.getPosX() + lookVec.x;
        double z = owner.getPosZ() + lookVec.z;
        return drone.getDistanceSq(owner) > 6 && drone.getNavigator().tryMoveToXYZ(x, owner.getPosY(), z, drone.getSpeed());
    }

    @Override
    public boolean shouldContinueExecuting() {
        ServerPlayerEntity owner = getOnlineOwner();
        return owner != null && !drone.getNavigator().noPath() && drone.getDistanceSq(owner) > 6;
    }

    private ServerPlayerEntity getOnlineOwner() {
        for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (player.getGameProfile().equals(drone.getFakePlayer().getGameProfile())) return player;
        }
        return null;
    }
}
