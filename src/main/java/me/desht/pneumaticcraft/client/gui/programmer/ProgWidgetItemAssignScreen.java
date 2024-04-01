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
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetItemAssign;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetItemAssignScreen extends AbstractProgWidgetScreen<ProgWidgetItemAssign> {
    private WidgetComboBox textfield;

    public ProgWidgetItemAssignScreen(ProgWidgetItemAssign widget, ProgrammerScreen guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        textfield = new WidgetComboBox(font, guiLeft + 10, guiTop + 40, 160, font.lineHeight + 3);
        textfield.setElements(guiProgrammer.te.getAllVariables());
        textfield.setMaxLength(GlobalVariableManager.MAX_VARIABLE_LEN);
        textfield.setValue(progWidget.getVariable());
        addRenderableWidget(textfield);

        addRenderableWidget(new WidgetLabel(guiLeft + 10, guiTop + 30, xlate("pneumaticcraft.gui.progWidget.itemFilter.variableLabel")));
    }

    @Override
    public void removed() {
        progWidget.setVariable(textfield.getValue());

        super.removed();
    }
}
