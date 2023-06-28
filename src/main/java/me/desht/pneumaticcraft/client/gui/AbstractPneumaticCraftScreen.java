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

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.client.ITickableWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractPneumaticCraftScreen extends Screen {
    public int guiLeft, guiTop, xSize, ySize;

    public AbstractPneumaticCraftScreen(Component title) {
        super(title);
    }

    @Override
    public void init() {
        super.init();
        guiLeft = width / 2 - xSize / 2;
        guiTop = height / 2 - ySize / 2;
    }

    protected abstract ResourceLocation getTexture();

    protected WidgetLabel addLabel(Component text, int x, int y) {
        return addLabel(text, x, y, WidgetLabel.Alignment.LEFT);
    }

    protected WidgetLabel addLabel(Component text, int x, int y, WidgetLabel.Alignment alignment) {
        return addRenderableWidget(new WidgetLabel(x, y, text).setAlignment(alignment));
    }

    @Override
    public void tick() {
        super.tick();

        renderables.stream().filter(w -> w instanceof ITickableWidget).forEach(w -> ((ITickableWidget) w).tickWidget());
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTicks) {
        if (getTexture() != null) {
            graphics.blit(getTexture(), guiLeft, guiTop, 0, 0, xSize, ySize);
        }

        super.render(graphics, x, y, partialTicks);

        drawForeground(graphics, x, y, partialTicks);

//        List<Component> tooltip = new ArrayList<>();
//        boolean shift = Screen.hasShiftDown();
//        renderables.stream()
//                .filter(widget -> widget instanceof ITooltipProvider provider && provider.shouldProvide())
//                .forEach(widget -> ((ITooltipProvider) widget).addTooltip(x, y, tooltip, shift));
//
//        if (!tooltip.isEmpty()) {
//            int max = Math.min(xSize * 4 / 3, width / 3);
//            renderTooltip(graphics, GuiUtils.wrapTextComponentList(tooltip, max, font), x, y);
//        }
    }

    /**
     * Do GUI-specific foreground drawing here rather than overriding render(), so that tooltips drawn by render are
     * drawn last and stay on top.
     *
     * @param graphics     the matrix stack
     * @param x            mouse X
     * @param y            mouse Y
     * @param partialTicks partial ticks
     */
    protected void drawForeground(GuiGraphics graphics, int x, int y, float partialTicks) {
    }
}
