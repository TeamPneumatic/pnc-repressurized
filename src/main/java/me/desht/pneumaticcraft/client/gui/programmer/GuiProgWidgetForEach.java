package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.IVariableSetWidget;
import net.minecraft.client.resources.I18n;

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
        addButton(variableField);
        variableField.setText(progWidget.getVariable());
        variableField.setFocused2(true);
    }

    @Override
    public void onClose() {
        progWidget.setVariable(variableField.getText());

        super.onClose();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        font.drawString(I18n.format("gui.progWidget.coordinate.variableName"), guiLeft + 10, guiTop + 30, 0xFF000000);
    }
}
