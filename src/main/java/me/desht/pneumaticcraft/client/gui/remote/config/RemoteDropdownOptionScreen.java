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

import me.desht.pneumaticcraft.client.gui.remote.AbstractRemoteScreen;
import me.desht.pneumaticcraft.client.gui.remote.RemoteClientRegistry;
import me.desht.pneumaticcraft.client.gui.remote.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.remote.RemoteWidgetDropdown;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class RemoteDropdownOptionScreen extends AbstractRemoteVariableConfigScreen<RemoteWidgetDropdown> {
    private WidgetTextField dropDownElementsField;
    private WidgetTextFieldNumber widthField;
    private WidgetCheckBox sortCheckBox;

    public RemoteDropdownOptionScreen(RemoteWidgetDropdown widget, RemoteEditorScreen guiRemote) {
        super(widget, guiRemote);
    }

    @Override
    public void init() {
        super.init();

        addLabel(xlate("pneumaticcraft.gui.remote.button.width"), guiLeft + 10, guiTop + 100);
        addLabel(xlate("pneumaticcraft.gui.remote.dropdown.dropDownElements"), guiLeft + 10, guiTop + 40);

        dropDownElementsField = new WidgetTextField(font, guiLeft + 10, guiTop + 49, 160);
        dropDownElementsField.setMaxLength(1024);
        dropDownElementsField.setValue(String.join(",", remoteWidget.elements()));
        dropDownElementsField.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.remote.dropdown.dropDownElements.tooltip")));
        addRenderableWidget(dropDownElementsField);

        widthField = new WidgetTextFieldNumber(font, guiLeft + 49, guiTop + 99, 30).setRange(10, Integer.MAX_VALUE);
        widthField.setValue(remoteWidget.widgetSettings().width());
        widthField.minValue = 10;
        addRenderableWidget(widthField);

        sortCheckBox = new WidgetCheckBox(guiLeft + 10, guiTop + 120, 0x404040, xlate("pneumaticcraft.gui.remote.dropdown.sort"))
                .setChecked(remoteWidget.sorted());
        sortCheckBox.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.remote.dropdown.sort.tooltip")));
        addRenderableWidget(sortCheckBox);
    }

    @Override
    protected RemoteWidgetDropdown makeUpdatedRemoteWidget() {
        return new RemoteWidgetDropdown(
                makeBaseSettings(),
                makeWidgetSettings().resize(widthField.getIntValue(), Minecraft.getInstance().font.lineHeight + 3),
                makeVarName(),
                List.of(dropDownElementsField.getValue().split(",")),
                sortCheckBox.isChecked()
        );
    }

    public enum Factory implements RemoteClientRegistry.Factory<RemoteWidgetDropdown,WidgetComboBox> {
        INSTANCE;

        @Override
        public WidgetComboBox createMinecraftWidget(RemoteWidgetDropdown remoteWidget, AbstractRemoteScreen screen) {
            WidgetComboBox res = new WidgetComboBox(Minecraft.getInstance().font,
                    remoteWidget.widgetSettings().x() + screen.getGuiLeft(),
                    remoteWidget.widgetSettings().y() + screen.getGuiTop(),
                    remoteWidget.widgetSettings().width(), Minecraft.getInstance().font.lineHeight + 3,
                    btn -> {
                        if (btn.getSelectedElementIndex() >= 0 && !remoteWidget.varName().isEmpty()) {
                            NetworkHandler.sendToServer(PacketSetGlobalVariable.forInt(remoteWidget.varName(), btn.getSelectedElementIndex()));
                        }
                    }
            );
            res.setElements(remoteWidget.elements());
            res.setFixedOptions(true);
            res.setShouldSort(remoteWidget.sorted());
            res.setValue(remoteWidget.getSelectedElement());
            res.setTooltip(Tooltip.create(remoteWidget.widgetSettings().tooltip()));
            return res;
        }

        @Override
        public Screen createConfigurationScreen(RemoteWidgetDropdown remoteWidget, RemoteEditorScreen screen) {
            return new RemoteDropdownOptionScreen(remoteWidget, screen);
        }

        @Override
        public void handleGlobalVariableChange(RemoteWidgetDropdown remoteWidget, WidgetComboBox mcWidget, String varName) {
            int idx = GlobalVariableHelper.getInstance().getInt(ClientUtils.getClientPlayer().getUUID(), varName);
            mcWidget.selectElement(idx);
        }
    }
}
