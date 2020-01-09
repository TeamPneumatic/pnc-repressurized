package me.desht.pneumaticcraft.common.item;

public class ItemProgrammingPuzzle extends ItemPneumatic {

    public ItemProgrammingPuzzle() {
        super("programming_puzzle");
    }

//    public static IProgWidget getWidgetForClass(Class<? extends IProgWidget> clazz) {
//        for (IProgWidget widget : WidgetRegistrator.registeredWidgets) {
//            if (widget.getClass() == clazz) return widget;
//        }
//        throw new IllegalArgumentException("Widget " + clazz.getCanonicalName() + " isn't registered!");
//    }
//
//    public static IProgWidget getWidgetForName(ResourceLocation registryName) {
//        ProgWidgetType w = ModRegistries.PROG_WIDGETS.getValue(registryName);
//        Validate.notNull(w, "Progwidget " + registryName + " isn't registered!");
//        return w.create();
//    }
}
