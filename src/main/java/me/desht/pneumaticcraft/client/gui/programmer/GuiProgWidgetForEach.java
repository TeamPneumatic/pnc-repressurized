package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.IVariableSetWidget;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

public class GuiProgWidgetForEach extends GuiProgWidgetAreaShow {

    private WidgetComboBox variableField;

    public GuiProgWidgetForEach(IProgWidget widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui() {
        super.initGui();

        variableField = new WidgetComboBox(fontRenderer, guiLeft + 10, guiTop + 42, 160, fontRenderer.FONT_HEIGHT + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        addWidget(variableField);
        variableField.setText(((IVariableSetWidget) widget).getVariable());
    }

    @Override
    public void keyTyped(char chr, int keyCode) throws IOException {
        if (keyCode == 1) {
            ((IVariableSetWidget) widget).setVariable(variableField.getText());
        }
        super.keyTyped(chr, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRenderer.drawString(I18n.format("gui.progWidget.coordinate.variableName"), guiLeft + 10, guiTop + 30, 0xFF000000);
    }
}
