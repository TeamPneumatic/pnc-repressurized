package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.entity.ai.EntityAIBase;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.lib.Log;

public abstract class ProgWidgetCondition extends ProgWidgetAreaItemBase implements ICondition{

    private DroneAIBlockCondition evaluator;
    public boolean isAndFunction;

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        evaluator = getEvaluator(drone, widget);
        return evaluator;
    }

    @Override
    public abstract Class<? extends IProgWidget>[] getParameters();

    protected abstract DroneAIBlockCondition getEvaluator(EntityDrone drone, IProgWidget widget);

    @Override
    public IProgWidget getOutputWidget(List<IProgWidget> allWidgets){
        if(evaluator != null) {
            return ProgWidgetJump.jumpToLabel(allWidgets, this, evaluator.getResult());
        } else {
            Log.error("Shouldn't be happening!");
            return super.getOutputWidget(allWidgets);
        }
    }

    @Override
    public boolean isAndFunction(){
        return isAndFunction;
    }

}
