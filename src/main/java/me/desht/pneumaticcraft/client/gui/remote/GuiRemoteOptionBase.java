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

public class GuiRemoteOptionBase<A extends ActionWidget<?>> extends GuiPneumaticScreenBase {
    protected final A actionWidget;
    final GuiRemoteEditor guiRemote;
    private WidgetTextField labelField, tooltipField;
    private WidgetComboBox enableField;
    private WidgetTextFieldNumber xValueField, yValueField, zValueField;

    public GuiRemoteOptionBase(A actionWidget, GuiRemoteEditor guiRemote) {
        super(new TranslationTextComponent("remote." + actionWidget.getId() + ".name"));

        this.actionWidget = actionWidget;
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

        addLabel(I18n.format("pneumaticcraft.gui.remote.enable"), guiLeft + 10, guiTop + 150);
        String t = title.getFormattedText();
        addLabel(t, width / 2 - font.getStringWidth(t) / 2, guiTop + 5);
        addLabel("#", guiLeft + 10, guiTop + 161);

        if (actionWidget instanceof IActionWidgetLabeled) {
            addLabel(I18n.format("pneumaticcraft.gui.remote.text"), guiLeft + 10, guiTop + 20);
            addLabel(I18n.format("pneumaticcraft.gui.remote.tooltip"), guiLeft + 10, guiTop + 46);
        }

        addLabel(I18n.format("pneumaticcraft.gui.remote.enableValue"), guiLeft + 10, guiTop + 175);
        addLabel("X:", guiLeft + 10, guiTop + 186);
        addLabel("Y:", guiLeft + 67, guiTop + 186);
        addLabel("Z:", guiLeft + 124, guiTop + 186);

        enableField = new WidgetComboBox(font, guiLeft + 18, guiTop + 160, 152, 10);
        enableField.setElements(guiRemote.getContainer().variables);
        enableField.setText(actionWidget.getEnableVariable());
        enableField.setTooltip(I18n.format("pneumaticcraft.gui.remote.enable.tooltip"));
        addButton(enableField);

        String valueTooltip = I18n.format("pneumaticcraft.gui.remote.enableValue.tooltip");

        xValueField = new WidgetTextFieldNumber(font, guiLeft + 20, guiTop + 185, 38, 10);
        xValueField.setValue(actionWidget.getEnablingValue().getX());
        xValueField.setTooltip(valueTooltip);
        addButton(xValueField);

        yValueField = new WidgetTextFieldNumber(font, guiLeft + 78, guiTop + 185, 38, 10);
        yValueField.setValue(actionWidget.getEnablingValue().getY());
        yValueField.setTooltip(valueTooltip);
        addButton(yValueField);

        zValueField = new WidgetTextFieldNumber(font, guiLeft + 136, guiTop + 185, 38, 10);
        zValueField.setValue(actionWidget.getEnablingValue().getZ());
        zValueField.setTooltip(valueTooltip);
        addButton(zValueField);

        if (actionWidget instanceof IActionWidgetLabeled) {
            labelField = new WidgetTextField(font, guiLeft + 10, guiTop + 30, 160, 10);
            labelField.setText(((IActionWidgetLabeled) actionWidget).getText());
            labelField.setTooltip(I18n.format("pneumaticcraft.gui.remote.label.tooltip"));
            labelField.setMaxStringLength(1000);
            addButton(labelField);

            tooltipField = new WidgetTextField(font, guiLeft + 10, guiTop + 56, 160, 10);
            tooltipField.setText(((IActionWidgetLabeled) actionWidget).getTooltip());
            addButton(tooltipField);
        }
    }

    @Override
    public void onClose() {
        actionWidget.setEnableVariable(enableField.getText());
        actionWidget.setEnablingValue(xValueField.getValue(), yValueField.getValue(), zValueField.getValue());
        if (actionWidget instanceof IActionWidgetLabeled) {
            ((IActionWidgetLabeled) actionWidget).setText(labelField.getText());
            ((IActionWidgetLabeled) actionWidget).setTooltip(tooltipField.getText());
        }

        minecraft.displayGuiScreen(guiRemote);
    }
}
