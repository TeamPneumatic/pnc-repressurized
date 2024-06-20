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

import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetAreaShowScreen<P extends IProgWidget> extends AbstractProgWidgetScreen<P> {

    public ProgWidgetAreaShowScreen(P progWidget, ProgrammerScreen guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        if (displayShowAreaButtons()) {
            addRenderableWidget(new WidgetCheckBox(guiLeft + 8, guiTop + ySize - (font.lineHeight + 8), 0x404040,
                    xlate("pneumaticcraft.gui.programmer.button.showArea"), this::previewArea)
                    .setChecked(AreaRenderManager.getInstance().isShowing(guiProgrammer.te)));
        }
    }

    protected void previewArea(WidgetCheckBox button) {
        guiProgrammer.te.previewArea(button.checked ? progWidget : null);
    }

    public boolean displayShowAreaButtons() {
        return true;
    }
}
