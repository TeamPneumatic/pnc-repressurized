package me.desht.pneumaticcraft.client.gui.remote.actionwidget;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.GuiRemoteDropdown;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetGlobalVariable;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ActionWidgetDropdown extends ActionWidgetVariable<WidgetComboBox> {

    private int x, y, width, height;
    private String dropDownElements = "";
    private String selectedElement = "";
    private boolean sorted;

    public ActionWidgetDropdown() {
        super();
    }

    public ActionWidgetDropdown(WidgetComboBox widget) {
        super(widget);
        x = widget.x;
        y = widget.y;
        width = widget.getWidth();
        height = widget.getHeightRealms();
        widget.setText(I18n.format("pneumaticcraft.gui.remote.tray.dropdown.name"));
        widget.setTooltip(xlate("pneumaticcraft.gui.remote.tray.dropdown.tooltip"));
    }

    @Override
    public void readFromNBT(CompoundNBT tag, int guiLeft, int guiTop) {
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
    public CompoundNBT toNBT(int guiLeft, int guiTop) {
        CompoundNBT tag = super.toNBT(guiLeft, guiTop);
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
            widget = new WidgetComboBox(Minecraft.getInstance().fontRenderer, x, y, width, height, this::onPressed);
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
        int idx = GlobalVariableHelper.getInt(ClientUtils.getClientPlayer().getUniqueID(), getVariableName());
        selectedElement = elements[MathHelper.clamp(idx, 0, elements.length - 1)];

        if (widget != null) {
            widget.x = x;
            widget.y = y;
            widget.setWidth(width);
            widget.setHeight(height);
            widget.setElements(getDropdownElements());
            widget.setText(selectedElement);
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
    public Screen getGui(GuiRemoteEditor guiRemote) {
        return new GuiRemoteDropdown(this, guiRemote);
    }
}
