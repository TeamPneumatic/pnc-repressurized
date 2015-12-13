package pneumaticCraft.client.gui.remote;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.gui.GuiPneumaticScreenBase;
import pneumaticCraft.client.gui.GuiRemoteEditor;
import pneumaticCraft.client.gui.widget.WidgetComboBox;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.client.gui.widget.WidgetTextFieldNumber;
import pneumaticCraft.common.inventory.ContainerRemote;
import pneumaticCraft.common.remote.ActionWidget;
import pneumaticCraft.common.remote.IActionWidgetLabeled;
import pneumaticCraft.lib.Textures;

public class GuiRemoteOptionBase<Widget extends ActionWidget> extends GuiPneumaticScreenBase{
    protected Widget widget;
    protected GuiRemoteEditor guiRemote;
    private WidgetTextField labelField, tooltipField;
    private WidgetComboBox enableField;
    private WidgetTextFieldNumber xValueField, yValueField, zValueField;

    public GuiRemoteOptionBase(Widget widget, GuiRemoteEditor guiRemote){
        this.widget = widget;
        this.guiRemote = guiRemote;
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void keyTyped(char key, int keyCode){
        if(keyCode == 1) {
            onGuiClosed();
            mc.displayGuiScreen(guiRemote);
        } else {
            super.keyTyped(key, keyCode);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.GUI_WIDGET_OPTIONS;
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }

    @Override
    public void initGui(){
        super.initGui();

        String title = I18n.format("remote." + widget.getId() + ".name");
        addLabel(I18n.format("gui.remote.enable"), guiLeft + 10, guiTop + 150);
        addLabel(title, width / 2 - fontRendererObj.getStringWidth(title) / 2, guiTop + 5);
        addLabel("#", guiLeft + 10, guiTop + 161);

        if(widget instanceof IActionWidgetLabeled) {
            addLabel(I18n.format("gui.remote.text"), guiLeft + 10, guiTop + 20);
            addLabel(I18n.format("gui.remote.tooltip"), guiLeft + 10, guiTop + 46);
        }

        addLabel(I18n.format("gui.remote.enableValue"), guiLeft + 10, guiTop + 175);
        addLabel("X:", guiLeft + 10, guiTop + 186);
        addLabel("Y:", guiLeft + 67, guiTop + 186);
        addLabel("Z:", guiLeft + 124, guiTop + 186);

        enableField = new WidgetComboBox(fontRendererObj, guiLeft + 18, guiTop + 160, 152, 10);
        enableField.setElements(((ContainerRemote)guiRemote.inventorySlots).variables);
        enableField.setText(widget.getEnableVariable());
        enableField.setTooltip(I18n.format("gui.remote.enable.tooltip"));
        addWidget(enableField);

        String valueTooltip = I18n.format("gui.remote.enableValue.tooltip");

        xValueField = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 20, guiTop + 185, 38, 10);
        xValueField.setValue(widget.getEnablingValue().chunkPosX);
        xValueField.setTooltip(valueTooltip);
        addWidget(xValueField);

        yValueField = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 78, guiTop + 185, 38, 10);
        yValueField.setValue(widget.getEnablingValue().chunkPosY);
        yValueField.setTooltip(valueTooltip);
        addWidget(yValueField);

        zValueField = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 136, guiTop + 185, 38, 10);
        zValueField.setValue(widget.getEnablingValue().chunkPosZ);
        zValueField.setTooltip(valueTooltip);
        addWidget(zValueField);

        if(widget instanceof IActionWidgetLabeled) {
            labelField = new WidgetTextField(fontRendererObj, guiLeft + 10, guiTop + 30, 160, 10);
            labelField.setText(((IActionWidgetLabeled)widget).getText());
            labelField.setTooltip(I18n.format("gui.remote.label.tooltip"));
            labelField.setMaxStringLength(1000);
            addWidget(labelField);

            tooltipField = new WidgetTextField(fontRendererObj, guiLeft + 10, guiTop + 56, 160, 10);
            tooltipField.setText(((IActionWidgetLabeled)widget).getTooltip());
            addWidget(tooltipField);
        }
    }

    @Override
    public void onGuiClosed(){
        super.onGuiClosed();
        widget.setEnableVariable(enableField.getText());
        widget.setEnablingValue(xValueField.getValue(), yValueField.getValue(), zValueField.getValue());
        if(widget instanceof IActionWidgetLabeled) {
            ((IActionWidgetLabeled)widget).setText(labelField.getText());
            ((IActionWidgetLabeled)widget).setTooltip(tooltipField.getText());
        }
    }
}
