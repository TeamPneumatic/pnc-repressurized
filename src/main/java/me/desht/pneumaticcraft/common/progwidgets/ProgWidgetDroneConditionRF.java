package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ProgWidgetDroneConditionRF extends ProgWidgetDroneEvaluation {
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
        if (!drone.hasCapability(CapabilityEnergy.ENERGY, null)) return 0;
        IEnergyStorage storage = drone.getCapability(CapabilityEnergy.ENERGY, null);
        return storage.getEnergyStored();
    }
}
