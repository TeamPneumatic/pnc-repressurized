package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class DroneAIEntityImport extends DroneEntityBase<IProgWidget, EntityLivingBase> {

    public DroneAIEntityImport(IDroneBase drone, IProgWidget progWidget) {
        super(drone, progWidget);
    }

    @Override
    protected boolean isEntityValid(Entity entity) {
        return entity instanceof EntityLivingBase && drone.getCarryingEntities().isEmpty();
    }

    @Override
    protected boolean doAction() {
        drone.setCarryingEntity(targetedEntity);
        return false;
    }

}
