package pneumaticCraft.common.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.IProgWidget;

public class DroneAIEntityImport extends DroneEntityBase<IProgWidget, EntityLivingBase>{

    public DroneAIEntityImport(EntityDrone drone, double speed, IProgWidget progWidget){
        super(drone, speed, progWidget);
    }

    @Override
    protected boolean isEntityValid(Entity entity){
        return entity instanceof EntityLivingBase && drone.riddenByEntity == null;
    }

    @Override
    protected boolean doAction(){
        targetedEntity.mountEntity(drone);
        return false;
    }

}
