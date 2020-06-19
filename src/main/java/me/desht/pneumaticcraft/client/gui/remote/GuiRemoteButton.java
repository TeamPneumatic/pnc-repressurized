package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidgetButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;

public class GuiRemoteButton extends GuiRemoteVariable<ActionWidgetButton> {
    private WidgetTextFieldNumber widthField;
    private WidgetTextFieldNumber heightField;
    private WidgetTextFieldNumber xValueField, yValueField, zValueField;

    public GuiRemoteButton(ActionWidgetButton widget, GuiRemoteEditor guiRemote) {
        super(widget, guiRemote);
    }

    @Override
    public void init() {
        super.init();

        addLabel(I18n.format("pneumaticcraft.gui.remote.button.settingValue"), guiLeft + 10, guiTop + 95);
        addLabel("X:", guiLeft + 10, guiTop + 106);
        addLabel("Y:", guiLeft + 67, guiTop + 106);
        addLabel("Z:", guiLeft + 124, guiTop + 106);
        addLabel(I18n.format("pneumaticcraft.gui.remote.button.width"), guiLeft + 10, guiTop + 123);
        addLabel(I18n.format("pneumaticcraft.gui.remote.button.height"), guiLeft + 10, guiTop + 138);

        String valueTooltip = I18n.format("pneumaticcraft.gui.remote.button.value.tooltip");

        xValueField = new WidgetTextFieldNumber(font, guiLeft + 20, guiTop + 105, 38, 10);
        xValueField.setValue(actionWidget.settingCoordinate.getX());
        xValueField.setTooltip(valueTooltip);
        addButton(xValueField);

        yValueField = new WidgetTextFieldNumber(font, guiLeft + 78, guiTop + 105, 38, 10);
        yValueField.setValue(actionWidget.settingCoordinate.getY());
        yValueField.setTooltip(valueTooltip);
        addButton(yValueField);

        zValueField = new WidgetTextFieldNumber(font, guiLeft + 136, guiTop + 105, 38, 10);
        zValueField.setValue(actionWidget.settingCoordinate.getZ());
        zValueField.setTooltip(valueTooltip);
        addButton(zValueField);

        widthField = new WidgetTextFieldNumber(font, guiLeft + 100, guiTop + 123, 60, 10);
        widthField.setValue(actionWidget.getWidth());
        widthField.minValue = 10;
        addButton(widthField);

        heightField = new WidgetTextFieldNumber(font, guiLeft + 100, guiTop + 138, 60, 10);
        heightField.setValue(actionWidget.getHeight());
        heightField.minValue = 10;
        heightField.maxValue = 20;
        addButton(heightField);

    }

    @Override
    public void onClose() {
        actionWidget.settingCoordinate = new BlockPos(xValueField.getValue(), yValueField.getValue(), zValueField.getValue());
        actionWidget.setWidth(widthField.getValue());
        actionWidget.setHeight(heightField.getValue());

        super.onClose();
    }
}
