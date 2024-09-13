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

package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetEmitRedstone;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetEmitRedstoneScreen extends AbstractProgWidgetScreen<ProgWidgetEmitRedstone> {

    public ProgWidgetEmitRedstoneScreen(ProgWidgetEmitRedstone widget, ProgrammerScreen guiProgrammer) {
        super(widget, guiProgrammer);

        ySize = 110;
    }

    @Override
    public void init() {
        super.init();

        for (Direction dir : DirectionUtil.VALUES) {
            Component sideName = ClientUtils.translateDirectionComponent(dir);
            WidgetCheckBox checkBox = new WidgetCheckBox(guiLeft + 8, guiTop + 30 + dir.get3DDataValue() * 12, 0xFF404040, sideName,
                    b -> progWidget.setSideSelected(dir, b.checked));
            checkBox.checked = progWidget.isSideSelected(dir);
            addRenderableWidget(checkBox);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        graphics.drawString(font, xlate("pneumaticcraft.gui.progWidget.general.affectingSides"), guiLeft + 8, guiTop + 20, 0xFF604040, false);
    }
}
