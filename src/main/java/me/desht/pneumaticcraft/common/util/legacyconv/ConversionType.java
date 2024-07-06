package me.desht.pneumaticcraft.common.util.legacyconv;

import com.google.gson.JsonObject;

import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

public enum ConversionType {
    PROG_WIDGET(ProgWidgetLegacyConv::determineVersion, ProgWidgetLegacyConv::convertLegacy),
    ACTION_WIDGET(ActionWidgetLegacyConv::determineVersion, ActionWidgetLegacyConv::convertLegacy);

    private final ToIntFunction<JsonObject> determineVersion;
    private final BiConsumer<JsonObject, Integer> convertLegacy;

    ConversionType(ToIntFunction<JsonObject> determineVersion, BiConsumer<JsonObject, Integer> convertLegacy) {
        this.determineVersion = determineVersion;
        this.convertLegacy = convertLegacy;
    }

    public int determineVersion(JsonObject json) {
        return determineVersion.applyAsInt(json);
    }

    public void convertLegacy(JsonObject json, int oldVersion) {
        convertLegacy.accept(json, oldVersion);
    }
}
