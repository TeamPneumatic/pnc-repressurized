package me.desht.pneumaticcraft.common.remote;

import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;

public class WidgetLabelVariable extends WidgetLabel {
    private final TextVariableParser parser;

    public WidgetLabelVariable(int x, int y, String text) {
        super(x, y, text);
        parser = new TextVariableParser(text);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        String oldText = getMessage();
        setMessage(parser.parse());
        super.render(mouseX, mouseY, partialTick);
        setMessage(oldText);
    }

    @Override
    public Rectangle getBounds() {
        FontRenderer fr = Minecraft.getInstance().fontRenderer;
        return new Rectangle(x, y, fr.getStringWidth(parser.parse()), fr.FONT_HEIGHT);
    }
}
