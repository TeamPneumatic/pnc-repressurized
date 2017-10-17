package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.client.Minecraft;

import java.awt.*;

public class WidgetLabel extends WidgetBase {
    public String text;
    private final int color;

    public WidgetLabel(int x, int y, String text) {
        this(x, y, text, 0xFF404040);
    }

    public WidgetLabel(int x, int y, String text, int color) {
        super(-1, x, y, 0, 0);
        this.text = text;
        this.color = color;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, color);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, Minecraft.getMinecraft().fontRenderer.getStringWidth(text), Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);
    }

}
