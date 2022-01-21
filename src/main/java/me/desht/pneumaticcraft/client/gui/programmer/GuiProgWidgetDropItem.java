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

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetDropItem;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetDropItem extends GuiProgWidgetImportExport<ProgWidgetDropItem> {

    public GuiProgWidgetDropItem(ProgWidgetDropItem progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        addLabel(xlate("pneumaticcraft.gui.progWidget.drop.dropMethod"), guiLeft + 8, guiTop + 70);

        WidgetRadioButton.Builder.create()
                .addRadioButton(new WidgetRadioButton(guiLeft + 8, guiTop + 82, 0xFF404040,
                                xlate("pneumaticcraft.gui.progWidget.drop.dropMethod.random"),
                                b -> progWidget.setDropStraight(false)),
                        !progWidget.dropStraight())
                .addRadioButton(new WidgetRadioButton(guiLeft + 8, guiTop + 94, 0xFF404040,
                                xlate("pneumaticcraft.gui.progWidget.drop.dropMethod.straight"),
                                b -> progWidget.setDropStraight(true)),
                        progWidget.dropStraight())
                .build(this::addRenderableWidget);

        addRenderableWidget(new WidgetCheckBox(guiLeft + 8, guiTop + 115, 0xFF404040,
                xlate("pneumaticcraft.gui.progWidget.drop.hasPickupDelay"), b -> progWidget.setPickupDelay(b.checked))
                .setChecked(progWidget.hasPickupDelay()));
    }

    @Override
    protected boolean showSides() {
        return false;
    }
}
