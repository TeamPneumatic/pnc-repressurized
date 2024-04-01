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

import me.desht.pneumaticcraft.client.gui.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidgetButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class RemoteButtonOptionScreen extends RemoteVariableOptionScreen<ActionWidgetButton> {
    private WidgetTextFieldNumber widthField;
    private WidgetTextFieldNumber heightField;
    private WidgetTextFieldNumber xValueField, yValueField, zValueField;

    public RemoteButtonOptionScreen(ActionWidgetButton widget, RemoteEditorScreen guiRemote) {
        super(widget, guiRemote);
    }

    @Override
    public void init() {
        super.init();

        int textFieldHeight = font.lineHeight + 3;

        addLabel(xlate("pneumaticcraft.gui.remote.button.settingValue"), guiLeft + 10, guiTop + 95);
        addLabel(Component.literal("X:"), guiLeft + 10, guiTop + 106);
        addLabel(Component.literal("Y:"), guiLeft + 67, guiTop + 106);
        addLabel(Component.literal("Z:"), guiLeft + 124, guiTop + 106);
        WidgetLabel lw = addLabel(xlate("pneumaticcraft.gui.remote.button.width"), guiLeft + 10, guiTop + 123);
        WidgetLabel lh = addLabel(xlate("pneumaticcraft.gui.remote.button.height"), guiLeft + 10, guiTop + 136);
        int xOff = guiLeft + 13 + Math.max(lw.getWidth(), lh.getWidth());
        lw.setX(xOff - (lw.getWidth() + 3));
        lh.setX(xOff - (lh.getWidth() + 3));

        Component valueTooltip = xlate("pneumaticcraft.gui.remote.button.value.tooltip");

        xValueField = new WidgetTextFieldNumber(font, guiLeft + 20, guiTop + 104, 38, textFieldHeight);
        xValueField.setValue(actionWidget.settingCoordinate.getX());
        xValueField.setTooltip(Tooltip.create(valueTooltip));
        addRenderableWidget(xValueField);

        yValueField = new WidgetTextFieldNumber(font, guiLeft + 78, guiTop + 104, 38, textFieldHeight);
        yValueField.setValue(actionWidget.settingCoordinate.getY());
        yValueField.setTooltip(Tooltip.create(valueTooltip));
        addRenderableWidget(yValueField);

        zValueField = new WidgetTextFieldNumber(font, guiLeft + 136, guiTop + 104, 38, textFieldHeight);
        zValueField.setValue(actionWidget.settingCoordinate.getZ());
        zValueField.setTooltip(Tooltip.create(valueTooltip));
        addRenderableWidget(zValueField);

        widthField = new WidgetTextFieldNumber(font, xOff, guiTop + 121, 35, textFieldHeight)
                .setRange(10, Integer.MAX_VALUE).setAdjustments(1, 10);
        widthField.setValue(actionWidget.getWidth());
        widthField.minValue = 10;
        addRenderableWidget(widthField);

        heightField = new WidgetTextFieldNumber(font, xOff, guiTop + 134, 35, textFieldHeight)
                .setRange(10, Integer.MAX_VALUE).setAdjustments(1, 10);
        heightField.setValue(actionWidget.getHeight());
        heightField.minValue = 10;
        heightField.maxValue = 20;
        addRenderableWidget(heightField);

    }

    @Override
    public void removed() {
        actionWidget.settingCoordinate = new BlockPos(xValueField.getIntValue(), yValueField.getIntValue(), zValueField.getIntValue());
        actionWidget.setWidth(widthField.getIntValue());
        actionWidget.setHeight(heightField.getIntValue());

        super.removed();
    }
}
