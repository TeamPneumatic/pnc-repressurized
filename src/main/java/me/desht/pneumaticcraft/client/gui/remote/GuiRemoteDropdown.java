package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.remote.ActionWidgetDropdown;
import net.minecraft.client.resources.I18n;

public class GuiRemoteDropdown extends GuiRemoteVariable<ActionWidgetDropdown> {
    private WidgetTextField dropDownElementsField;
    private WidgetTextFieldNumber widthField;
    private GuiCheckBox sortCheckBox;

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

        sortCheckBox = new GuiCheckBox(1, guiLeft + 10, guiTop + 120, 0x404040, I18n.format("gui.remote.dropdown.sort"));
        sortCheckBox.checked = widget.getSorted();
        sortCheckBox.setTooltip(I18n.format("gui.remote.dropdown.sort.tooltip"));
        addWidget(sortCheckBox);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        widget.setDropDownElements(dropDownElementsField.getText());
        widget.setWidth(widthField.getValue());
        widget.setSorted(sortCheckBox.checked);
    }
}
