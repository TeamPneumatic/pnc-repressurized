package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.lib.Textures;

public class ProgWidgetEntityCondition extends ProgWidgetCondition{

    @Override
    public String getWidgetString(){
        return "conditionEntity";
    }

    @Override
    public String getGuiTabText(){
        return "bla";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFFFFFFFF;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class, ProgWidgetString.class};
    }

    @Override
    protected DroneAIBlockCondition getEvaluator(EntityDrone drone, IProgWidget widget){
        return null;
    }

    @Override
    public IProgWidget getOutputWidget(EntityDrone drone, List<IProgWidget> allWidgets){
        List<Entity> entities = getEntitiesInArea(drone.worldObj);
        boolean result = getOperator() == Operator.EQUALS ? entities.size() == getRequiredCount() : entities.size() >= getRequiredCount();
        return ProgWidgetJump.jumpToLabel(allWidgets, this, result);
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_ENTITY;
    }

}
