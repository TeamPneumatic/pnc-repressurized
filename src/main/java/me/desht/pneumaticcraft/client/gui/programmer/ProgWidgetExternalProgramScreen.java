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
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetExternalProgram;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetExternalProgramScreen extends ProgWidgetAreaShowScreen<ProgWidgetExternalProgram> {

    private WidgetCheckBox shareVariables;

    public ProgWidgetExternalProgramScreen(ProgWidgetExternalProgram widget, ProgrammerScreen guiProgrammer) {
        super(widget, guiProgrammer);

        ySize = 65;
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(shareVariables = new WidgetCheckBox(guiLeft + 10, guiTop + 22, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.externalProgram.shareVariables"))
                .setTooltipKey("pneumaticcraft.gui.progWidget.externalProgram.shareVariables.tooltip")
                .setChecked(progWidget.shareVariables)
        );
    }

    @Override
    public void removed() {
        progWidget.shareVariables = shareVariables.checked;

        super.removed();
    }
}
