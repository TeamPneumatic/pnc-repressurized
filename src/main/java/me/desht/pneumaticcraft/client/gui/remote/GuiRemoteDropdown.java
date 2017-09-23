package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.remote.ActionWidgetDropdown;
import net.minecraft.client.resources.I18n;

public class GuiRemoteDropdown extends GuiRemoteVariable<ActionWidgetDropdown> {
    private WidgetTextField dropDownElementsField;
    private WidgetTextFieldNumber widthField;

    public GuiRemoteDropdown(ActionWidgetDropdown widget, GuiRemoteEditor guiRemote) {
        super(widget, guiRemote);
    }

    @Override
    public void initGui() {
        super.initGui();

        addLabel(I18n.format("gui.remote.button.width"), guiLeft + 10, guiTop + 100);
        addLabel(I18n.format("gui.remote.dropdown.dropDownElements"), guiLeft + 10, guiTop + 40);

        dropDownElementsField = new WidgetTextField(fontRenderer, guiLeft + 10, guiTop + 50, 160, 10);
        dropDownElementsField.setText(widget.getDropDownElements());
        dropDownElementsField.setTooltip(I18n.format("gui.remote.dropdown.dropDownElements.tooltip"));
        addWidget(dropDownElementsField);

        widthField = new WidgetTextFieldNumber(fontRenderer, guiLeft + 50, guiTop + 99, 30, 10);
        widthField.setValue(widget.getWidth());
        widthField.minValue = 10;
        addWidget(widthField);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        widget.setDropDownElements(dropDownElementsField.getText());
        widget.setWidth(widthField.getValue());
    }
}
