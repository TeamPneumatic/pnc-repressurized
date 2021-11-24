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
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidgetDropdown;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiRemoteDropdown extends GuiRemoteVariable<ActionWidgetDropdown> {
    private WidgetTextField dropDownElementsField;
    private WidgetTextFieldNumber widthField;
    private WidgetCheckBox sortCheckBox;

    public GuiRemoteDropdown(ActionWidgetDropdown widget, GuiRemoteEditor guiRemote) {
        super(widget, guiRemote);
    }

    @Override
    public void init() {
        super.init();

        addLabel(xlate("pneumaticcraft.gui.remote.button.width"), guiLeft + 10, guiTop + 100);
        addLabel(xlate("pneumaticcraft.gui.remote.dropdown.dropDownElements"), guiLeft + 10, guiTop + 40);

        dropDownElementsField = new WidgetTextField(font, guiLeft + 10, guiTop + 50, 160, 10);
        dropDownElementsField.setMaxLength(1024);
        dropDownElementsField.setValue(actionWidget.getDropDownElements());
        dropDownElementsField.setTooltip(xlate("pneumaticcraft.gui.remote.dropdown.dropDownElements.tooltip"));
        addButton(dropDownElementsField);

        widthField = new WidgetTextFieldNumber(font, guiLeft + 50, guiTop + 99, 30, 10).setRange(10, Integer.MAX_VALUE);
        widthField.setValue(actionWidget.getWidth());
        widthField.minValue = 10;
        addButton(widthField);

        sortCheckBox = new WidgetCheckBox(guiLeft + 10, guiTop + 120, 0x404040, xlate("pneumaticcraft.gui.remote.dropdown.sort"))
                .setTooltipKey("pneumaticcraft.gui.remote.dropdown.sort.tooltip")
                .setChecked(actionWidget.getSorted());
        addButton(sortCheckBox);
    }

    @Override
    public void removed() {
        actionWidget.setDropDownElements(dropDownElementsField.getValue());
        actionWidget.setWidth(widthField.getIntValue());
        actionWidget.setSorted(sortCheckBox.checked);

        super.removed();
    }
}
