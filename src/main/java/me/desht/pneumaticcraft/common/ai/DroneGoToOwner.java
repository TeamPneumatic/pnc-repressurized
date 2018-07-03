package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class DroneGoToOwner extends EntityAIBase {
    private final EntityDrone drone;

    public DroneGoToOwner(EntityDrone drone) {
        this.drone = drone;
    }

    @Override
    public boolean shouldExecute() {
        EntityPlayerMP owner = getOnlineOwner();
        if (owner == null) return false;

        Vec3d lookVec = owner.getLookVec().scale(2.0);
        double x = owner.posX + lookVec.x;
        double z = owner.posZ + lookVec.z;
        return drone.getDistanceSq(owner) > 6 && drone.getNavigator().tryMoveToXYZ(x, owner.posY, z, drone.getSpeed());
    }

    @Override
    public boolean shouldContinueExecuting() {
        EntityPlayerMP owner = getOnlineOwner();
        return owner != null && !drone.getNavigator().noPath() && drone.getDistanceSq(owner) > 6;
    }

    private EntityPlayerMP getOnlineOwner() {
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            if (player.getGameProfile().equals(drone.getFakePlayer().getGameProfile())) return player;
        }
        return null;
    }
}
