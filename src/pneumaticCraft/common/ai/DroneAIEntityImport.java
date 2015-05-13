package pneumaticCraft.common.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import pneumaticCraft.api.drone.IDrone;
import pneumaticCraft.common.progwidgets.IProgWidget;

public class DroneAIEntityImport extends DroneEntityBase<IProgWidget, EntityLivingBase>{

    public DroneAIEntityImport(IDrone drone, double speed, IProgWidget progWidget){
        super(drone, speed, progWidget);
    }

    @Override
    protected boolean isEntityValid(Entity entity){
        return entity instanceof EntityLivingBase && drone.getCarryingEntity() == null;
    }

    @Override
    protected boolean doAction(){
        drone.setCarryingEntity(targetedEntity);
        return false;
    }

}
