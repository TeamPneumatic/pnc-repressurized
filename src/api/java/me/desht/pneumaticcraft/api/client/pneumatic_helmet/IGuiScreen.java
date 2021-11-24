/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;

import java.util.List;

/**
 * An interface wrapper for actual Screen objects.
 */
public interface IGuiScreen {
    /**
     * Add a new widget to the GUI
     * @param w the widget to add
     */
    <T extends Widget> T addWidget(T w);

    /**
     * Get a list of all widgets in this GUI.  Don't use this to add subwidgets; use {@link #addWidget(Widget)} instead.
     *
     * @return a list of widgets
     */
    List<Widget> getWidgetList();

    /**
     * Convenience method to get the GUI's font renderer.
     *
     * @return a font renderer
     */
    FontRenderer getFontRenderer();

    /**
     * Set the focus on a particular subwidget (generally a text field)
     */
    void setFocusedWidget(Widget w);

    /**
     * Get the actual Screen object represented by this wrapper.
     *
     * @return the screen
     */
    default Screen getScreen() {
        return (Screen) this;
    }
}
