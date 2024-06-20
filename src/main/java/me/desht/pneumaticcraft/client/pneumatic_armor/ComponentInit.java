package me.desht.pneumaticcraft.client.pneumatic_armor;

import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;

public enum ComponentInit implements ITranslatableEnum {
    ALL("all"),
    ENABLED_ONLY("enabled_only"),
    NONE("none");

    private final String name;

    ComponentInit(String name) {
        this.name = name;
    }

    @Override
    public String getTranslationKey() {
        return "pneumaticcraft.armor.gui.component_init." + name;
    }
}
