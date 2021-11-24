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
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collections;
import java.util.List;

public class WidgetTooltipArea extends Widget implements ITooltipProvider {
    public final ITextComponent[] tooltip;

    public WidgetTooltipArea(int x, int y, int width, int height, ITextComponent... tooltip) {
        super(x, y, width, height, StringTextComponent.EMPTY);
        this.tooltip = tooltip;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_) {
        // nothing
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        // not clickable: any mouse clicks should be passed through to any other widget in this positions
        return false;
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shiftPressed) {
        Collections.addAll(curTip, tooltip);
    }

    @Override
    public boolean changeFocus(boolean focus) {
        return false;  // not focusable
    }
}
