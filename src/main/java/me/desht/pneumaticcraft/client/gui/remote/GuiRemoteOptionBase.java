package me.desht.pneumaticcraft.client.gui.remote;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidget;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.IActionWidgetLabeled;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiRemoteOptionBase<A extends ActionWidget<?>> extends GuiPneumaticScreenBase {
    private static final String TOOLTIP_DELIMITER = "//";

    protected final A actionWidget;
    final GuiRemoteEditor guiRemote;
    private WidgetTextField labelField, tooltipField;
    private WidgetComboBox enableField;
    private WidgetTextFieldNumber xValueField, yValueField, zValueField;

    public GuiRemoteOptionBase(A actionWidget, GuiRemoteEditor guiRemote) {
        super(new TranslationTextComponent("pneumaticcraft.gui.remote.tray." + actionWidget.getId() + ".name"));

        this.actionWidget = actionWidget;
        this.guiRemote = guiRemote;
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
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

        minecraft.keyboardHandler.setSendRepeatsToGui(true);

        addLabel(xlate("pneumaticcraft.gui.remote.enable"), guiLeft + 10, guiTop + 150);
        addLabel(title, width / 2, guiTop + 5, WidgetLabel.Alignment.CENTRE);
        addLabel(new StringTextComponent("#"), guiLeft + 10, guiTop + 161);

        if (actionWidget instanceof IActionWidgetLabeled) {
            addLabel(xlate("pneumaticcraft.gui.remote.text"), guiLeft + 10, guiTop + 20);
            addLabel(xlate("pneumaticcraft.gui.remote.tooltip"), guiLeft + 10, guiTop + 46);
        }

        addLabel(xlate("pneumaticcraft.gui.remote.enableValue"), guiLeft + 10, guiTop + 175);
        addLabel(new StringTextComponent("X:"), guiLeft + 10, guiTop + 186);
        addLabel(new StringTextComponent("Y:"), guiLeft + 67, guiTop + 186);
        addLabel(new StringTextComponent("Z:"), guiLeft + 124, guiTop + 186);

        enableField = new WidgetComboBox(font, guiLeft + 18, guiTop + 160, 152, 10);
        enableField.setElements(guiRemote.getMenu().variables);
        enableField.setValue(actionWidget.getEnableVariable());
        enableField.setTooltip(xlate("pneumaticcraft.gui.remote.enable.tooltip"));
        addButton(enableField);

        ITextComponent valueTooltip = xlate("pneumaticcraft.gui.remote.enableValue.tooltip");

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
            labelField.setValue(((IActionWidgetLabeled) actionWidget).getText().getString());
            labelField.setTooltip(xlate("pneumaticcraft.gui.remote.label.tooltip"));
            labelField.setMaxLength(1000);
            addButton(labelField);

            tooltipField = new WidgetTextField(font, guiLeft + 10, guiTop + 56, 160, 10);

            String joined = ((IActionWidgetLabeled) actionWidget).getTooltip().stream()
                    .map(ITextComponent::getString)
                    .collect(Collectors.joining(TOOLTIP_DELIMITER));
            tooltipField.setValue(joined);
            addButton(tooltipField);
        }
    }

    @Override
    public void removed() {
        minecraft.keyboardHandler.setSendRepeatsToGui(false);

        actionWidget.setEnableVariable(enableField.getValue());
        actionWidget.setEnablingValue(xValueField.getIntValue(), yValueField.getIntValue(), zValueField.getIntValue());
        if (actionWidget instanceof IActionWidgetLabeled) {
            ((IActionWidgetLabeled) actionWidget).setText(new StringTextComponent(labelField.getValue()));
            if (tooltipField.getValue().isEmpty()) {
                ((IActionWidgetLabeled) actionWidget).setTooltip(Collections.emptyList());
            } else {
                List<ITextComponent> l = Arrays.stream(tooltipField.getValue().split(TOOLTIP_DELIMITER))
                        .map(StringTextComponent::new)
                        .collect(Collectors.toList());
                ((IActionWidgetLabeled) actionWidget).setTooltip(l);
            }
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(guiRemote);
    }
}
