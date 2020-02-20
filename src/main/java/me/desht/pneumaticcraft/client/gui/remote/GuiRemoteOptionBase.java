package me.desht.pneumaticcraft.client.gui.remote;

import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidget;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.IActionWidgetLabeled;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiRemoteOptionBase<A extends ActionWidget> extends GuiPneumaticScreenBase {
    protected final A widget;
    final GuiRemoteEditor guiRemote;
    private WidgetTextField labelField, tooltipField;
    private WidgetComboBox enableField;
    private WidgetTextFieldNumber xValueField, yValueField, zValueField;

    public GuiRemoteOptionBase(A widget, GuiRemoteEditor guiRemote) {
        super(new TranslationTextComponent("remote." + widget.getId() + ".name"));

        this.widget = widget;
        this.guiRemote = guiRemote;
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        super.render(mouseX, mouseY, partialTicks);

    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_WIDGET_OPTIONS;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        super.init();

        addLabel(I18n.format("gui.remote.enable"), guiLeft + 10, guiTop + 150);
        String t = title.getFormattedText();
        addLabel(t, width / 2 - font.getStringWidth(t) / 2, guiTop + 5);
        addLabel("#", guiLeft + 10, guiTop + 161);

        if (widget instanceof IActionWidgetLabeled) {
            addLabel(I18n.format("gui.remote.text"), guiLeft + 10, guiTop + 20);
            addLabel(I18n.format("gui.remote.tooltip"), guiLeft + 10, guiTop + 46);
        }

        addLabel(I18n.format("gui.remote.enableValue"), guiLeft + 10, guiTop + 175);
        addLabel("X:", guiLeft + 10, guiTop + 186);
        addLabel("Y:", guiLeft + 67, guiTop + 186);
        addLabel("Z:", guiLeft + 124, guiTop + 186);

        enableField = new WidgetComboBox(font, guiLeft + 18, guiTop + 160, 152, 10);
        enableField.setElements(guiRemote.getContainer().variables);
        enableField.setText(widget.getEnableVariable());
        enableField.setTooltip(I18n.format("gui.remote.enable.tooltip"));
        addButton(enableField);

        String valueTooltip = I18n.format("gui.remote.enableValue.tooltip");

        xValueField = new WidgetTextFieldNumber(font, guiLeft + 20, guiTop + 185, 38, 10);
        xValueField.setValue(widget.getEnablingValue().getX());
        xValueField.setTooltip(valueTooltip);
        addButton(xValueField);

        yValueField = new WidgetTextFieldNumber(font, guiLeft + 78, guiTop + 185, 38, 10);
        yValueField.setValue(widget.getEnablingValue().getY());
        yValueField.setTooltip(valueTooltip);
        addButton(yValueField);

        zValueField = new WidgetTextFieldNumber(font, guiLeft + 136, guiTop + 185, 38, 10);
        zValueField.setValue(widget.getEnablingValue().getZ());
        zValueField.setTooltip(valueTooltip);
        addButton(zValueField);

        if (widget instanceof IActionWidgetLabeled) {
            labelField = new WidgetTextField(font, guiLeft + 10, guiTop + 30, 160, 10);
            labelField.setText(((IActionWidgetLabeled) widget).getText());
            labelField.setTooltip(I18n.format("gui.remote.label.tooltip"));
            labelField.setMaxStringLength(1000);
            addButton(labelField);

            tooltipField = new WidgetTextField(font, guiLeft + 10, guiTop + 56, 160, 10);
            tooltipField.setText(((IActionWidgetLabeled) widget).getTooltip());
            addButton(tooltipField);
        }
    }

    @Override
    public void onClose() {
//        super.onClose();

        widget.setEnableVariable(enableField.getText());
        widget.setEnablingValue(xValueField.getValue(), yValueField.getValue(), zValueField.getValue());
        if (widget instanceof IActionWidgetLabeled) {
            ((IActionWidgetLabeled) widget).setText(labelField.getText());
            ((IActionWidgetLabeled) widget).setTooltip(tooltipField.getText());
        }

        minecraft.displayGuiScreen(guiRemote);
    }
}
