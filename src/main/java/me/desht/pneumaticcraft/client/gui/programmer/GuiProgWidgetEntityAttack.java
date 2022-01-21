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

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetEntityAttack;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetEntityAttack extends GuiProgWidgetAreaShow<ProgWidgetEntityAttack> {
    private WidgetTextFieldNumber textField;

    public GuiProgWidgetEntityAttack(ProgWidgetEntityAttack progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetCheckBox useMaxActions = new WidgetCheckBox(guiLeft + 8, guiTop + 25, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.digAndPlace.useMaxActions"), b -> {
            progWidget.setUseMaxActions(b.checked);
            textField.setVisible(progWidget.useMaxActions());
        })
                .setTooltipKey("pneumaticcraft.gui.progWidget.digAndPlace.useMaxActions.tooltip")
                .setChecked(progWidget.useMaxActions());
        addRenderableWidget(useMaxActions);

        textField = new WidgetTextFieldNumber(font, guiLeft + 20, useMaxActions.y + useMaxActions.getHeight() + 2, 30, 11)
                .setRange(1, Integer.MAX_VALUE)
                .setAdjustments(1, 10);
        textField.setValue(progWidget.getMaxActions());
        textField.setVisible(useMaxActions.checked);
        addRenderableWidget(textField);
    }

    @Override
    public void removed() {
        progWidget.setMaxActions(textField.getIntValue());

        super.removed();
    }
}
