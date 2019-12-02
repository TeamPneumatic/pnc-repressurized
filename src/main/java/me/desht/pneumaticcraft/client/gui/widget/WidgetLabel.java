package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;

import java.awt.*;

public class WidgetLabel extends Widget {
    public enum Alignment {
        LEFT, CENTRE, RIGHT
    }
    private int color;
    private Alignment alignment = Alignment.LEFT;
    public boolean visible = true;

    public WidgetLabel(int x, int y, String text) {
        this(x, y, text, 0xFF404040);
    }

    public WidgetLabel(int x, int y, String text, int color) {
        super(x, y, 0, 0, text);
        this.color = color;
    }

    public WidgetLabel setAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        if (visible) {
            int drawX;
            FontRenderer fr = Minecraft.getInstance().fontRenderer;
            switch (alignment) {
                case LEFT:
                default:
                    drawX = x;
                    break;
                case CENTRE:
                    drawX = x - fr.getStringWidth(getMessage()) / 2;
                    break;
                case RIGHT:
                    drawX = x - fr.getStringWidth(getMessage());
                    break;
            }
            fr.drawString(getMessage(), drawX, y, color);
        }
    }

    public Rectangle getBounds() {
        FontRenderer fr = Minecraft.getInstance().fontRenderer;
        return new Rectangle(x, y, fr.getStringWidth(getMessage()), fr.FONT_HEIGHT);
    }

}
