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

import java.util.HashMap;
import java.util.Map;

public class ProgWidgetGuiManager {
    private static final Map<Class<? extends IProgWidget>, ProgWidgetGuiFactory<? extends IProgWidget>> widgetToGuiMap = new HashMap<>();

    public static <T extends IProgWidget> void registerProgWidgetGui(Class<T> widgetClass, ProgWidgetGuiFactory<T> factory) {
        widgetToGuiMap.put(widgetClass, factory);
    }

    public static <T extends IProgWidget> boolean hasGui(T widget) {
        return widgetToGuiMap.containsKey(widget.getClass());
    }

    public static <T extends IProgWidget> AbstractProgWidgetScreen<T> getGui(T widget, ProgrammerScreen programmer) {
        @SuppressWarnings("unchecked") ProgWidgetGuiFactory<T> factory = (ProgWidgetGuiFactory<T>) widgetToGuiMap.get(widget.getClass());
        return factory == null ? null : factory.createGui(widget, programmer);
    }

    @FunctionalInterface
    public interface ProgWidgetGuiFactory<T extends IProgWidget> {
        AbstractProgWidgetScreen<T> createGui(T progWidget, ProgrammerScreen programmer);
    }
}
