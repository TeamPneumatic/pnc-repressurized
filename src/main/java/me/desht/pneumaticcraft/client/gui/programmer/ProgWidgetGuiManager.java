package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;

import java.util.HashMap;
import java.util.Map;

public class ProgWidgetGuiManager {
    private static final Map<Class<? extends IProgWidget>, ProgWidgetGuiFactory> widgetToGuiMap = new HashMap<>();

    public static <T extends IProgWidget> void registerProgWidgetGui(Class<T> widgetClass, ProgWidgetGuiFactory<T> factory) {
        widgetToGuiMap.put(widgetClass, factory);
    }

    public static <T extends IProgWidget> boolean hasGui(T widget) {
        return widgetToGuiMap.containsKey(widget.getClass());
    }

    public static <T extends IProgWidget> GuiProgWidgetOptionBase getGui(T widget, GuiProgrammer programmer) {
        @SuppressWarnings("unchecked") ProgWidgetGuiFactory<T> factory = widgetToGuiMap.get(widget.getClass());
        return factory == null ? null : factory.createGui(widget, programmer);
    }

    public interface ProgWidgetGuiFactory<T extends IProgWidget> {
        GuiProgWidgetOptionBase createGui(T progWidget, GuiProgrammer programmer);
    }
}
