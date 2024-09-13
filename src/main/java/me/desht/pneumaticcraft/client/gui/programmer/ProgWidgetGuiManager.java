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
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.client.gui.ProgrammerScreen;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ProgWidgetGuiManager {
    private static final Map<ProgWidgetType<? extends IProgWidget>, ProgWidgetGuiFactory<? extends IProgWidget>> widgetToGuiMap = new HashMap<>();

    public static <P extends IProgWidget> void registerProgWidgetGui(Supplier<ProgWidgetType<P>> typeSupplier, ProgWidgetGuiFactory<P> factory) {
        widgetToGuiMap.put(typeSupplier.get(), factory);
    }

    public static <P extends IProgWidget> boolean hasGui(P widget) {
        return widgetToGuiMap.containsKey(widget.getType());
    }

    public static <P extends IProgWidget> AbstractProgWidgetScreen<P> getGui(P widget, ProgrammerScreen programmer) {
        @SuppressWarnings("unchecked") ProgWidgetGuiFactory<P> factory = (ProgWidgetGuiFactory<P>) widgetToGuiMap.get(widget.getType());
        return factory == null ? null : factory.createGui(widget, programmer);
    }

    @FunctionalInterface
    public interface ProgWidgetGuiFactory<P extends IProgWidget> {
        AbstractProgWidgetScreen<P> createGui(P progWidget, ProgrammerScreen programmer);
    }
}
