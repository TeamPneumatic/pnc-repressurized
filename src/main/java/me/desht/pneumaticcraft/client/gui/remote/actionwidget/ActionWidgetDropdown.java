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

package me.desht.pneumaticcraft.client.gui.remote.actionwidget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.client.gui.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.RemoteScreen;
import me.desht.pneumaticcraft.client.gui.remote.RemoteDropdownOptionScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.List;

public class ActionWidgetDropdown extends ActionWidgetVariable<WidgetComboBox> {
    public static final MapCodec<ActionWidgetDropdown> CODEC = RecordCodecBuilder.mapCodec(builder ->
            varParts(builder).and(builder.group(
                    Codec.STRING.listOf().fieldOf("elements").forGetter(ActionWidgetDropdown::getDropDownElements),
                    Codec.BOOL.optionalFieldOf("sorted", false).forGetter(ActionWidgetDropdown::getSorted)
            )).apply(builder, ActionWidgetDropdown::new));
    public static final String ID = "dropdown";

    private List<String> dropDownElements;
    private boolean sorted;

    public ActionWidgetDropdown(RemoteEditorScreen remoteEditorScreen, WidgetComboBox widgetComboBox) {
        super(BaseSettings.DEFAULT, WidgetSettings.forWidget(remoteEditorScreen, widgetComboBox), "");
    }

    public ActionWidgetDropdown(BaseSettings baseSettings, WidgetSettings widgetSettings, String variableName, List<String> elements, boolean sorted) {
        super(baseSettings, widgetSettings, variableName);

        this.dropDownElements = elements;
        this.sorted = sorted;
    }

    @Override
    public MapCodec<? extends ActionWidget<WidgetComboBox>> codec() {
        return CODEC;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void onKeyTyped() {
        if (!getVariableName().isEmpty()) NetworkHandler.sendToServer(PacketSetGlobalVariable.forInt(getVariableName(), widget.getSelectedElementIndex()));
    }

    @Override
    public void onVariableChange() {
        if (widget != null) {
            widget.setValue(getSelectedElement());
        }
    }

    @Override
    protected WidgetComboBox createMinecraftWidget(RemoteScreen screen) {
        WidgetComboBox res = new WidgetComboBox(Minecraft.getInstance().font,
                widgetSettings.getX(), widgetSettings.getY(), widgetSettings.getWidth(), widgetSettings.getHeight(),
                this::onPressed
        );
        res.setElements(dropDownElements);
        res.setFixedOptions(true);
        res.setShouldSort(sorted);
        res.setValue(getSelectedElement());
        return res;
    }

    private void onPressed(WidgetComboBox comboBox) {
        if (comboBox.getSelectedElementIndex() >= 0 && !getVariableName().isEmpty()) {
            NetworkHandler.sendToServer(PacketSetGlobalVariable.forInt(getVariableName(), comboBox.getSelectedElementIndex()));
        }
    }

    private String getSelectedElement() {
        int idx = GlobalVariableHelper.getInt(ClientUtils.getClientPlayer().getUUID(), getVariableName());
        return dropDownElements.get(Mth.clamp(idx, 0, dropDownElements.size() - 1));
    }

    @Override
    public void onActionPerformed() {
        // nothing
    }

    public void setDropDownElements(String[] dropDownElements) {
        this.dropDownElements = Arrays.asList(dropDownElements);
        if (widget != null) {
            widget.setElements(dropDownElements);
            widget.setValue(getSelectedElement());
        }
    }

    public List<String> getDropDownElements() {
        return dropDownElements;
    }

    public boolean getSorted() {
        return sorted;
    }

    public void setSorted(boolean sorted) {
        this.sorted = sorted;
        if (widget != null) {
            widget.setShouldSort(sorted);
        }
    }

    public void setWidth(int width) {
        widgetSettings.setWidth(width);
        if (widget != null) {
            widget.setWidth(width);
        }
    }

    public int getWidth() {
        return widgetSettings.getWidth();
    }

    @Override
    public Screen createConfigurationGui(RemoteEditorScreen guiRemote) {
        return new RemoteDropdownOptionScreen(this, guiRemote);
    }
}
