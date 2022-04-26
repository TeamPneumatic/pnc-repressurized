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

package me.desht.pneumaticcraft.common.util;

import com.google.gson.*;
import net.minecraft.nbt.*;

import java.util.Set;

public class NBTToJsonConverter {
    private final CompoundTag tag;

    public NBTToJsonConverter(CompoundTag tag) {
        this.tag = tag;
    }

    public String convert(boolean pretty) {
        JsonObject json = getObject(tag);
        String jsonString = json.toString();

        JsonParser parser = new JsonParser();
        GsonBuilder builder = new GsonBuilder();
        if (pretty) builder.setPrettyPrinting();
        Gson gson = builder.create();

        JsonElement el = parser.parse(jsonString);
        return gson.toJson(el); // done
    }

    public static JsonObject getObject(CompoundTag tag) {
        Set<String> keys = tag.getAllKeys();
        JsonObject jsonRoot = new JsonObject();
        for (String key : keys) {
            JsonObject keyObject = new JsonObject();
            jsonRoot.add(key, keyObject);
            Tag nbt = tag.get(key);

            keyObject.addProperty("type", nbt.getId());

            if (nbt instanceof CompoundTag) {
                keyObject.add("value", getObject((CompoundTag) nbt));
            } else if (nbt instanceof NumericTag) {
                keyObject.addProperty("value", ((NumericTag) nbt).getAsDouble());
            } else if (nbt instanceof StringTag) {
                keyObject.addProperty("value", nbt.getAsString());
            } else if (nbt instanceof ListTag tagList) {
                JsonArray array = new JsonArray();
                for (int i = 0; i < tagList.size(); i++) {
                    if (tagList.getElementType() == Tag.TAG_COMPOUND) {
                        array.add(getObject(tagList.getCompound(i)));
                    } else if (tagList.getElementType() == Tag.TAG_STRING) {
                        array.add(new JsonPrimitive(tagList.getString(i)));
                    }
                }
                keyObject.add("value", array);
            } else if (nbt instanceof IntArrayTag intArray) {
                JsonArray array = new JsonArray();
                for (int i : intArray.getAsIntArray()) {
                    array.add(new JsonPrimitive(i));
                }
                keyObject.add("value", array);
            } else {
                throw new IllegalArgumentException("NBT to JSON converter doesn't support the nbt tag: " + nbt.getId() + ", tag: " + nbt);
            }
        }
        return jsonRoot;
    }
}
