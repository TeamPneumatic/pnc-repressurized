package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateOperator;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateOperator.EnumOperator;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetCoordinateOperator extends GuiProgWidgetAreaShow<ProgWidgetCoordinateOperator> {

    private WidgetComboBox variableField;

    public GuiProgWidgetCoordinateOperator(ProgWidgetCoordinateOperator widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinateOperator.operator"), guiLeft + 7, guiTop + 30);
        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinate.variableName"), guiLeft + 7, guiTop + 88);

        List<WidgetRadioButton> radioButtons = new ArrayList<>();
        for (EnumOperator op : EnumOperator.values()) {
            String key = op.getTranslationKey();
            WidgetRadioButton radioButton = new WidgetRadioButton(guiLeft + 7, guiTop + 42 + 12 * op.ordinal(), 0xFF404040,
                    xlate(op.getTranslationKey()), b -> progWidget.setOperator(op));
            radioButtons.add(radioButton);
            radioButton.checked = progWidget.getOperator() == op;
            radioButton.otherChoices = radioButtons;
//            radioButton.setTooltip(RenderComponentsUtil.func_238505_a_(xlate(key + ".hint"), 100, font));
            radioButton.setTooltip(PneumaticCraftUtils.splitStringComponent(I18n.format(key + ".hint")));
            addButton(radioButton);
        }

        variableField = new WidgetComboBox(font, guiLeft + 7, guiTop + 100, 80, font.FONT_HEIGHT + 1);
        variableField.setElements(guiProgrammer.te.getAllVariables());
        addButton(variableField);
        variableField.setText(progWidget.getVariable());
    }

    @Override
    public void onClose() {
        progWidget.setVariable(variableField.getText());

        super.onClose();
    }
}
