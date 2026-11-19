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

import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

public class WidgetEnergy extends AbstractWidget {
    private static final int DEFAULT_SCALE = 42;

    private final EnergyHandler storage;

    public WidgetEnergy(int x, int y, EnergyHandler storage) {
        super(x, y, 16, DEFAULT_SCALE, Component.empty());
        this.storage = storage;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick){
        int amount = getScaled();

        int x = getX(), y = getY();
        graphics.blit(RenderPipelines.GUI_TEXTURED, Textures.WIDGET_ENERGY, x + 1, y, 1, 0, width - 2, height, 32, 64);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Textures.WIDGET_ENERGY, x + 1, y + DEFAULT_SCALE - amount, 17, DEFAULT_SCALE - amount, width - 2, amount, 32, 64);

        if (isHovered) {
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font,
                    Component.literal(storage.getAmountAsInt() + " / " + storage.getCapacityAsInt() + " FE"),
                    mouseX, mouseY);
        }
    }

    private int getScaled(){
        if (storage.getCapacityAsInt() <= 0) {
            return height;
        }
        return storage.getAmountAsInt() * height / storage.getCapacityAsInt();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}
