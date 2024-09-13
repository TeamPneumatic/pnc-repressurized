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

import com.mojang.blaze3d.platform.InputConstants;
import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetText;

public class ProgWidgetStringScreen<T extends ProgWidgetText> extends AbstractProgWidgetScreen<T> {
    private WidgetTextField textfield;

    public ProgWidgetStringScreen(T widget, ProgrammerScreen guiProgrammer) {
        super(widget, guiProgrammer);
        ySize = 45;
    }

    @Override
    public void init() {
        super.init();

        textfield = new WidgetTextField(font, guiLeft + 8, guiTop + 22, 166);
        textfield.setMaxLength(1000);
        textfield.setValue(progWidget.getString());
        setInitialFocus(textfield);
        addRenderableWidget(textfield);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_RETURN) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        progWidget.setString(textfield.getValue());

        super.removed();
    }
}
