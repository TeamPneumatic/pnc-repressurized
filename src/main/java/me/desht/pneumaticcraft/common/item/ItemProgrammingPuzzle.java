package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.WidgetRegistrator;

public class ItemProgrammingPuzzle extends ItemPneumatic {

    public ItemProgrammingPuzzle() {
        super("programming_puzzle");
    }

    public static IProgWidget getWidgetForClass(Class<? extends IProgWidget> clazz) {
        for (IProgWidget widget : WidgetRegistrator.registeredWidgets) {
            if (widget.getClass() == clazz) return widget;
        }
        throw new IllegalArgumentException("Widget " + clazz.getCanonicalName() + " isn't registered!");
    }

    public static IProgWidget getWidgetForName(String name) {
        for (IProgWidget widget : WidgetRegistrator.registeredWidgets) {
            if (widget.getWidgetString().equals(name)) return widget;
        }
        throw new IllegalArgumentException("Widget " + name + " isn't registered!");
    }
}
