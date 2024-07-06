package me.desht.pneumaticcraft.common.util.legacyconv;

import com.google.gson.JsonObject;

public class ActionWidgetLegacyConv {
    public static int determineVersion(JsonObject json) {
        return 1;
    }

    public static void convertLegacy(JsonObject json, int oldVersion) {

    }
}
