package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WidgetTextField extends TextFieldWidget implements ITooltipProvider {

    private final List<ITextComponent> tooltip = new ArrayList<>();
    private boolean passwordBox;

    public WidgetTextField(FontRenderer fontRenderer, int x, int y, int width, int height) {
        super(fontRenderer, x, y, width, height, StringTextComponent.EMPTY);
    }

    public WidgetTextField setAsPasswordBox() {
        passwordBox = true;
        return this;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        String oldText = getText();
        int oldCursorPos = getCursorPosition();
        if (passwordBox) {
            setText(StringUtils.repeat('*', oldText.length()));
            setCursorPosition(oldCursorPos);
        }
        super.renderButton(matrixStack, mouseX, mouseY, partialTick);
        if (passwordBox) {
            setText(oldText);
            setCursorPosition(oldCursorPos);
        }
    }

    public void setTooltip(ITextComponent... tooltip) {
        this.tooltip.clear();
        Collections.addAll(this.tooltip, tooltip);
    }

    public void setTooltip(List<ITextComponent> tooltip) {
        this.tooltip.clear();
        this.tooltip.addAll(tooltip);
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shift) {
        if (!isFocused()) {
            // hide tooltip when actually typing; it just gets in the way
            curTip.addAll(tooltip);
        }
    }
}
