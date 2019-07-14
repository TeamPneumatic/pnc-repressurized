package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ProgWidgetDroneConditionEnergy extends ProgWidgetDroneCondition {
    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_DRONE_RF;
    }

    @Override
    public Class[] getParameters() {
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    public String getWidgetString() {
        return "droneConditionRF";
    }

    @Override
    protected int getCount(IDroneBase drone, IProgWidget widget) {
        return drone.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }
}
