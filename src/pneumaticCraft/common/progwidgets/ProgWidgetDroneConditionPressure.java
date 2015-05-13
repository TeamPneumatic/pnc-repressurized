package pneumaticCraft.common.progwidgets;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.api.drone.IDrone;
import pneumaticCraft.lib.Textures;

public class ProgWidgetDroneConditionPressure extends ProgWidgetDroneEvaluation{

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    public String getWidgetString(){
        return "droneConditionPressure";
    }

    @Override
    protected int getCount(IDrone drone, IProgWidget widget){
        return (int)drone.getPressure(null);
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_DRONE_PRESSURE;
    }

}
