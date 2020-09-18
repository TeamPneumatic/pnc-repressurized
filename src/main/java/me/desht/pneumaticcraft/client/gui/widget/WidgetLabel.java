package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WidgetLabel extends Widget implements ITooltipProvider {

    public enum Alignment {
        LEFT, CENTRE, RIGHT
    }

    private float scale = 1.0f;
    private int color;
    private Alignment alignment = Alignment.LEFT;
    private List<ITextComponent> tooltip = new ArrayList<>();

    public WidgetLabel(int x, int y, ITextComponent text) {
        this(x, y, text, 0xFF404040);
    }

    public WidgetLabel(int x, int y, ITextComponent text, int color) {
        super(x, y, 0, 0, text);
        this.color = color;
        this.width = Minecraft.getInstance().fontRenderer.getStringPropertyWidth(getMessage());
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
    protected boolean clicked(double mouseX, double mouseY) {
        switch (alignment) {
            case LEFT: return super.clicked(mouseX, mouseY);
            case CENTRE: return super.clicked(mouseX + width / 2d, mouseY);
            case RIGHT: return super.clicked(mouseX + width, mouseY);
            default: return false;
        }
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shift) {
        curTip.addAll(tooltip);
    }

    public WidgetLabel setTooltip(ITextComponent tooltip) {
        return setTooltip(Collections.singletonList(tooltip));
    }

    public WidgetLabel setTooltip(List<ITextComponent> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public List<ITextComponent> getTooltip() {
        return tooltip;
    }

//    public <T extends WidgetLabel> T setTooltipText(String text) {
//        tooltip.clear();
//        if (!text.isEmpty()) {
//            tooltip.addAll(PneumaticCraftUtils.splitStringComponent(I18n.format(text)));
//        }
//        return (T) this;
//    }

//    public String getTooltip() {
//        return tooltip.isEmpty() ? "" : tooltip.get(0);
//    }

    public WidgetLabel setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public void setMessage(ITextComponent p_setMessage_1_) {
        super.setMessage(p_setMessage_1_);

        width = Minecraft.getInstance().fontRenderer.getStringPropertyWidth(getMessage());
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            int drawX;
            FontRenderer fr = Minecraft.getInstance().fontRenderer;
            switch (alignment) {
                case LEFT:
                default:
                    drawX = x;
                    break;
                case CENTRE:
                    drawX = x - (int)(width / 2 * scale);
                    break;
                case RIGHT:
                    drawX = x - (int)(width * scale);
                    break;
            }
            if (scale != 1.0f) {
                matrixStack.push();
                matrixStack.scale(scale, scale, scale);
                matrixStack.translate(drawX, y, 0);
                fr.func_238422_b_(matrixStack, getMessage().func_241878_f(), drawX, y, color);
                matrixStack.pop();
            } else {
                fr.func_238422_b_(matrixStack, getMessage().func_241878_f(), drawX, y, color);
            }
        }
    }
}
