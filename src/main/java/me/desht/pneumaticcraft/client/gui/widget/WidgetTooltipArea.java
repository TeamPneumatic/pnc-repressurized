package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collections;
import java.util.List;

public class WidgetTooltipArea extends Widget implements ITooltipProvider {
    public final ITextComponent[] tooltip;

    public WidgetTooltipArea(int x, int y, int width, int height, ITextComponent... tooltip) {
        super(x, y, width, height, StringTextComponent.EMPTY);
        this.tooltip = tooltip;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_) {
        // nothing
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shiftPressed) {
        Collections.addAll(curTip, tooltip);
    }
}
