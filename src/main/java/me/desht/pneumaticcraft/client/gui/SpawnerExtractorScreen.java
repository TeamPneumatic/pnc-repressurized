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

import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.spawning.SpawnerExtractorBlockEntity;
import me.desht.pneumaticcraft.common.inventory.SpawnerExtractorMenu;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class SpawnerExtractorScreen extends AbstractPneumaticCraftContainerScreen<SpawnerExtractorMenu, SpawnerExtractorBlockEntity> {
    public SpawnerExtractorScreen(SpawnerExtractorMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_ELEVATOR;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        return new PointXY(xStart + (int)(imageWidth * 0.82), yStart + imageHeight / 4 + 4);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);

        int progress = Mth.clamp((int)(te.getProgress() * 100f), 0, 100);
        graphics.drawString(font, "Progress:", 65, 35, 0x404040, false);
        graphics.drawString(font, progress + "%", 80, 47, 0x404040, false);
    }

    @Override
    protected void addWarnings(List<Component> curInfo) {
        super.addWarnings(curInfo);

        if (te.getMode() == SpawnerExtractorBlockEntity.Mode.FINISHED) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.spawnerExtractor.finished"));
        }
    }
}
