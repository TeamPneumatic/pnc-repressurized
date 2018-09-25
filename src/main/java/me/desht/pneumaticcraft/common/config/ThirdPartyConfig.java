package me.desht.pneumaticcraft.common.config;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class ThirdPartyConfig extends JsonConfig {
    private static final Map<String, Boolean> MODS = Maps.newHashMap();

    public static final ThirdPartyConfig INSTANCE = new ThirdPartyConfig();

    private ThirdPartyConfig() {
        super(true);
    }

    @Override
    public String getConfigFilename() {
        return "thirdparty";
    }

    @Override
    protected void writeToJson(JsonObject json) {
        json.addProperty("description", "Enable/disable integration of specific third-party mods");
        JsonObject obj = new JsonObject();
        for (Map.Entry<String,Boolean> entry : MODS.entrySet()) {
            obj.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
        }
        json.add("enabled_mods", obj);
    }

    @Override
    protected void readFromJson(JsonObject json) {
        JsonObject obj = json.get("enabled_mods").getAsJsonObject();
        MODS.clear();
        for (Map.Entry<String,JsonElement> entry : obj.entrySet()) {
            MODS.put(entry.getKey(), entry.getValue().getAsBoolean());
        }
    }

    public static void setupDefaults(Set<String> modids) {
        for (String modid : modids) {
            MODS.putIfAbsent(modid, true);
        }
        try {
            INSTANCE.writeToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isEnabled(String modId) {
        return MODS.getOrDefault(modId, false);
    }
}
