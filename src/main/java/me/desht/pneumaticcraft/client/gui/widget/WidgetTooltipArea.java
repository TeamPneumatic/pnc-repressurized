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
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.function.Supplier;

public class WidgetTooltipArea extends AbstractWidget {
    private final Supplier<Tooltip> tooltipSupplier;

    public WidgetTooltipArea(int x, int y, int width, int height, Component... tooltip) {
        super(x, y, width, height, Component.empty());

        tooltipSupplier = null;
        setTooltip(Tooltip.create(PneumaticCraftUtils.combineComponents(Arrays.asList(tooltip))));
    }

    public WidgetTooltipArea(int x, int y, int width, int height, Supplier<Tooltip> tooltipSupplier) {
        super(x, y, width, height, Component.empty());
        this.tooltipSupplier = tooltipSupplier;
    }


    @Override
    public void renderWidget(GuiGraphics matrixStack, int p_render_1_, int p_render_2_, float p_render_3_) {
        if (tooltipSupplier != null) {
            setTooltip(tooltipSupplier.get());
        }
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        // not clickable: any mouse clicks should be passed through to any other widget in this positions
        return false;
    }

    @Override
    public void setFocused(boolean pFocused) {
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}
