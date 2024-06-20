/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.drone.progwidgets;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Base class for non-world conditions (drone/item/coordinate)
 */
public abstract class ProgWidgetConditionBase extends ProgWidget implements IJump {

    ProgWidgetConditionBase(PositionFields pos) {
        super(pos);
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
    public void addErrors(List<Component> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        IProgWidget widget = getConnectedParameters()[getParameters().size() - 1];
        IProgWidget widget2 = getConnectedParameters()[getParameters().size() * 2 - 1];
        boolean hasMeasureVar = this instanceof ICondition cond && !cond.getMeasureVar().isEmpty();
        if (widget == null && widget2 == null && !hasMeasureVar) {
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
    public IProgWidget getOutputWidget(IDrone drone, List<IProgWidget> allWidgets) {
        boolean evaluation = evaluate(drone, this);
        if (evaluation) {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.condition.evaluatedTrue");
        } else {
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.condition.evaluatedFalse");
        }
        return ProgWidgetJump.jumpToLabel(drone, allWidgets, this, evaluation);
    }

    public abstract boolean evaluate(IDrone drone, IProgWidget widget);

    @Override
    public DyeColor getColor() {
        return DyeColor.CYAN;
    }
}
