package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.remote.ActionWidgetButton;
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
    public void initGui() {
        super.initGui();

        addLabel(I18n.format("gui.remote.button.settingValue"), guiLeft + 10, guiTop + 95);
        addLabel("X:", guiLeft + 10, guiTop + 106);
        addLabel("Y:", guiLeft + 67, guiTop + 106);
        addLabel("Z:", guiLeft + 124, guiTop + 106);
        addLabel(I18n.format("gui.remote.button.width"), guiLeft + 10, guiTop + 123);
        addLabel(I18n.format("gui.remote.button.height"), guiLeft + 10, guiTop + 138);

        String valueTooltip = I18n.format("gui.remote.button.value.tooltip");

        xValueField = new WidgetTextFieldNumber(fontRenderer, guiLeft + 20, guiTop + 105, 38, 10);
        xValueField.setValue(widget.settingCoordinate.getX());
        xValueField.setTooltip(valueTooltip);
        addWidget(xValueField);

        yValueField = new WidgetTextFieldNumber(fontRenderer, guiLeft + 78, guiTop + 105, 38, 10);
        yValueField.setValue(widget.settingCoordinate.getY());
        yValueField.setTooltip(valueTooltip);
        addWidget(yValueField);

        zValueField = new WidgetTextFieldNumber(fontRenderer, guiLeft + 136, guiTop + 105, 38, 10);
        zValueField.setValue(widget.settingCoordinate.getZ());
        zValueField.setTooltip(valueTooltip);
        addWidget(zValueField);

        widthField = new WidgetTextFieldNumber(fontRenderer, guiLeft + 100, guiTop + 123, 60, 10);
        widthField.setValue(widget.getWidth());
        widthField.minValue = 10;
        addWidget(widthField);

        heightField = new WidgetTextFieldNumber(fontRenderer, guiLeft + 100, guiTop + 138, 60, 10);
        heightField.setValue(widget.getHeight());
        heightField.minValue = 10;
        heightField.maxValue = 20;
        addWidget(heightField);

    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        widget.settingCoordinate = new BlockPos(xValueField.getValue(), yValueField.getValue(), zValueField.getValue());
        widget.setWidth(widthField.getValue());
        widget.setHeight(heightField.getValue());
    }
}
