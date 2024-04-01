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
import me.desht.pneumaticcraft.common.drone.progwidgets.IBlockOrdered;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetLiquidImport;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetLiquidImportScreen extends ProgWidgetImportExportScreen<ProgWidgetLiquidImport> {
    private WidgetComboBox orderSelector;
    private WidgetCheckBox voidExcess;

    public ProgWidgetLiquidImportScreen(ProgWidgetLiquidImport progWidget, ProgrammerScreen guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        WidgetLabel orderLabel = new WidgetLabel(guiLeft + 8, guiTop + 150, xlate("pneumaticcraft.gui.progWidget.digAndPlace.order"));
        addRenderableWidget(orderLabel);

        orderSelector = new WidgetComboBox(font,guiLeft + 8 + orderLabel.getWidth() + 5, guiTop + 148, 80, font.lineHeight + 3)
                .initFromEnum(progWidget.getOrder());
        addRenderableWidget(orderSelector);

        addRenderableWidget(voidExcess = new WidgetCheckBox(guiLeft + 8, guiTop + 165, 0x404040, xlate("pneumaticcraft.gui.progWidget.liquidImport.voidExcess"))
                .setTooltipKey("pneumaticcraft.gui.progWidget.liquidImport.voidExcess.tooltip")
                .setChecked(progWidget.shouldVoidExcess()));
    }

    @Override
    public void removed() {
        if (orderSelector.getSelectedElementIndex() >= 0) {
            progWidget.setOrder(IBlockOrdered.Ordering.values()[orderSelector.getSelectedElementIndex()]);
            progWidget.setVoidExcess(voidExcess.checked);
        }

        super.removed();
    }
}
