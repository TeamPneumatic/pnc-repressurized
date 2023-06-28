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

import me.desht.pneumaticcraft.client.gui.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.remote.RemoteDropdownOptionScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ActionWidgetDropdown extends ActionWidgetVariable<WidgetComboBox> {

    private int x, y, width, height;
    private String dropDownElements = "";
    private boolean sorted;

    public ActionWidgetDropdown() {
        super();
    }

    public ActionWidgetDropdown(WidgetComboBox widget) {
        super(widget);
        x = widget.getX();
        y = widget.getY();
        width = widget.getWidth();
        height = widget.getHeight();
        widget.setValue(I18n.get("pneumaticcraft.gui.remote.tray.dropdown.name"));
        widget.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.remote.tray.dropdown.tooltip")));
    }

    @Override
    public void readFromNBT(CompoundTag tag, int guiLeft, int guiTop) {
        super.readFromNBT(tag, guiLeft, guiTop);
        x = tag.getInt("x") + guiLeft;
        y = tag.getInt("y") + guiTop;
        width = tag.getInt("width");
        height = tag.getInt("height");
        dropDownElements = tag.getString("dropDownElements");
        sorted = tag.getBoolean("sorted");
        updateWidget();
    }

    @Override
    public CompoundTag toNBT(int guiLeft, int guiTop) {
        CompoundTag tag = super.toNBT(guiLeft, guiTop);
        tag.putInt("x", x - guiLeft);
        tag.putInt("y", y - guiTop);
        tag.putInt("width", width);
        tag.putInt("height", height);
        tag.putString("dropDownElements", dropDownElements);
        tag.putBoolean("sorted", sorted);

        return tag;
    }

    @Override
    public String getId() {
        return "dropdown";
    }

    @Override
    public void onKeyTyped() {
        if (!getVariableName().isEmpty()) NetworkHandler.sendToServer(new PacketSetGlobalVariable(getVariableName(), widget.getSelectedElementIndex()));
    }

    @Override
    public void onVariableChange() {
        updateWidget();
    }

    @Override
    public void setWidgetPos(int x, int y) {
        this.x = x;
        this.y = y;
        updateWidget();
    }

    @Override
    public WidgetComboBox getWidget() {
        if (widget == null) {
            widget = new WidgetComboBox(Minecraft.getInstance().font, x, y, width, height, this::onPressed);
            widget.setElements(getDropdownElements());
            widget.setFixedOptions(true);
            widget.setShouldSort(sorted);
            updateWidget();
        }
        return widget;
    }

    private void onPressed(WidgetComboBox comboBox) {
        if (comboBox.getSelectedElementIndex() >= 0 && !getVariableName().isEmpty()) {
            NetworkHandler.sendToServer(new PacketSetGlobalVariable(getVariableName(), comboBox.getSelectedElementIndex()));
        }
    }

    private String[] getDropdownElements() {
        return dropDownElements.split(",");
    }

    private void updateWidget() {
        String[] elements = getDropdownElements();
        int idx = GlobalVariableHelper.getInt(ClientUtils.getClientPlayer().getUUID(), getVariableName());
        String selectedElement = elements[Mth.clamp(idx, 0, elements.length - 1)];

        if (widget != null) {
            widget.setPosition(x, y);
            widget.setWidth(width);
            widget.setHeight(height);
            widget.setElements(getDropdownElements());
            widget.setValue(selectedElement);
            widget.setShouldSort(sorted);
        }
    }

    @Override
    public void onActionPerformed() {
    }

    public void setDropDownElements(String dropDownElements) {
        this.dropDownElements = dropDownElements;
        updateWidget();
    }

    public String getDropDownElements() {
        return dropDownElements;
    }

    public boolean getSorted() {
        return sorted;
    }

    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

    public void setWidth(int width) {
        this.width = width;
        updateWidget();
    }

    public int getWidth() {
        return width;
    }

    @Override
    public Screen getGui(RemoteEditorScreen guiRemote) {
        return new RemoteDropdownOptionScreen(this, guiRemote);
    }
}
