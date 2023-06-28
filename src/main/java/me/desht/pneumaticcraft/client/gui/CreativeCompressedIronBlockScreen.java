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
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.CreativeCompressedIronBlockBlockEntity;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.CreativeCompressedIronBlockMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class CreativeCompressedIronBlockScreen extends AbstractPneumaticCraftContainerScreen<CreativeCompressedIronBlockMenu, CreativeCompressedIronBlockBlockEntity> {
    private WidgetButtonExtended down2, down1, up1, up2;

    public CreativeCompressedIronBlockScreen(CreativeCompressedIronBlockMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        int y = height / 2 - 5;
        int x = width / 2;
        down2 = addRenderableWidget(new WidgetButtonExtended(x - 90, y, 30, 20, "-10").withTag("-10"));
        down1 = addRenderableWidget(new WidgetButtonExtended(x - 58, y, 30, 20, "-1").withTag("-1"));
        up1 = addRenderableWidget(new WidgetButtonExtended(x + 28, y, 30, 20, "+1").withTag("+1"));
        up2 = addRenderableWidget(new WidgetButtonExtended(x + 60, y, 30, 20, "+10").withTag("+10"));
    }

    @Override
    public void containerTick() {
        super.containerTick();

        setText(down2, "-100", "-10");
        setText(down1, "-10", "-1");
        setText(up1, "+10", "+1");
        setText(up2, "+100", "+10");
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return null;
    }

    @Override
    protected boolean shouldDrawBackground() {
        return false;
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
    protected int getTitleColor() {
        return 0xff00ff;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);
        Component txt = HeatUtil.formatHeatString(te.targetTemperature);
        graphics.drawCenteredString(font, txt, width / 2 - leftPos, height / 2 - topPos - 20, 0xFFFFFF);
        graphics.drawCenteredString(font, xlate("pneumaticcraft.gui.misc.holdShiftFastAdjust"), width / 2 - leftPos, height / 2 - topPos + 20, 0x808080);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int i, int j){
        renderBackground(graphics);
        super.renderBg(graphics, partialTicks, i, j);
    }

    private void setText(WidgetButtonExtended b, String txt1, String txt2) {
        b.setMessage(Component.literal(ClientUtils.hasShiftDown() ? txt1 : txt2));
    }
}
