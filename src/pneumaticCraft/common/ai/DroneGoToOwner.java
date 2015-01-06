package pneumaticCraft.common.ai;

import java.util.List;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import pneumaticCraft.common.entity.living.EntityDrone;

public class DroneGoToOwner extends EntityAIBase{
    private final EntityDrone drone;
    public boolean isExecuting;

    public DroneGoToOwner(EntityDrone drone){
        this.drone = drone;
    }

    @Override
    public boolean shouldExecute(){
        EntityPlayer owner = getOnlineOwner();
        return isExecuting = owner != null && drone.getNavigator().tryMoveToEntityLiving(owner, drone.getSpeed());
    }

    @Override
    public boolean continueExecuting(){
        return isExecuting = getOnlineOwner() != null && !drone.getNavigator().noPath();
    }

    private EntityPlayer getOnlineOwner(){
        for(EntityPlayer player : (List<EntityPlayer>)MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if(player.getGameProfile().equals(drone.getFakePlayer().getGameProfile())) return player;
        }
        return null;
    }
}
