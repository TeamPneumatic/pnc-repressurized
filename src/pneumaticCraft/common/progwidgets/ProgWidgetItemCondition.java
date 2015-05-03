package pneumaticCraft.common.progwidgets;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.entity.living.EntityDrone;
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
    public boolean evaluate(EntityDrone drone, IProgWidget widget){
        ProgWidgetItemFilter checkedFilter = (ProgWidgetItemFilter)widget.getConnectedParameters()[0];
        while(checkedFilter != null) {
            if(checkedFilter.getFilter() != null) {
                if(!ProgWidgetItemFilter.isItemValidForFilters(checkedFilter.getFilter(), ProgWidget.getConnectedWidgetList(this, 1), ProgWidget.getConnectedWidgetList(this, getParameters().length + 1), -1)) return false;
            }

            checkedFilter = (ProgWidgetItemFilter)checkedFilter.getConnectedParameters()[0];
        }

        checkedFilter = (ProgWidgetItemFilter)widget.getConnectedParameters()[3];
        while(checkedFilter != null) {
            if(checkedFilter.getFilter() != null) {
                if(ProgWidgetItemFilter.isItemValidForFilters(checkedFilter.getFilter(), ProgWidget.getConnectedWidgetList(this, 1), ProgWidget.getConnectedWidgetList(this, getParameters().length + 1), -1)) return false;
            }

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
