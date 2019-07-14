package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.IEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;

public class DroneAIEntityImport extends DroneEntityBase<IEntityProvider, Entity> {

    public DroneAIEntityImport(IDroneBase drone, IEntityProvider progWidget) {
        super(drone, progWidget);
    }

    @Override
    protected boolean isEntityValid(Entity entity) {
        return drone.getCarryingEntities().isEmpty() &&
                (entity instanceof LivingEntity || entity instanceof AbstractMinecartEntity || entity instanceof BoatEntity);
    }

    @Override
    protected boolean doAction() {
        drone.setCarryingEntity(targetedEntity);
        return false;
    }

}
