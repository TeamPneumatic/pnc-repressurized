package me.desht.pneumaticcraft.api.drone;

import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Supplier;

/**
 * Represents the type of a programming widget. You do not need to use this directly.
 */
public class ProgWidgetType<P extends IProgWidgetBase> extends ForgeRegistryEntry<ProgWidgetType<?>> {
    private final Supplier<? extends P> factory;

    public ProgWidgetType(Supplier<P> factory) {
        this.factory = factory;
    }

    public P create() {
        return factory.get();
    }

    public String getTranslationKey() {
        return "programmingPuzzle." + getRegistryName().toString().replace(':', '.') + ".name";
    }
}
