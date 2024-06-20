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

package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.ProgressingLine;
import me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL11;

import static me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation.GRID_WIDTH;

public class HackSimulationRenderer {
    private final int baseX;
    private final int baseY;
    private final int nodeSpacing;

    public HackSimulationRenderer(int baseX, int baseY, int nodeSpacing) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.nodeSpacing = nodeSpacing;
    }

    public void render(GuiGraphics graphics, HackSimulation hackSimulation, int color) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        for (HackSimulation.ConnectionEntry entry : hackSimulation.allConnections) {
            GuiUtils.renderProgressingLine2d(graphics, makeProgressingLine(entry), color, 3F);
        }
        RenderSystem.disableBlend();
    }

    private ProgressingLine makeProgressingLine(HackSimulation.ConnectionEntry entry) {
        int xOff = 0;
        int yOff = 0;
        int delta = entry.from() - entry.to();
        if (delta == GRID_WIDTH) {
            xOff = 1;
        } else if (delta == -GRID_WIDTH) {
            xOff = -1;
        } else {
            yOff = delta < 0 ? -1 : 1;
        }
        int startX = baseX + xOff + entry.from() % GRID_WIDTH * nodeSpacing;
        int startY = baseY + yOff + entry.from() / GRID_WIDTH * nodeSpacing;
        int endX = baseX + xOff + entry.to() % GRID_WIDTH * nodeSpacing;
        int endY = baseY + yOff + entry.to() / GRID_WIDTH * nodeSpacing;
        return new ProgressingLine(startX, startY, endX, endY).setProgress(entry.progress());
    }
}
