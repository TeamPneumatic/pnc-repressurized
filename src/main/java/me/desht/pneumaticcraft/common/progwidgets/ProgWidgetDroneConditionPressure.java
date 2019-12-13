package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.capabilities.CapabilityAirHandler;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;

public class ProgWidgetDroneConditionPressure extends ProgWidgetDroneCondition {

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    public String getWidgetString() {
        return "droneConditionPressure";
    }

    @Override
    protected int getCount(IDroneBase drone, IProgWidget widget) {
        return drone.getCapability(CapabilityAirHandler.AIR_HANDLER_CAPABILITY)
                .map(h -> (int) h.getPressure())
                .orElseThrow(IllegalStateException::new);
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_DRONE_PRESSURE;
    }

}
