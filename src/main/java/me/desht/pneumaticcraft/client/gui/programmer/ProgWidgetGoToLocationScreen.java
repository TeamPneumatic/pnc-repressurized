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
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetGoToLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetGoToLocationScreen<T extends ProgWidgetGoToLocation> extends ProgWidgetAreaShowScreen<T> {

    public ProgWidgetGoToLocationScreen(T progWidget, ProgrammerScreen guiProgrammer) {
        super(progWidget, guiProgrammer);

        ySize = 75;
    }

    @Override
    public void init() {
        super.init();

        WidgetRadioButton.Builder.create()
                .addRadioButton(new WidgetRadioButton(guiLeft + 8, guiTop + 20, 0xFF404040,
                                xlate("pneumaticcraft.gui.progWidget.goto.doneWhenArrived"),
                                b -> progWidget.setDoneWhenDeparting(false))
                                .setTooltipKey("pneumaticcraft.gui.progWidget.goto.doneWhenArrived.tooltip"),
                        !progWidget.doneWhenDeparting())
                .addRadioButton(new WidgetRadioButton(guiLeft + 8, guiTop + 34, 0xFF404040,
                                xlate("pneumaticcraft.gui.progWidget.goto.doneWhenDeparting"),
                                b -> progWidget.setDoneWhenDeparting(true))
                                .setTooltipKey("pneumaticcraft.gui.progWidget.goto.doneWhenDeparting.tooltip"),
                        progWidget.doneWhenDeparting())
                .build(this::addRenderableWidget);
    }
}
