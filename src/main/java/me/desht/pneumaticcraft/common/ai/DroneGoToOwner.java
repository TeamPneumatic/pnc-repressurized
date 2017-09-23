package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class DroneGoToOwner extends EntityAIBase {
    private final EntityDrone drone;
    public boolean isExecuting;

    public DroneGoToOwner(EntityDrone drone) {
        this.drone = drone;
    }

    @Override
    public boolean shouldExecute() {
        EntityPlayerMP owner = getOnlineOwner();
        return isExecuting = owner != null && drone.getNavigator().tryMoveToEntityLiving(owner, drone.getSpeed());
    }

    @Override
    public boolean shouldContinueExecuting() {
        return isExecuting = getOnlineOwner() != null && !drone.getNavigator().noPath();
    }

    private EntityPlayerMP getOnlineOwner() {
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            if (player.getGameProfile().equals(drone.getFakePlayer().getGameProfile())) return player;
        }
        return null;
    }
}
