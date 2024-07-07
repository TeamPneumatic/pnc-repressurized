package me.desht.pneumaticcraft.common.util.legacyconv;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.Util;
import net.minecraft.nbt.Tag;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

public class ConversionUtil {
    static OptionalInt getInt(JsonObject json, String fieldName) {
        if (json.has(fieldName)) {
            JsonObject sub = json.get(fieldName).getAsJsonObject();
            if (sub.has("type") && sub.get("type").getAsInt() == Tag.TAG_INT) {
                return OptionalInt.of(sub.get("value").getAsInt());
            }
        }
        return OptionalInt.empty();
    }

    static Optional<String> getString(JsonObject json, String fieldName) {
        if (json.has(fieldName)) {
            JsonObject sub = json.get(fieldName).getAsJsonObject();
            if (sub.has("type") && sub.get("type").getAsInt() == Tag.TAG_STRING) {
                return Optional.of(sub.get("value").getAsString());
            }
        }
        return Optional.empty();
    }

    static Optional<Boolean> getBool(JsonObject json, String fieldName) {
        if (json.has(fieldName)) {
            JsonObject sub = json.get(fieldName).getAsJsonObject();
            if (sub.has("type") && sub.get("type").getAsInt() == Tag.TAG_BYTE) {
                return Optional.of(sub.get("value").getAsBoolean());
            }
        }
        return Optional.empty();
    }

    static void convXYZ(JsonObject from, JsonObject to, String prefix, String newKey) {
        int sx = getInt(from, prefix + "X").orElse(0);
        int sy = getInt(from, prefix + "Y").orElse(0);
        int sz = getInt(from, prefix + "Z").orElse(0);
        to.add(newKey, makeArray(sx, sy, sz));
    }

    static String prefixVar(String varName) {
        return varName.isEmpty() || varName.startsWith("%") || varName.startsWith("#") ? varName : "#" + varName;
    }

    static JsonArray makeArray(int... vals) {
        return Util.make(new JsonArray(), a -> Arrays.stream(vals).forEach(a::add));
    }
}
