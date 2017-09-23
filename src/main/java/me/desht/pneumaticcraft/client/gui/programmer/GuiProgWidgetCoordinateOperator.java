package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiRadioButton;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateOperator;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateOperator.EnumOperator;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetCoordinateOperator extends GuiProgWidgetAreaShow<ProgWidgetCoordinateOperator> {

    private WidgetComboBox variableField;

    public GuiProgWidgetCoordinateOperator(ProgWidgetCoordinateOperator widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui() {
        super.initGui();

        List<GuiRadioButton> radioButtons = new ArrayList<GuiRadioButton>();
        for (int i = 0; i < EnumOperator.values().length; i++) {
            GuiRadioButton radioButton = new GuiRadioButton(i, guiLeft + 7, guiTop + 42 + 12 * i, 0xFF000000, I18n.format(EnumOperator.values()[i].getUnlocalizedName()));
            radioButtons.add(radioButton);
            radioButton.checked = widget.getOperator().ordinal() == i;
            radioButton.otherChoices = radioButtons;
            addWidget(radioButton);
        }

        variableField = new WidgetComboBox(fontRenderer, guiLeft + 90, guiTop + 42, 80, fontRenderer.FONT_HEIGHT + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        addWidget(variableField);
        variableField.setText(widget.getVariable());
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget) {
        if (guiWidget.getID() >= 0 && guiWidget.getID() < EnumOperator.values().length) {
            widget.setOperator(EnumOperator.values()[guiWidget.getID()]);
        }
        super.actionPerformed(guiWidget);
    }

    @Override
    public void keyTyped(char chr, int keyCode) throws IOException {
        if (keyCode == 1) {
            widget.setVariable(variableField.getText());
        }
        super.keyTyped(chr, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRenderer.drawString(I18n.format("gui.progWidget.coordinate.variableName"), guiLeft + 90, guiTop + 30, 0xFF000000);
        fontRenderer.drawString(I18n.format("gui.progWidget.coordinateOperator.operator"), guiLeft + 7, guiTop + 30, 0xFF000000);
    }
}
