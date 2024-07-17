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

package me.desht.pneumaticcraft.client.gui.remote.config;

import me.desht.pneumaticcraft.api.remote.WidgetSettings;
import me.desht.pneumaticcraft.client.gui.remote.AbstractRemoteScreen;
import me.desht.pneumaticcraft.client.gui.remote.RemoteClientRegistry;
import me.desht.pneumaticcraft.client.gui.remote.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.remote.RemoteWidgetButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class RemoteButtonOptionScreen extends AbstractRemoteVariableConfigScreen<RemoteWidgetButton> {
    private WidgetTextFieldNumber widthField;
    private WidgetTextFieldNumber heightField;
    private WidgetTextFieldNumber xValueField, yValueField, zValueField;

    public RemoteButtonOptionScreen(RemoteWidgetButton widget, RemoteEditorScreen guiRemote) {
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
        xValueField.setValue(remoteWidget.settingPos().getX());
        xValueField.setTooltip(Tooltip.create(valueTooltip));
        addRenderableWidget(xValueField);

        yValueField = new WidgetTextFieldNumber(font, guiLeft + 78, guiTop + 104, 38, textFieldHeight);
        yValueField.setValue(remoteWidget.settingPos().getY());
        yValueField.setTooltip(Tooltip.create(valueTooltip));
        addRenderableWidget(yValueField);

        zValueField = new WidgetTextFieldNumber(font, guiLeft + 136, guiTop + 104, 38, textFieldHeight);
        zValueField.setValue(remoteWidget.settingPos().getZ());
        zValueField.setTooltip(Tooltip.create(valueTooltip));
        addRenderableWidget(zValueField);

        widthField = new WidgetTextFieldNumber(font, xOff, guiTop + 121, 35, textFieldHeight)
                .setRange(10, Integer.MAX_VALUE).setAdjustments(1, 10);
        widthField.setValue(remoteWidget.widgetSettings().width());
        widthField.minValue = 10;
        addRenderableWidget(widthField);

        heightField = new WidgetTextFieldNumber(font, xOff, guiTop + 134, 35, textFieldHeight)
                .setRange(10, Integer.MAX_VALUE).setAdjustments(1, 10);
        heightField.setValue(remoteWidget.widgetSettings().height());
        heightField.minValue = 10;
        heightField.maxValue = 20;
        addRenderableWidget(heightField);
    }

    @Override
    protected RemoteWidgetButton makeUpdatedRemoteWidget() {
        return new RemoteWidgetButton(
                makeBaseSettings(),
                makeWidgetSettings().resize(widthField.getIntValue(), heightField.getIntValue()),
                makeVarName(),
                new BlockPos(xValueField.getIntValue(), yValueField.getIntValue(), zValueField.getIntValue())
        );
    }

    public enum Factory implements RemoteClientRegistry.Factory<RemoteWidgetButton,WidgetButtonExtended> {
        INSTANCE;

        @Override
        public WidgetButtonExtended createMinecraftWidget(RemoteWidgetButton remoteWidget, AbstractRemoteScreen screen) {
            WidgetSettings widgetSettings = remoteWidget.widgetSettings();
            return new WidgetButtonExtended(
                    widgetSettings.x() + screen.getGuiLeft(), widgetSettings.y() + screen.getGuiTop(),
                    widgetSettings.width(), widgetSettings.height(),
                    widgetSettings.title(),
                    btn -> {
                        if (!remoteWidget.varName().isEmpty()) {
                            NetworkHandler.sendToServer(PacketSetGlobalVariable.forPos(remoteWidget.varName(), remoteWidget.settingPos()));
                        }
                    }
            ).setTooltipText(remoteWidget.widgetSettings().tooltip());
        }

        @Override
        public Screen createConfigurationScreen(RemoteWidgetButton remoteWidget, RemoteEditorScreen screen) {
            return new RemoteButtonOptionScreen(remoteWidget, screen);
        }
    }
}
