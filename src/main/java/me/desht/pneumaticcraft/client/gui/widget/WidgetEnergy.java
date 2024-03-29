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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.vehicle.Minecart;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Optional;

public class WidgetEnergy extends AbstractWidget {
    private static final int DEFAULT_SCALE = 42;

    private final IEnergyStorage storage;

    public WidgetEnergy(int x, int y, IEnergyStorage storage) {
        super(x, y, 16, DEFAULT_SCALE, Component.empty());
        this.storage = storage;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick){
        int amount = getScaled();

        int x = getX(), y = getY();
        graphics.blit(Textures.WIDGET_ENERGY, x + 1, y, 1, 0, width - 2, height, 32, 64);
        graphics.blit(Textures.WIDGET_ENERGY, x + 1, y + DEFAULT_SCALE - amount, 17, DEFAULT_SCALE - amount, width - 2, amount, 32, 64);

        if (isHovered) {
            graphics.renderTooltip(Minecraft.getInstance().font,
                    Component.literal(storage.getEnergyStored() + " / " + storage.getMaxEnergyStored() + " FE"),
                    mouseX, mouseY);
        }
    }

    private int getScaled(){
        if (storage.getMaxEnergyStored() <= 0) {
            return height;
        }
        return storage.getEnergyStored() * height / storage.getMaxEnergyStored();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }
}
