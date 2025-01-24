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
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetDig;
import net.minecraft.core.Direction;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetDigScreen extends ProgWidgetDigAndPlaceScreen<ProgWidgetDig> {

    public ProgWidgetDigScreen(ProgWidgetDig progWidget, ProgrammerScreen guiProgrammer){
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetLabel sideLabel;
        addRenderableWidget(sideLabel = new WidgetLabel(guiLeft + 8, guiTop + 45, xlate("pneumaticcraft.gui.progWidget.blockRightClick.clickSide"))
                .setTooltipKey("pneumaticcraft.gui.progWidget.dig.digSide.tooltip"));

        addRenderableWidget(
                new WidgetComboBox(font, guiLeft + 8 + sideLabel.getWidth() + 5, guiTop + 43, 50, 12,
                        comboBox -> progWidget.setDigSide(Direction.from3DDataValue(comboBox.getSelectedElementIndex()))
                ).initFromEnum(progWidget.getDigSide(), ClientUtils::translateDirection)
        );

        addRenderableWidget(
                new WidgetCheckBox(guiLeft + 8, guiTop + 85, 0xFF404040,
                        xlate("pneumaticcraft.gui.progWidget.dig.requiresDiggingTool"), b -> progWidget.setRequiresTool(b.checked))
                        .setTooltipKey("pneumaticcraft.gui.progWidget.dig.requiresDiggingTool.tooltip")
                        .setChecked(progWidget.requiresTool())
        );
    }
}
