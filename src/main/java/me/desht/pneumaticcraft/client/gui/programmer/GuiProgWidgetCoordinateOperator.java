package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateOperator;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateOperator.EnumOperator;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetCoordinateOperator extends GuiProgWidgetAreaShow<ProgWidgetCoordinateOperator> {

    private WidgetComboBox variableField;

    public GuiProgWidgetCoordinateOperator(ProgWidgetCoordinateOperator widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        List<WidgetRadioButton> radioButtons = new ArrayList<>();
        for (EnumOperator op : EnumOperator.values()) {
            String key = op.getTranslationKey();
            WidgetRadioButton radioButton = new WidgetRadioButton(guiLeft + 7, guiTop + 42 + 12 * op.ordinal(), 0xFF404040,
                    I18n.format(op.getTranslationKey()), b -> progWidget.setOperator(op));
            radioButtons.add(radioButton);
            radioButton.checked = progWidget.getOperator() == op;
            radioButton.otherChoices = radioButtons;
            radioButton.setTooltip(I18n.format(key + ".hint"));
            addButton(radioButton);
        }

        variableField = new WidgetComboBox(font, guiLeft + 7, guiTop + 100, 80, font.FONT_HEIGHT + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        addButton(variableField);
        variableField.setText(progWidget.getVariable());
    }

    @Override
    public void onClose() {
        super.onClose();

        progWidget.setVariable(variableField.getText());
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        font.drawString(I18n.format("pneumaticcraft.gui.progWidget.coordinate.variableName"), guiLeft + 7, guiTop + 88, 0xFF404060);
        font.drawString(I18n.format("pneumaticcraft.gui.progWidget.coordinateOperator.operator"), guiLeft + 7, guiTop + 30, 0xFF404060);
    }
}
