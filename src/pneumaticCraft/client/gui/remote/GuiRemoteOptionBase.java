package pneumaticCraft.client.gui.remote;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.gui.GuiPneumaticScreenBase;
import pneumaticCraft.client.gui.GuiRemoteEditor;
import pneumaticCraft.client.gui.widget.WidgetComboBox;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.common.inventory.ContainerRemote;
import pneumaticCraft.common.remote.ActionWidget;
import pneumaticCraft.common.remote.IActionWidgetLabeled;
import pneumaticCraft.lib.Textures;

public class GuiRemoteOptionBase<Widget extends ActionWidget> extends GuiPneumaticScreenBase{
    protected Widget widget;
    protected GuiRemoteEditor guiRemote;
    private WidgetTextField labelField, tooltipField;
    private WidgetComboBox enableField;

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
        addLabel(I18n.format("gui.remote.enable"), guiLeft + 10, guiTop + 170);
        addLabel(title, width / 2 - fontRendererObj.getStringWidth(title) / 2, guiTop + 5);
        addLabel("#", guiLeft + 10, guiTop + 181);

        if(widget instanceof IActionWidgetLabeled) {
            addLabel(I18n.format("gui.remote.text"), guiLeft + 10, guiTop + 20);
            addLabel(I18n.format("gui.remote.tooltip"), guiLeft + 10, guiTop + 46);
        }

        enableField = new WidgetComboBox(fontRendererObj, guiLeft + 18, guiTop + 180, 152, 10);
        enableField.setElements(((ContainerRemote)guiRemote.inventorySlots).variables);
        enableField.setText(widget.getEnableVariable());
        enableField.setTooltip(I18n.format("gui.remote.enable.tooltip"));
        addWidget(enableField);

        if(widget instanceof IActionWidgetLabeled) {
            labelField = new WidgetTextField(fontRendererObj, guiLeft + 10, guiTop + 30, 160, 10);
            labelField.setText(((IActionWidgetLabeled)widget).getText());
            labelField.setTooltip(I18n.format("gui.remote.label.tooltip"));
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
        if(widget instanceof IActionWidgetLabeled) {
            ((IActionWidgetLabeled)widget).setText(labelField.getText());
            ((IActionWidgetLabeled)widget).setTooltip(tooltipField.getText());
        }
    }
}
