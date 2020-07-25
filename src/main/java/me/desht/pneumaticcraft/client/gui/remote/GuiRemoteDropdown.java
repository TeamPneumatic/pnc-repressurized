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
        dropDownElementsField.setMaxStringLength(1024);
        dropDownElementsField.setText(actionWidget.getDropDownElements());
        dropDownElementsField.setTooltip(xlate("pneumaticcraft.gui.remote.dropdown.dropDownElements.tooltip"));
        addButton(dropDownElementsField);

        widthField = new WidgetTextFieldNumber(font, guiLeft + 50, guiTop + 99, 30, 10);
        widthField.setValue(actionWidget.getWidth());
        widthField.minValue = 10;
        addButton(widthField);

        sortCheckBox = new WidgetCheckBox(guiLeft + 10, guiTop + 120, 0x404040, xlate("pneumaticcraft.gui.remote.dropdown.sort"));
        sortCheckBox.checked = actionWidget.getSorted();
        sortCheckBox.setTooltip(xlate("pneumaticcraft.gui.remote.dropdown.sort.tooltip"));
        addButton(sortCheckBox);
    }

    @Override
    public void onClose() {
        actionWidget.setDropDownElements(dropDownElementsField.getText());
        actionWidget.setWidth(widthField.getValue());
        actionWidget.setSorted(sortCheckBox.checked);

        super.onClose();
    }
}
