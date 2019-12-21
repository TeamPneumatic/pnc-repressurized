package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;

public class ProgWidgetDroneConditionFluid extends ProgWidgetDroneCondition implements ILiquidFiltered {

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetLiquidFilter.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString() {
        return "droneConditionLiquid";
    }

    @Override
    protected int getCount(IDroneBase drone, IProgWidget widget) {
        return drone.getFluidTank().getFluid() != null && ((ILiquidFiltered) widget).isFluidValid(drone.getFluidTank().getFluid().getFluid()) ? drone.getFluidTank().getFluidAmount() : 0;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_DRONE_LIQUID_INVENTORY;
    }

    @Override
    public boolean isFluidValid(Fluid fluid) {
        return ProgWidgetLiquidFilter.isLiquidValid(fluid, this, 0);
    }

}
