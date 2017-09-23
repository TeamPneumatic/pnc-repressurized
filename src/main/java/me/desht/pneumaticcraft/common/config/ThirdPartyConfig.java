package me.desht.pneumaticcraft.common.config;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;

import static me.desht.pneumaticcraft.lib.ModIds.*;

public class ThirdPartyConfig extends JsonConfig {
    private static final Map<String, Boolean> MODS = Maps.newHashMap();
    static {
        MODS.put(BUILDCRAFT, true);
        MODS.put(COMPUTERCRAFT, true);
        MODS.put(IGWMOD, true);
        MODS.put(TE,  true);
        MODS.put(AE2, true);
        MODS.put(FORESTRY, true);
        MODS.put(OPEN_BLOCKS, true);
        MODS.put(COFH_CORE, true);
        MODS.put(OPEN_COMPUTERS, true);
        MODS.put(EIO, true);
        MODS.put(MCMP,  true);
    }

    public ThirdPartyConfig() {
        super(true);
    }

    @Override
    public String getFolderName() {
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

    public static boolean isEnabled(String modId) {
        return MODS.getOrDefault(modId, false);
    }
}
