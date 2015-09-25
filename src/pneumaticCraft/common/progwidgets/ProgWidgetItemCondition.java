package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.lib.Textures;

public class ProgWidgetItemCondition extends ProgWidgetConditionBase{

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetItemFilter.class, ProgWidgetItemFilter.class, ProgWidgetString.class};
    }

    @Override
    public String getWidgetString(){
        return "conditionItem";
    }

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets){
        super.addErrors(curInfo, widgets);
        if(getConnectedParameters()[0] == null && getConnectedParameters()[3] == null) {
            curInfo.add("gui.progWidget.conditionItem.error.noCheckingItem");
        }
        if(getConnectedParameters()[1] == null && getConnectedParameters()[4] == null) {
            curInfo.add("gui.progWidget.conditionItem.error.noFilter");
        }
    }

    @Override
    public boolean evaluate(IDroneBase drone, IProgWidget widget){
        ProgWidgetItemFilter checkedFilter = (ProgWidgetItemFilter)widget.getConnectedParameters()[0];
        while(checkedFilter != null) {
            if(!ProgWidgetItemFilter.isItemValidForFilters(checkedFilter.getFilter(), ProgWidget.getConnectedWidgetList(this, 1), ProgWidget.getConnectedWidgetList(this, getParameters().length + 1), -1)) return false;
            checkedFilter = (ProgWidgetItemFilter)checkedFilter.getConnectedParameters()[0];
        }

        checkedFilter = (ProgWidgetItemFilter)widget.getConnectedParameters()[3];
        while(checkedFilter != null) {
            if(ProgWidgetItemFilter.isItemValidForFilters(checkedFilter.getFilter(), ProgWidget.getConnectedWidgetList(this, 1), ProgWidget.getConnectedWidgetList(this, getParameters().length + 1), -1)) return false;
            checkedFilter = (ProgWidgetItemFilter)checkedFilter.getConnectedParameters()[0];
        }
        return true;
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_CONDITION_ITEM;
    }

}
