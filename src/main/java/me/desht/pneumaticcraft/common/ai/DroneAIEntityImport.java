package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;

public class DroneAIEntityImport extends DroneEntityBase<IProgWidget, Entity> {

    public DroneAIEntityImport(IDroneBase drone, IProgWidget progWidget) {
        super(drone, progWidget);
    }

    @Override
    protected boolean isEntityValid(Entity entity) {
        return drone.getCarryingEntities().isEmpty() &&
                (entity instanceof EntityLivingBase || entity instanceof EntityMinecart || entity instanceof EntityBoat);
    }

    @Override
    protected boolean doAction() {
        drone.setCarryingEntity(targetedEntity);
        return false;
    }

}
