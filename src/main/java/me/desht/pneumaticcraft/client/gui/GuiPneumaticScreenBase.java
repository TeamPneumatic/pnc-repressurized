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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.ITickableWidget;
import me.desht.pneumaticcraft.client.gui.widget.ITooltipProvider;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiPneumaticScreenBase extends Screen {
    public int guiLeft, guiTop, xSize, ySize;
    private final List<Slider> sliders = new ArrayList<>();

    public GuiPneumaticScreenBase(ITextComponent title) {
        super(title);
    }

    @Override
    public void init() {
        super.init();
        guiLeft = width / 2 - xSize / 2;
        guiTop = height / 2 - ySize / 2;
    }

    protected abstract ResourceLocation getTexture();

    protected WidgetLabel addLabel(ITextComponent text, int x, int y) {
        return addLabel(text, x, y, WidgetLabel.Alignment.LEFT);
    }

    protected WidgetLabel addLabel(ITextComponent text, int x, int y, WidgetLabel.Alignment alignment) {
        return addButton(new WidgetLabel(x, y, text).setAlignment(alignment));
    }

    @Override
    protected <T extends Widget> T addButton(T widget) {
        if (widget instanceof Slider) sliders.add((Slider) widget);

        return super.addButton(widget);
    }

    protected void removeWidget(Widget widget) {
        buttons.remove(widget);
        children.remove(widget);
        if (widget instanceof Slider) sliders.remove(widget);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        // if mouse is not over slider, then Slider#onRelease() won't get called to release any in-progress drag
        sliders.forEach(slider -> slider.dragging = false);

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public void tick() {
        super.tick();

        buttons.stream().filter(w -> w instanceof ITickableWidget).forEach(w -> ((ITickableWidget) w).tickWidget());
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        if (getTexture() != null) {
            minecraft.getTextureManager().bind(getTexture());
            blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
        }

        super.render(matrixStack, x, y, partialTicks);

        drawForeground(matrixStack, x, y, partialTicks);

        List<ITextComponent> tooltip = new ArrayList<>();
        boolean shift = Screen.hasShiftDown();
        buttons.stream()
                .filter(widget -> widget instanceof ITooltipProvider && widget.isHovered())
                .forEach(widget -> ((ITooltipProvider) widget).addTooltip(x, y, tooltip, shift));

        if (!tooltip.isEmpty()) {
            int max = Math.min(xSize * 4 / 3, width / 3);
            renderTooltip(matrixStack, GuiUtils.wrapTextComponentList(tooltip, max, font), x, y);
        }
    }

    /**
     * Do GUI-specific foreground drawing here rather than overriding render(), so that tooltips drawn by render are
     * drawn last and stay on top.
     *
     * @param matrixStack the matrix stack
     * @param x mouse X
     * @param y mouse Y
     * @param partialTicks partial ticks
     */
    protected void drawForeground(MatrixStack matrixStack, int x, int y, float partialTicks) {
    }
}
