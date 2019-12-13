package me.desht.pneumaticcraft.common.remote;

import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import net.minecraft.client.Minecraft;

public class WidgetLabelVariable extends WidgetLabel {
    private final TextVariableParser parser;

    public WidgetLabelVariable(int x, int y, String text) {
        super(x, y, text);

        this.parser = new TextVariableParser(text);
        this.width = Minecraft.getInstance().fontRenderer.getStringWidth(parser.parse());
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTick) {
        String oldText = getMessage();
        setMessage(parser.parse());
        super.renderButton(mouseX, mouseY, partialTick);
        setMessage(oldText);
    }
}
