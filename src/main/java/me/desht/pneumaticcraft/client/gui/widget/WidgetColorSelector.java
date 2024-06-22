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

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import java.util.function.Consumer;

public class WidgetColorSelector extends WidgetButtonExtended implements IDrawAfterRender {
    private boolean expanded = false;
    private DyeColor color = DyeColor.WHITE;
    private final Rect2i mainArea;
    private final Rect2i expandedArea;
    private final Consumer<WidgetColorSelector> callback;

    public WidgetColorSelector(int xIn, int yIn) {
        this(xIn, yIn, null);
    }

    public WidgetColorSelector(int xIn, int yIn, Consumer<WidgetColorSelector> callback) {
        super(xIn, yIn, 16, 16, Component.empty());

        mainArea = new Rect2i(xIn, yIn, width, height);
        expandedArea = new Rect2i(xIn, yIn + height, width * 4, height * 4);

        this.callback = callback;
    }

    public WidgetColorSelector withInitialColor(DyeColor color) {
        this.color = color;
        return this;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public DyeColor getColor() {
        return color;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        int x = getX(), y = getY();
        graphics.fill(x + 3, y + 3, x + width - 4, y + height - 4, 0xFF000000 | color.getTextureDiffuseColor());
        graphics.hLine(x + 3, x + width - 3, y + height - 4, 0xFF606060);
        graphics.vLine(x + width - 4, y + 3, y + height - 3, 0xFF606060);
    }

    @Override
    public void renderAfterEverythingElse(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (expanded) {
            int x = getX(), y = getY();
            graphics.fill(x, y - 1 + height, x + width * 4, y -1 + height * 5, 0xFF000000);
            graphics.fill(x + 1, y + height, x + width * 4 - 1, y - 2 + height * 5, 0xFF808080);
            for (DyeColor color : DyeColor.values()) {
                int dx = x + (color.getId() % 4) * 16;
                int dy = y - 1 + height + (color.getId() / 4) * 16;
                graphics.fill(dx + 3, dy + 3, dx + 13, dy + 13, 0xFF000000 | color.getTextureDiffuseColor());
                graphics.hLine(dx + 3, dx + 13, dy + 13, 0xFF606060);
                graphics.vLine(dx + 13, dy + 3, dy + 13, 0xFF606060);
            }
        }
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return super.clicked(mouseX, mouseY) || expanded && expandedArea.contains((int) mouseX, (int) mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (mainArea.contains((int) mouseX, (int) mouseY)) {
            expanded = !expanded;
        } else if (expandedArea.contains((int) mouseX, (int) mouseY)) {
            int dx = (int)mouseX - expandedArea.getX();
            int dy = (int)mouseY - expandedArea.getY();
            int id = dx / 16 + (dy / 16) * 4;
            color = DyeColor.byId(id);
            expanded = !expanded;
            if (callback != null) {
                callback.accept(this);
            }
        }
    }
}
