package me.desht.pneumaticcraft.client.gui.remote;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.GuiRemoteEditor;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.ActionWidget;
import me.desht.pneumaticcraft.client.gui.remote.actionwidget.IActionWidgetLabeled;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
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
    private WidgetButtonExtended enableVarTypeButton;
    private boolean playerGlobalEnableVar;

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

        minecraft.keyboardListener.enableRepeatEvents(true);

        playerGlobalEnableVar = actionWidget.getEnableVariable().isEmpty() || actionWidget.getEnableVariable().startsWith("#");

        addLabel(xlate("pneumaticcraft.gui.remote.enable"), guiLeft + 10, guiTop + 150);
        addLabel(title, width / 2, guiTop + 5, WidgetLabel.Alignment.CENTRE);
        enableVarTypeButton = new WidgetButtonExtended(guiLeft + 10, guiTop + 158, 12, 14, getVarPrefix(playerGlobalEnableVar),
                b -> togglePlayerGlobalEnable()).setTooltipKey("pneumaticcraft.gui.remote.varType.tooltip");
        addButton(enableVarTypeButton);

        if (actionWidget instanceof IActionWidgetLabeled) {
            addLabel(xlate("pneumaticcraft.gui.remote.text"), guiLeft + 10, guiTop + 20);
            addLabel(xlate("pneumaticcraft.gui.remote.tooltip"), guiLeft + 10, guiTop + 46);
        }

        addLabel(xlate("pneumaticcraft.gui.remote.enableValue"), guiLeft + 10, guiTop + 175);
        addLabel(new StringTextComponent("X:"), guiLeft + 10, guiTop + 186);
        addLabel(new StringTextComponent("Y:"), guiLeft + 67, guiTop + 186);
        addLabel(new StringTextComponent("Z:"), guiLeft + 124, guiTop + 186);

        enableField = new WidgetComboBox(font, guiLeft + 23, guiTop + 160, 147, 10);
        enableField.setElements(extractVarnames(guiRemote.getContainer().variables, playerGlobalEnableVar));
        enableField.setText(stripVarPrefix(actionWidget.getEnableVariable()));
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
            labelField.setText(((IActionWidgetLabeled) actionWidget).getText().getString());
            labelField.setTooltip(xlate("pneumaticcraft.gui.remote.label.tooltip"));
            labelField.setMaxStringLength(1000);
            addButton(labelField);

            tooltipField = new WidgetTextField(font, guiLeft + 10, guiTop + 56, 160, 10);

            String joined = ((IActionWidgetLabeled) actionWidget).getTooltip().stream()
                    .map(ITextComponent::getString)
                    .collect(Collectors.joining(TOOLTIP_DELIMITER));
            tooltipField.setText(joined);
            addButton(tooltipField);
        }
    }

    @Override
    public void onClose() {
        minecraft.keyboardListener.enableRepeatEvents(false);

        actionWidget.setEnableVariable(getPrefixedVar(enableField.getText(), playerGlobalEnableVar));
        actionWidget.setEnablingValue(xValueField.getValue(), yValueField.getValue(), zValueField.getValue());
        if (actionWidget instanceof IActionWidgetLabeled) {
            ((IActionWidgetLabeled) actionWidget).setText(new StringTextComponent(labelField.getText()));
            if (tooltipField.getText().isEmpty()) {
                ((IActionWidgetLabeled) actionWidget).setTooltip(Collections.emptyList());
            } else {
                List<ITextComponent> l = Arrays.stream(tooltipField.getText().split(TOOLTIP_DELIMITER))
                        .map(StringTextComponent::new)
                        .collect(Collectors.toList());
                ((IActionWidgetLabeled) actionWidget).setTooltip(l);
            }
        }
    }

    private void togglePlayerGlobalEnable() {
        playerGlobalEnableVar = !playerGlobalEnableVar;
        enableVarTypeButton.setMessage(new StringTextComponent(getVarPrefix(playerGlobalEnableVar)));
        enableField.setElements(extractVarnames(guiRemote.getContainer().variables, playerGlobalEnableVar));
    }

    @Override
    public void closeScreen() {
        minecraft.displayGuiScreen(guiRemote);
    }

    String getPrefixedVar(String varName, boolean playerGlobal) {
        return varName.isEmpty() ? "" : getVarPrefix(playerGlobal) + varName;
    }

    String getVarPrefix(boolean playerGlobal) {
        return playerGlobal ? "#" : "%";
    }

    String stripVarPrefix(String varName) {
        return varName.startsWith("#") || varName.startsWith("%") ? varName.substring(1) : varName;
    }

    List<String> extractVarnames(String[] varnames, boolean playerGlobal) {
        List<String> res = new ArrayList<>();
        for (String v : varnames) {
            if (playerGlobal && v.startsWith("#") || !playerGlobal && v.startsWith("%")) {
               res.add(v.substring(1));
            }
        }
        return res;
    }
}
