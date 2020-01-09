package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ProgWidgetDroneConditionFluid extends ProgWidgetDroneCondition implements ILiquidFiltered {

    public ProgWidgetDroneConditionFluid() {
        super(ModProgWidgets.DRONE_CONDITION_LIQUID);
    }

    @Override
    public List<ProgWidgetType> getParameters() {
        return ImmutableList.of(ModProgWidgets.LIQUID_FILTER, ModProgWidgets.TEXT);
    }

    @Override
    protected int getCount(IDroneBase drone, IProgWidget widget) {
        return !drone.getFluidTank().getFluid().isEmpty()
                && ((ILiquidFiltered) widget).isFluidValid(drone.getFluidTank().getFluid().getFluid()) ? drone.getFluidTank().getFluidAmount() : 0;
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
