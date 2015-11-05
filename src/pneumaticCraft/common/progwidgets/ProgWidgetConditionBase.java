package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.List;

import pneumaticCraft.common.ai.IDroneBase;
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
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets){
        super.addErrors(curInfo, widgets);
        IProgWidget widget = getConnectedParameters()[getParameters().length - 1];
        IProgWidget widget2 = getConnectedParameters()[getParameters().length * 2 - 1];
        if(widget == null && widget2 == null) {
            curInfo.add("gui.progWidget.condition.error.noFlowControl");
        } else if(widget != null && !(widget instanceof ProgWidgetString) || widget2 != null && !(widget2 instanceof ProgWidgetString)) {
            curInfo.add("gui.progWidget.condition.error.shouldConnectTextPieces");
        }
    }

    @Override
    public List<String> getPossibleJumpLocations(){
        IProgWidget widget = getConnectedParameters()[getParameters().length - 1];
        IProgWidget widget2 = getConnectedParameters()[getParameters().length * 2 - 1];
        ProgWidgetString textWidget = widget != null ? (ProgWidgetString)widget : null;
        ProgWidgetString textWidget2 = widget2 != null ? (ProgWidgetString)widget2 : null;
        List<String> locations = new ArrayList<String>();
        if(textWidget != null) locations.add(textWidget.string);
        if(textWidget2 != null) locations.add(textWidget2.string);
        return locations;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets){
        boolean evaluation = evaluate(drone, this);
        if(evaluation) {
            drone.addDebugEntry("gui.progWidget.condition.evaluatedTrue");
        } else {
            drone.addDebugEntry("gui.progWidget.condition.evaluatedFalse");
        }
        return ProgWidgetJump.jumpToLabel(drone, allWidgets, this, evaluation);
    }

    public abstract boolean evaluate(IDroneBase drone, IProgWidget widget);

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.LIGHTNING_PLANT_DAMAGE;
    }

}
