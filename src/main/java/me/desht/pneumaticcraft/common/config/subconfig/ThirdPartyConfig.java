/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.config.subconfig;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;
import java.util.Set;

public class ThirdPartyConfig extends AuxConfigJson {
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

    @Override
    public Sidedness getSidedness() {
        return Sidedness.BOTH;
    }

    public static void setupDefaults(Set<String> modids) {
        for (String modid : modids) {
            MODS.putIfAbsent(modid, true);
        }
        INSTANCE.tryWriteToFile();
    }

    public static boolean isEnabled(String modId) {
        return MODS.getOrDefault(modId, false);
    }
}
