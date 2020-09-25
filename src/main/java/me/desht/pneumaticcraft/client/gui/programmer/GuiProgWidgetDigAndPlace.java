package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.progwidgets.IBlockOrdered.Ordering;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetDigAndPlace;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class GuiProgWidgetDigAndPlace<P extends ProgWidgetDigAndPlace> extends GuiProgWidgetAreaShow<P> {

    private WidgetTextFieldNumber textField;
    private WidgetComboBox orderSelector;

    public GuiProgWidgetDigAndPlace(P progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetLabel orderLabel = new WidgetLabel(guiLeft + 8, guiTop + 25, xlate("pneumaticcraft.gui.progWidget.digAndPlace.order"));
        addButton(orderLabel);

        orderSelector = new WidgetComboBox(font,guiLeft + 8 + orderLabel.getWidth() + 5, guiTop + 23, 80, 12)
                .initFromEnum(progWidget.getOrder());
        addButton(orderSelector);

        if (progWidget.supportsMaxActions()) {
            WidgetCheckBox useMaxActions = new WidgetCheckBox(guiLeft + 8, guiTop + 115, 0xFF404040,
                    xlate("pneumaticcraft.gui.progWidget.digAndPlace.useMaxActions"), b -> {
                progWidget.setUseMaxActions(b.checked);
                textField.setVisible(progWidget.useMaxActions());
            })
                    .setTooltipKey("pneumaticcraft.gui.progWidget.digAndPlace.useMaxActions.tooltip")
                    .setChecked(progWidget.useMaxActions());
            addButton(useMaxActions);

            textField = new WidgetTextFieldNumber(font, guiLeft + 20, guiTop + 128, 30, 11);
            textField.setValue(progWidget.getMaxActions());
            textField.setVisible(useMaxActions.checked);
            addButton(textField);
        }
    }

    @Override
    public void onClose() {
        if (orderSelector.getSelectedElementIndex() >= 0) {
            progWidget.setOrder(Ordering.values()[orderSelector.getSelectedElementIndex()]);
        }
        if (progWidget.supportsMaxActions()) {
            progWidget.setMaxActions(textField.getValue());
        }

        super.onClose();
    }
}
