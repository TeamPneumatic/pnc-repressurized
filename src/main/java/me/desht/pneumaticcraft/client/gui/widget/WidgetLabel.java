package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public class WidgetLabel extends Widget implements ITooltipProvider {
    public enum Alignment {
        LEFT, CENTRE, RIGHT
    }

    private float scale = 1.0f;
    private int color;
    private Alignment alignment = Alignment.LEFT;
    private final List<String> tooltip = new ArrayList<>();
    public WidgetLabel(int x, int y, String text) {
        this(x, y, text, 0xFF404040);
    }

    public WidgetLabel(int x, int y, String text, int color) {
        super(x, y, 0, 0, text);
        this.color = color;
        this.width = Minecraft.getInstance().fontRenderer.getStringWidth(getMessage());
        this.height = Minecraft.getInstance().fontRenderer.FONT_HEIGHT;
    }

    public WidgetLabel setAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public WidgetLabel setScale(float scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shift) {
        curTip.addAll(tooltip);
    }

    public void setTooltipText(String text) {
        tooltip.clear();
        tooltip.addAll(PneumaticCraftUtils.splitString(I18n.format(text), 35));
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void setMessage(String p_setMessage_1_) {
        super.setMessage(p_setMessage_1_);

        width = Minecraft.getInstance().fontRenderer.getStringWidth(getMessage());
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        if (visible) {
            int drawX;
            FontRenderer fr = Minecraft.getInstance().fontRenderer;
            switch (alignment) {
                case LEFT:
                default:
                    drawX = x;
                    break;
                case CENTRE:
                    drawX = x - (int)(fr.getStringWidth(getMessage()) / 2 * scale);
                    break;
                case RIGHT:
                    drawX = x - (int)(fr.getStringWidth(getMessage()) * scale);
                    break;
            }
            if (scale != 1.0f) {
                GlStateManager.pushMatrix();
                GlStateManager.scaled(scale, scale, scale);
                GlStateManager.translated(drawX, y, 0);
                fr.drawString(getMessage(), drawX, y, color);
                GlStateManager.popMatrix();
            } else {
                fr.drawString(getMessage(), drawX, y, color);
            }
        }
    }
}
