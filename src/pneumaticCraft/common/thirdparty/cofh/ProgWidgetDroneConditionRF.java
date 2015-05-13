package pneumaticCraft.common.thirdparty.cofh;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.api.drone.IDrone;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetDroneEvaluation;
import pneumaticCraft.common.progwidgets.ProgWidgetString;
import pneumaticCraft.lib.Textures;

public class ProgWidgetDroneConditionRF extends ProgWidgetDroneEvaluation{

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    public String getWidgetString(){
        return "droneConditionRF";
    }

    @Override
    protected int getCount(IDrone drone, IProgWidget widget){
        return CoFHCore.getEnergyStorage(drone).getEnergyStored();
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_DRONE_RF;
    }

}
