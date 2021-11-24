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
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WidgetTextField extends TextFieldWidget implements ITooltipProvider {

    private final List<ITextComponent> tooltip = new ArrayList<>();
    private boolean passwordBox;

    public WidgetTextField(FontRenderer fontRenderer, int x, int y, int width, int height) {
        super(fontRenderer, x, y, width, height, StringTextComponent.EMPTY);
    }

    public WidgetTextField setAsPasswordBox() {
        passwordBox = true;
        return this;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        String oldText = getValue();
        int oldCursorPos = getCursorPosition();
        if (passwordBox) {
            setValue(StringUtils.repeat('*', oldText.length()));
            moveCursorTo(oldCursorPos);
        }
        super.renderButton(matrixStack, mouseX, mouseY, partialTick);
        if (passwordBox) {
            setValue(oldText);
            moveCursorTo(oldCursorPos);
        }
    }

    public void setTooltip(ITextComponent... tooltip) {
        this.tooltip.clear();
        Collections.addAll(this.tooltip, tooltip);
    }

    public void setTooltip(List<ITextComponent> tooltip) {
        this.tooltip.clear();
        this.tooltip.addAll(tooltip);
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shift) {
        if (!isFocused()) {
            // hide tooltip when actually typing; it just gets in the way
            curTip.addAll(tooltip);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (canConsumeInput() && button == 1) {
            setValue("");
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
