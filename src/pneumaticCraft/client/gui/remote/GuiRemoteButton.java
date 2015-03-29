package pneumaticCraft.client.gui.remote;

import net.minecraft.client.resources.I18n;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.client.gui.GuiRemoteEditor;
import pneumaticCraft.client.gui.widget.WidgetTextFieldNumber;
import pneumaticCraft.common.remote.ActionWidgetButton;

public class GuiRemoteButton extends GuiRemoteVariable<ActionWidgetButton>{
    private WidgetTextFieldNumber widthField;
    private WidgetTextFieldNumber heightField;
    private WidgetTextFieldNumber xValueField, yValueField, zValueField;

    public GuiRemoteButton(ActionWidgetButton widget, GuiRemoteEditor guiRemote){
        super(widget, guiRemote);
    }

    @Override
    public void initGui(){
        super.initGui();

        addLabel(I18n.format("gui.remote.button.settingValue"), guiLeft + 10, guiTop + 95);
        addLabel("X:", guiLeft + 10, guiTop + 106);
        addLabel("Y:", guiLeft + 67, guiTop + 106);
        addLabel("Z:", guiLeft + 124, guiTop + 106);
        addLabel(I18n.format("gui.remote.button.width"), guiLeft + 10, guiTop + 123);
        addLabel(I18n.format("gui.remote.button.height"), guiLeft + 10, guiTop + 138);

        String valueTooltip = I18n.format("gui.remote.button.value.tooltip");

        xValueField = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 20, guiTop + 105, 38, 10);
        xValueField.setValue(widget.settingCoordinate.chunkPosX);
        xValueField.setTooltip(valueTooltip);
        addWidget(xValueField);

        yValueField = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 78, guiTop + 105, 38, 10);
        yValueField.setValue(widget.settingCoordinate.chunkPosY);
        yValueField.setTooltip(valueTooltip);
        addWidget(yValueField);

        zValueField = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 136, guiTop + 105, 38, 10);
        zValueField.setValue(widget.settingCoordinate.chunkPosZ);
        zValueField.setTooltip(valueTooltip);
        addWidget(zValueField);

        widthField = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 100, guiTop + 123, 60, 10);
        widthField.setValue(widget.getWidth());
        widthField.minValue = 10;
        addWidget(widthField);

        heightField = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 100, guiTop + 138, 60, 10);
        heightField.setValue(widget.getHeight());
        heightField.minValue = 10;
        heightField.maxValue = 20;
        addWidget(heightField);

    }

    @Override
    public void onGuiClosed(){
        super.onGuiClosed();
        widget.settingCoordinate = new ChunkPosition(xValueField.getValue(), yValueField.getValue(), zValueField.getValue());
        widget.setWidth(widthField.getValue());
        widget.setHeight(heightField.getValue());
    }
}
