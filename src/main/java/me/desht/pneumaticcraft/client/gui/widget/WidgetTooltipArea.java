package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.client.gui.widget.Widget;

import java.util.Collections;
import java.util.List;

public class WidgetTooltipArea extends Widget implements ITooltipProvider {
    public final String[] tooltip;

    public WidgetTooltipArea(int x, int y, int width, int height, String... tooltip) {
        super(x, y, width, height, "");
        this.tooltip = tooltip;
    }

    @Override
    public void renderButton(int p_render_1_, int p_render_2_, float p_render_3_) {
        // nothing
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shiftPressed) {
        Collections.addAll(curTip, tooltip);
    }
}
