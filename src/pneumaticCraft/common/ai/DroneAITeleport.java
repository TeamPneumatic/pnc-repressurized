package pneumaticCraft.common.ai;

import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.ProgWidget;

public class DroneAITeleport extends DroneEntityAIGoToLocation{

    public DroneAITeleport(EntityDrone drone, double speed, ProgWidget gotoWidget){
        super(drone, speed, gotoWidget);
    }

    @Override
    public boolean shouldExecute(){
        EntityPathNavigateDrone navigator = (EntityPathNavigateDrone)drone.getNavigator();
        navigator.setForceTeleport(true);
        boolean result = super.shouldExecute();
        navigator.setForceTeleport(false);
        return result;
    }
}
