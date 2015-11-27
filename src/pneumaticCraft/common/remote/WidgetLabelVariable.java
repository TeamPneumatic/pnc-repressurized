package pneumaticCraft.common.remote;

import java.awt.Rectangle;

import net.minecraft.client.Minecraft;
import pneumaticCraft.client.gui.widget.WidgetLabel;

public class WidgetLabelVariable extends WidgetLabel{
    private final TextVariableParser parser;

    public WidgetLabelVariable(int x, int y, String text){
        super(x, y, text);
        parser = new TextVariableParser(text);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick){
        String oldText = text;
        text = parser.parse();
        super.render(mouseX, mouseY, partialTick);
        text = oldText;
    }

    @Override
    public Rectangle getBounds(){
        return new Rectangle(x, y, Minecraft.getMinecraft().fontRenderer.getStringWidth(parser.parse()), Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT);
    }
}
