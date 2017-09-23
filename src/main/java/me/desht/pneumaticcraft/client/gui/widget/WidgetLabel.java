package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.client.Minecraft;

import java.awt.*;

public class WidgetLabel extends WidgetBase {
    public String text;

    public WidgetLabel(int x, int y, String text) {
        super(-1, x, y, 0, 0);
        this.text = text;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, 0xFF000000);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, Minecraft.getMinecraft().fontRenderer.getStringWidth(text), Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);
    }

}
