package me.desht.pneumaticcraft.common.config;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Set;

public class HelmetWidgetDefaults extends JsonConfig {
    private static final Set<String> KEY_BINDS = Sets.newHashSet();

    public HelmetWidgetDefaults() {
        super(false);
    }

    @Override
    public String getFolderName() {
        return "HelmetWidgetDefaults";
    }

    @Override
    protected void writeToJson(JsonObject json) {
        json.addProperty("description", "Tracks a list of checked keybindings for the Pneumatic Helmet");
        JsonArray array = new JsonArray();
        for (String s : KEY_BINDS) {
            array.add(s);
        }
        json.add("keybindings", array);
    }

    @Override
    protected void readFromJson(JsonObject json) {
        JsonArray array = json.get("keybindings").getAsJsonArray();
        KEY_BINDS.clear();
        for (JsonElement element : array) {
            KEY_BINDS.add(element.getAsString());
        }
    }

    public static void setKey(String keyBindName, boolean checked) {
        if (checked) {
            KEY_BINDS.add(keyBindName);
        } else {
            KEY_BINDS.remove(keyBindName);
        }
    }

    public static boolean getKey(String keyBindingName) {
        return KEY_BINDS.contains(keyBindingName);
    }
}
