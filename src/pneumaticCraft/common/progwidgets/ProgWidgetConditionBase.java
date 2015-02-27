package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.List;

import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;

public abstract class ProgWidgetConditionBase extends ProgWidget implements IJump{

    @Override
    public boolean hasStepInput(){
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return null;
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.MEDIUM;
    }

    @Override
    public List<String> getPossibleJumpLocations(){
        ProgWidgetString textWidget = (ProgWidgetString)getConnectedParameters()[getParameters().length - 1];
        ProgWidgetString textWidget2 = (ProgWidgetString)getConnectedParameters()[getParameters().length * 2 - 1];
        List<String> locations = new ArrayList<String>();
        if(textWidget != null) locations.add(textWidget.string);
        if(textWidget2 != null) locations.add(textWidget2.string);
        return locations;
    }

    @Override
    public IProgWidget getOutputWidget(EntityDrone drone, List<IProgWidget> allWidgets){
        return ProgWidgetJump.jumpToLabel(allWidgets, this, evaluate(drone, this));
    }

    public abstract boolean evaluate(EntityDrone drone, IProgWidget widget);

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.LIGHTNING_PLANT_DAMAGE;
    }

}
