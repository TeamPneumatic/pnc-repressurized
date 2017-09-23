package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class ProgWidgetDroneConditionLiquid extends ProgWidgetDroneEvaluation implements ILiquidFiltered {

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
        return drone.getTank().getFluid() != null && ((ILiquidFiltered) widget).isFluidValid(drone.getTank().getFluid().getFluid()) ? drone.getTank().getFluidAmount() : 0;
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
