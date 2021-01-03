package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Base class for non-world conditions (drone/item/coordinate)
 */
public abstract class ProgWidgetConditionBase extends ProgWidget implements IJump {

    ProgWidgetConditionBase(ProgWidgetType<?> type) {
        super(type);
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.MEDIUM;
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        IProgWidget widget = getConnectedParameters()[getParameters().size() - 1];
        IProgWidget widget2 = getConnectedParameters()[getParameters().size() * 2 - 1];
        if (widget == null && widget2 == null) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.condition.error.noFlowControl"));
        } else if (widget != null && !(widget instanceof ProgWidgetText) || widget2 != null && !(widget2 instanceof ProgWidgetText)) {
            curInfo.add(xlate("pneumaticcraft.gui.progWidget.condition.error.shouldConnectTextPieces"));
        }
    }

    @Override
    public List<String> getPossibleJumpLocations() {
        IProgWidget widget = getConnectedParameters()[getParameters().size() - 1];
        IProgWidget widget2 = getConnectedParameters()[getParameters().size() * 2 - 1];
        ProgWidgetText textWidget = widget != null ? (ProgWidgetText) widget : null;
        ProgWidgetText textWidget2 = widget2 != null ? (ProgWidgetText) widget2 : null;
        List<String> locations = new ArrayList<>();
        if (textWidget != null) locations.add(textWidget.string);
        if (textWidget2 != null) locations.add(textWidget2.string);
        return locations;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        boolean evaluation = evaluate(drone, this);
        if (evaluation) {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.condition.evaluatedTrue");
        } else {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.condition.evaluatedFalse");
        }
        return ProgWidgetJump.jumpToLabel(drone, allWidgets, this, evaluation);
    }

    public abstract boolean evaluate(IDroneBase drone, IProgWidget widget);

    @Override
    public DyeColor getColor() {
        return DyeColor.CYAN;
    }
}
