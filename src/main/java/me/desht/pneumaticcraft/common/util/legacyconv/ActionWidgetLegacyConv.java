package me.desht.pneumaticcraft.common.util.legacyconv;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.remote.SavedRemoteLayout;
import me.desht.pneumaticcraft.lib.Log;

import static me.desht.pneumaticcraft.common.util.legacyconv.ConversionUtil.*;

public class ActionWidgetLegacyConv {
    public static int determineVersion(JsonObject json) {
        if (json.has("version")) {
            // 1.20.6 and newer
            return json.get("version").getAsInt();
        } else if (json.has("main")) {
            return 1;
        } else {
            throw new JsonSyntaxException("can't determine saved remote layout version!");
        }
    }

    public static void convertLegacy(JsonObject json, int oldVersion) {
        if (oldVersion == 1) {
            convertFromV1(json);
        }
    }

    private static void convertFromV1(JsonObject json) {
        JsonArray entries = json.getAsJsonObject("main").getAsJsonArray("value");

        JsonArray newDoc = new JsonArray();

        for (JsonElement entry : entries) {
            JsonObject oldEntry = entry.getAsJsonObject();

            JsonObject newEntry = new JsonObject();
            JsonObject base = new JsonObject();
            JsonObject widget = new JsonObject();

            getString(oldEntry, "enableVariable").ifPresent(val -> base.addProperty("enable_var", prefixVar(val)));
            convXYZ(oldEntry, base, "enabling", "enable_pos");

            getInt(oldEntry, "x").ifPresent(val -> widget.addProperty("x", val));
            getInt(oldEntry, "y").ifPresent(val -> widget.addProperty("y", val + 8));
            getInt(oldEntry, "width").ifPresent(val -> widget.addProperty("width", val));
            getInt(oldEntry, "height").ifPresent(val -> widget.addProperty("height", val));
            getString(oldEntry, "text").ifPresent(val -> widget.addProperty("title", val));
            getString(oldEntry, "tooltip").ifPresent(val -> widget.addProperty("tooltip", val));

            newEntry.add("base", base);
            newEntry.add("widget", widget);

            getString(oldEntry, "id").ifPresent(val -> newEntry.addProperty("type", Names.MOD_ID + ":" + val));
            getString(oldEntry, "variableName").ifPresent(val -> newEntry.addProperty("var_name", prefixVar(val)));
            convXYZ(oldEntry, newEntry, "setting", "set_pos");
            getString(oldEntry, "dropDownElements").ifPresent(val -> newEntry.addProperty("elements", val));
            getBool(oldEntry, "sorted").ifPresent(val -> newEntry.addProperty("sorted", val));

            newDoc.add(newEntry);
        }

        json.add("widgets", newDoc);
        json.remove("main");
        json.addProperty("version", SavedRemoteLayout.JSON_VERSION);

        Log.info("Pastebin Remote import (V1 -> V2): converted {} legacy widgets", newDoc.size());
    }

}
