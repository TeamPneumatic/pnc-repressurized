package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetEntityAttack;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetEntityAttack extends GuiProgWidgetAreaShow<ProgWidgetEntityAttack> {
    private WidgetTextFieldNumber textField;

    public GuiProgWidgetEntityAttack(ProgWidgetEntityAttack progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetCheckBox useMaxActions = new WidgetCheckBox(guiLeft + 8, guiTop + 25, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.digAndPlace.useMaxActions"), b -> {
            progWidget.setUseMaxActions(b.checked);
            textField.setVisible(progWidget.useMaxActions());
        })
                .setTooltipKey("pneumaticcraft.gui.progWidget.digAndPlace.useMaxActions.tooltip")
                .setChecked(progWidget.useMaxActions());
        addButton(useMaxActions);

        textField = new WidgetTextFieldNumber(font, guiLeft + 20, useMaxActions.y + useMaxActions.getHeightRealms() + 2, 30, 11)
                .setRange(1, Integer.MAX_VALUE)
                .setAdjustments(1, 10);
        textField.setValue(progWidget.getMaxActions());
        textField.setVisible(useMaxActions.checked);
        addButton(textField);
    }

    @Override
    public void onClose() {
        progWidget.setMaxActions(textField.getValue());

        super.onClose();
    }
}
