package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.IVariableSetWidget;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetForEach<W extends IProgWidget & IVariableSetWidget> extends GuiProgWidgetAreaShow<W> {

    private WidgetComboBox variableField;

    public GuiProgWidgetForEach(W progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        variableField = new WidgetComboBox(font, guiLeft + 10, guiTop + 42, 160, font.FONT_HEIGHT + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        variableField.setMaxStringLength(GlobalVariableManager.MAX_VARIABLE_LEN);
        addButton(variableField);
        variableField.setText(progWidget.getVariable());
        variableField.setFocused2(true);

        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinate.variableName"), guiLeft + 10, guiTop + 30);
    }

    @Override
    public void onClose() {
        progWidget.setVariable(variableField.getText());

        super.onClose();
    }
}
