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

import me.desht.pneumaticcraft.client.util.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class WidgetLabel extends PNCWidget<WidgetLabel> {

    public enum Alignment {
        LEFT, CENTRE, RIGHT
    }

    private float scale = 1.0f;
    private int color;
    private Alignment alignment = Alignment.LEFT;
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

    public WidgetLabel setColor(int color) {
        this.color = color;
        return this;
    }

    public WidgetLabel setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    @Override
    public void setMessage(Component pMessage) {
        super.setMessage(pMessage);

        width = Minecraft.getInstance().font.width(getMessage());
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            int drawX;
            Font fr = Minecraft.getInstance().font;
            drawX = switch (alignment) {
                case LEFT -> getX();
                case CENTRE -> getX() - (int) (width / 2 * scale);
                case RIGHT -> getX() - (int) (width * scale);
            };
            GuiUtils.drawScaledText(graphics, fr, getMessage(), drawX, getY(), color, scale,  dropShadow);
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}
