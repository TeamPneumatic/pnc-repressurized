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
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetLiquidExport;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetLiquidExportScreen extends ProgWidgetImportExportScreen<ProgWidgetLiquidExport> {

    public ProgWidgetLiquidExportScreen(ProgWidgetLiquidExport widget, ProgrammerScreen guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetCheckBox checkbox = new WidgetCheckBox(guiLeft + 8, guiTop + 150, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.liquidExport.placeFluidInWorld"), b -> progWidget.setPlaceFluidBlocks(b.checked));
        checkbox.setChecked(progWidget.isPlacingFluidBlocks());
        addRenderableWidget(checkbox);
    }
}
