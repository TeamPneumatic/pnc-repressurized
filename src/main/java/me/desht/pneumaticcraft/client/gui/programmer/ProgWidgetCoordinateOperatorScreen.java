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

package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetCoordinateOperator;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetCoordinateOperator.EnumOperator;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetCoordinateOperatorScreen extends ProgWidgetAreaShowScreen<ProgWidgetCoordinateOperator> {

    private WidgetComboBox variableField;

    public ProgWidgetCoordinateOperatorScreen(ProgWidgetCoordinateOperator widget, ProgrammerScreen guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinateOperator.operator"), guiLeft + 7, guiTop + 30);
        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinateOperator.axes"), guiLeft + 100, guiTop + 30);
        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinate.variableName"), guiLeft + 7, guiTop + 88);

        WidgetRadioButton.Builder<WidgetRadioButton> builder = WidgetRadioButton.Builder.create();
        for (EnumOperator op : EnumOperator.values()) {
            builder.addRadioButton(new WidgetRadioButton(guiLeft + 7, guiTop + 42 + 12 * op.ordinal(), 0xFF404040,
                            xlate(op.getTranslationKey()), b -> progWidget.setOperator(op))
                            .setTooltipKey(op.getTranslationKey() + ".hint"),
                    progWidget.getOperator() == op);
        }
        builder.build(this::addRenderableWidget);

        for (Direction.Axis axis : Direction.Axis.values()) {
            WidgetCheckBox checkBox = new WidgetCheckBox(guiLeft + 100, guiTop + 42 + axis.ordinal() * 12, 0xFF404040,
                    Component.literal(axis.getName()), b -> progWidget.getAxisOptions().setCheck(axis, b.checked));
            addRenderableWidget(checkBox);
            checkBox.setChecked(progWidget.getAxisOptions().shouldCheck(axis));
        }

        variableField = new WidgetComboBox(font, guiLeft + 7, guiTop + 100, 80, font.lineHeight + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        addRenderableWidget(variableField);
        variableField.setValue(progWidget.getVariable());
    }

    @Override
    public void removed() {
        progWidget.setVariable(variableField.getValue());

        super.removed();
    }
}
