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

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.CreativeCompressorBlockEntity;
import me.desht.pneumaticcraft.common.inventory.CreativeCompressorMenu;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CreativeCompressorScreen extends AbstractPneumaticCraftContainerScreen<CreativeCompressorMenu,CreativeCompressorBlockEntity> {

    public CreativeCompressorScreen(CreativeCompressorMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        super.init();
        int y = height / 2 - 5;
        int x = width / 2;
        addRenderableWidget(new WidgetButtonExtended(x - 90, y, 30, 20, "-1").withTag("-1"));
        addRenderableWidget(new WidgetButtonExtended(x - 58, y, 30, 20, "-0.1").withTag("-0.1"));
        addRenderableWidget(new WidgetButtonExtended(x + 28, y, 30, 20, "+0.1").withTag("+0.1"));
        addRenderableWidget(new WidgetButtonExtended(x + 60, y, 30, 20, "+1").withTag("+1"));
    }

    @Override
    protected PointXY getGaugeLocation() {
        return null;
    }

    @Override
    protected boolean shouldAddPressureTab() {
        return false;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }
    
    @Override
    protected boolean shouldAddInfoTab(){
        return false;
    }
    
    @Override
    protected boolean shouldAddUpgradeTab(){
        return false;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);
        graphics.drawCenteredString(font, PneumaticCraftUtils.roundNumberTo(te.getPressure(), 1) + " bar", width / 2 - leftPos, height / 2 - topPos, 0xFFFFFF);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int i, int j){
        renderBackground(graphics);
        super.renderBg(graphics, partialTicks, i, j);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return null;  // no texture!
    }

    @Override
    protected boolean shouldDrawBackground() {
        return false;
    }

    @Override
    protected int getTitleColor() {
        return 0xff00ff;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }
}
