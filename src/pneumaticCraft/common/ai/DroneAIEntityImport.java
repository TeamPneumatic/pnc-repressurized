package pneumaticCraft.common.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import pneumaticCraft.common.progwidgets.IProgWidget;

public class DroneAIEntityImport extends DroneEntityBase<IProgWidget, EntityLivingBase>{

    public DroneAIEntityImport(IDroneBase drone, IProgWidget progWidget){
        super(drone, progWidget);
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
