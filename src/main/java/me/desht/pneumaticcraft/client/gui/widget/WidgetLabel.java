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

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WidgetLabel extends AbstractWidget implements ITooltipProvider {

    public enum Alignment {
        LEFT, CENTRE, RIGHT
    }

    private float scale = 1.0f;
    private int color;
    private Alignment alignment = Alignment.LEFT;
    private List<Component> tooltip = new ArrayList<>();
    private boolean dropShadow = false;

    public WidgetLabel(int x, int y, Component text) {
        this(x, y, text, 0xFF404040);
    }

    public WidgetLabel(int x, int y, Component text, int color) {
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
        return switch (alignment) {
            case LEFT -> super.clicked(mouseX, mouseY);
            case CENTRE -> super.clicked(mouseX + width / 2d, mouseY);
            case RIGHT -> super.clicked(mouseX + width, mouseY);
        };
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<Component> curTip, boolean shift) {
        curTip.addAll(tooltip);
    }

    public WidgetLabel setTooltip(Component tooltip) {
        return setTooltip(Collections.singletonList(tooltip));
    }

    public WidgetLabel setTooltip(List<Component> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public List<Component> getTooltip() {
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
    public void setMessage(Component p_setMessage_1_) {
        super.setMessage(p_setMessage_1_);

        width = Minecraft.getInstance().font.width(getMessage());
    }

    @Override
    public void renderWidget(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            int drawX;
            Font fr = Minecraft.getInstance().font;
            drawX = switch (alignment) {
                case LEFT -> getX();
                case CENTRE -> getX() - (int) (width / 2 * scale);
                case RIGHT -> getX() - (int) (width * scale);
            };
            if (scale != 1.0f) {
                matrixStack.pushPose();
                matrixStack.scale(scale, scale, scale);
                matrixStack.translate(drawX, getY(), 0);
            }
            if (dropShadow) {
                fr.drawShadow(matrixStack, getMessage().getVisualOrderText(), drawX, getY(), color);
            } else {
                fr.draw(matrixStack, getMessage().getVisualOrderText(), drawX, getY(), color);
            }
            if (scale != 1.0f) {
                matrixStack.popPose();
            }
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}
