package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;

public class WidgetLabel extends WidgetBase {
    public enum Alignment {
        LEFT, CENTRE, RIGHT
    }
    public String text;
    private int color;
    private Alignment alignment = Alignment.LEFT;

    public WidgetLabel(int x, int y, String text) {
        this(x, y, text, 0xFF404040);
    }

    public WidgetLabel(int x, int y, String text, int color) {
        super(-1, x, y, 0, 0);
        this.text = text;
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
        int drawX;
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        switch (alignment) {
            case LEFT: default:
                drawX = x;
                break;
            case CENTRE:
                drawX = x - fr.getStringWidth(text) / 2;
                break;
            case RIGHT:
                drawX = x - fr.getStringWidth(text);
                break;
        }
        fr.drawString(text, drawX, y, color);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, Minecraft.getMinecraft().fontRenderer.getStringWidth(text), Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);
    }

}
