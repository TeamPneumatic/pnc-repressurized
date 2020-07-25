package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetItemAssign;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetItemAssign extends GuiProgWidgetOptionBase<ProgWidgetItemAssign> {
    private WidgetComboBox textfield;

    public GuiProgWidgetItemAssign(ProgWidgetItemAssign widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        textfield = new WidgetComboBox(font, guiLeft + 10, guiTop + 40, 160, 10);
        textfield.setElements(guiProgrammer.te.getAllVariables());
        textfield.setMaxStringLength(GlobalVariableManager.MAX_VARIABLE_LEN);
        textfield.setText(progWidget.getVariable());
        addButton(textfield);

        addButton(new WidgetLabel(guiLeft + 10, guiTop + 30, xlate("pneumaticcraft.gui.progWidget.itemAssign.settingVariable")));
    }

    @Override
    public void onClose() {
        progWidget.setVariable(textfield.getText());

        super.onClose();
    }
}
