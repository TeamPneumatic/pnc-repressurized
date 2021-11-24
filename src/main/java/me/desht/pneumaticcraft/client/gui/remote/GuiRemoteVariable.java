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

package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidgetVariable;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import net.minecraft.util.text.StringTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiRemoteVariable<A extends ActionWidgetVariable<?>> extends GuiRemoteOptionBase<A> {

    private WidgetComboBox variableField;

    public GuiRemoteVariable(A actionWidget, GuiRemoteEditor guiRemote) {
        super(actionWidget, guiRemote);
    }

    @Override
    public void init() {
        super.init();

        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinate.variableName"), guiLeft + 10, guiTop + 70);
        addLabel(new StringTextComponent("#"), guiLeft + 10, guiTop + 81);

        variableField = new WidgetComboBox(font, guiLeft + 18, guiTop + 80, 152, 10);
        variableField.setElements(guiRemote.getMenu().variables);
        variableField.setValue(actionWidget.getVariableName());
        variableField.setTooltip(xlate("pneumaticcraft.gui.remote.variable.tooltip"));
        addButton(variableField);
    }

    @Override
    public void removed() {
        actionWidget.setVariableName(variableField.getValue());

        super.removed();
    }
}
