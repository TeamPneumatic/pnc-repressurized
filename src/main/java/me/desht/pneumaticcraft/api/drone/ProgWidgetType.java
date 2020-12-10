package me.desht.pneumaticcraft.api.drone;

import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Supplier;

/**
 * Represents the type of a programming widget. You do not need to use this directly.
 */
public class ProgWidgetType<P extends IProgWidgetBase> extends ForgeRegistryEntry<ProgWidgetType<?>> {
    private final Supplier<? extends P> factory;

    // this is needed because Java's generics are so bad
    // you can use it if you want to register a custom progwidget via DeferredRegister
    @SuppressWarnings("unchecked")
    public static final Class<ProgWidgetType<?>> CLASS_GENERIC = (Class<ProgWidgetType<?>>)((Class<?>)ProgWidgetType.class);

    private ProgWidgetType(Supplier<P> factory) {
        this.factory = factory;
    }

    public static <P extends IProgWidgetBase> ProgWidgetType<P> createType(Supplier<P> factory) {
        return new ProgWidgetType<>(factory);
    }

    public P create() {
        return factory.get();
    }

    public String getTranslationKey() {
        return "programmingPuzzle." + getRegistryName().toString().replace(':', '.') + ".name";
    }

    public P cast(IProgWidgetBase widget) {
        //noinspection unchecked
        return (P) widget;
    }
}
