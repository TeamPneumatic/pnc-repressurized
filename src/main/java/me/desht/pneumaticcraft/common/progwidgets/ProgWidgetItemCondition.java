package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetItemCondition extends ProgWidgetConditionBase {

    public ProgWidgetItemCondition() {
        super(ModProgWidgets.CONDITION_ITEM.get());
    }

    @Override
    public List<ProgWidgetType> getParameters() {
        return ImmutableList.of(ModProgWidgets.ITEM_FILTER.get(), ModProgWidgets.ITEM_FILTER.get(), ModProgWidgets.TEXT.get());
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null && getConnectedParameters()[3] == null) {
            curInfo.add(xlate("gui.progWidget.conditionItem.error.noCheckingItem"));
        }
        if (getConnectedParameters()[1] == null && getConnectedParameters()[4] == null) {
            curInfo.add(xlate("gui.progWidget.conditionItem.error.noFilter"));
        }
    }

    @Override
    public boolean evaluate(IDroneBase drone, IProgWidget widget) {
        ProgWidgetItemFilter checkedFilter = (ProgWidgetItemFilter) widget.getConnectedParameters()[0];
        while (checkedFilter != null) {
            if (!ProgWidgetItemFilter.isItemValidForFilters(checkedFilter.getFilter(),
                    ProgWidget.getConnectedWidgetList(this, 1),
                    ProgWidget.getConnectedWidgetList(this, getParameters().size() + 1),
                    null))
                return false;
            checkedFilter = (ProgWidgetItemFilter) checkedFilter.getConnectedParameters()[0];
        }

        checkedFilter = (ProgWidgetItemFilter) widget.getConnectedParameters()[3];
        while (checkedFilter != null) {
            if (ProgWidgetItemFilter.isItemValidForFilters(checkedFilter.getFilter(),
                    ProgWidget.getConnectedWidgetList(this, 1),
                    ProgWidget.getConnectedWidgetList(this, getParameters().size() + 1),
                    null))
                return false;
            checkedFilter = (ProgWidgetItemFilter) checkedFilter.getConnectedParameters()[0];
        }
        return true;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_CONDITION_ITEM;
    }

}
