package me.desht.pneumaticcraft.client.gui.remote.actionwidget;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.common.variables.TextVariableParser;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class WidgetLabelVariable extends WidgetLabel {
    private final TextVariableParser parser;

    public WidgetLabelVariable(int x, int y, ITextComponent text) {
        super(x, y, text);

        this.parser = new TextVariableParser(text.getString());
        this.width = Minecraft.getInstance().fontRenderer.getStringWidth(parser.parse());
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        ITextComponent oldText = getMessage();
        setMessage(new StringTextComponent(parser.parse()));
        super.renderButton(matrixStack, mouseX, mouseY, partialTick);
        setMessage(oldText);
    }
}
