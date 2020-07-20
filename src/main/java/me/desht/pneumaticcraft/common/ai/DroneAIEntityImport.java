package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.config.PNCConfig.Common.General;
import me.desht.pneumaticcraft.common.progwidgets.IEntityProvider;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class DroneAIEntityImport extends DroneEntityBase<IEntityProvider, Entity> {

    public DroneAIEntityImport(IDroneBase drone, IEntityProvider progWidget) {
        super(drone, progWidget);
    }

    @Override
    protected boolean isEntityValid(Entity entity) {
        if (entity instanceof LivingEntity || entity instanceof AbstractMinecartEntity || entity instanceof BoatEntity) {
            return drone.getCarryingEntities().isEmpty();
        } else if (General.dronesCanImportXPOrbs && entity instanceof ExperienceOrbEntity) {
            return drone.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                    .map(handler -> PneumaticCraftUtils.fillTankWithOrb(handler, (ExperienceOrbEntity) entity, FluidAction.SIMULATE))
                    .orElse(false);
        }
        return false;
    }

    @Override
    protected boolean doAction() {
        if (General.dronesCanImportXPOrbs && targetedEntity instanceof ExperienceOrbEntity) {
            drone.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(handler -> {
                if (PneumaticCraftUtils.fillTankWithOrb(handler, (ExperienceOrbEntity) targetedEntity, FluidAction.EXECUTE)) {
                    targetedEntity.remove();
                }
            });
        } else {
            drone.setCarryingEntity(targetedEntity);
        }
        return false;
    }
}
