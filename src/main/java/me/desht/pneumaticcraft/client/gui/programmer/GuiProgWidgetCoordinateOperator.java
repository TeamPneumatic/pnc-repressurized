package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateOperator;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateOperator.EnumOperator;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;

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
        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinateOperator.axes"), guiLeft + 100, guiTop + 30);
        addLabel(xlate("pneumaticcraft.gui.progWidget.coordinate.variableName"), guiLeft + 7, guiTop + 88);

        WidgetRadioButton.Builder<WidgetRadioButton> builder = WidgetRadioButton.Builder.create();
        for (EnumOperator op : EnumOperator.values()) {
            builder.addRadioButton(new WidgetRadioButton(guiLeft + 7, guiTop + 42 + 12 * op.ordinal(), 0xFF404040,
                            xlate(op.getTranslationKey()), b -> progWidget.setOperator(op))
                            .setTooltip(xlate(op.getTranslationKey() + ".hint")),
                    progWidget.getOperator() == op);
        }
        builder.build(this::addButton);

        for (Direction.Axis axis : Direction.Axis.values()) {
            WidgetCheckBox checkBox = new WidgetCheckBox(guiLeft + 100, guiTop + 42 + axis.ordinal() * 12, 0xFF404040,
                    new StringTextComponent(axis.getName2()), b -> progWidget.getAxisOptions().setCheck(axis, b.checked));
            addButton(checkBox);
            checkBox.setChecked(progWidget.getAxisOptions().shouldCheck(axis));
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
