/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

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
    private boolean dropShadow = false;

    public WidgetLabel(int x, int y, ITextComponent text) {
        this(x, y, text, 0xFF404040);
    }

    public WidgetLabel(int x, int y, ITextComponent text, int color) {
        super(x, y, 0, 0, text);
        this.color = color;
        this.width = Minecraft.getInstance().font.width(getMessage());
        this.height = Minecraft.getInstance().font.lineHeight;
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

    public WidgetLabel setColor(int color) {
        this.color = color;
        return this;
    }

    public WidgetLabel setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    @Override
    public void setMessage(ITextComponent p_setMessage_1_) {
        super.setMessage(p_setMessage_1_);

        width = Minecraft.getInstance().font.width(getMessage());
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            int drawX;
            FontRenderer fr = Minecraft.getInstance().font;
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
                matrixStack.pushPose();
                matrixStack.scale(scale, scale, scale);
                matrixStack.translate(drawX, y, 0);
            }
            if (dropShadow) {
                fr.drawShadow(matrixStack, getMessage().getVisualOrderText(), drawX, y, color);
            } else {
                fr.draw(matrixStack, getMessage().getVisualOrderText(), drawX, y, color);
            }
            if (scale != 1.0f) {
                matrixStack.popPose();
            }
        }
    }
}
