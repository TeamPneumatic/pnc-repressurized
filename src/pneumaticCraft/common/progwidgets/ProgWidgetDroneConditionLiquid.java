package pneumaticCraft.common.progwidgets;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.lib.Textures;

public class ProgWidgetDroneConditionLiquid extends ProgWidgetDroneEvaluation{

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetLiquidFilter.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString(){
        return "droneConditionLiquid";
    }

    @Override
    protected int getCount(EntityDrone drone){
        return drone.getTank().getFluid() != null && ProgWidgetLiquidFilter.isLiquidValid(drone.getTank().getFluid().getFluid(), this, 0) ? drone.getTank().getFluidAmount() : 0;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_DRONE_LIQUID_INVENTORY;
    }

}
