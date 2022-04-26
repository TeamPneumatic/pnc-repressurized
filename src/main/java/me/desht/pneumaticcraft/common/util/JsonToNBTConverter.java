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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.*;

import java.util.Map;

public class JsonToNBTConverter {
    private final String jsonString;

    public JsonToNBTConverter(String jsonString) {
        this.jsonString = jsonString;
    }

    public CompoundTag convert() {
        JsonParser parser = new JsonParser();
        JsonElement el = parser.parse(jsonString);
        return getTag((JsonObject) el);
    }

    public static CompoundTag getTag(JsonObject object) {
        CompoundTag nbt = new CompoundTag();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonObject keyObject = entry.getValue().getAsJsonObject();
            int type = keyObject.get("type").getAsInt();
            JsonElement element = keyObject.get("value");

            switch (type) {
                case Tag.TAG_BYTE:
                    nbt.putByte(entry.getKey(), (byte) element.getAsDouble());
                    break;
                case Tag.TAG_SHORT:
                    nbt.putShort(entry.getKey(), (short) element.getAsDouble());
                    break;
                case Tag.TAG_INT:
                    nbt.putInt(entry.getKey(), (int) element.getAsDouble());
                    break;
                case Tag.TAG_LONG:
                    nbt.putLong(entry.getKey(), (long) element.getAsDouble());
                    break;
                case Tag.TAG_FLOAT:
                    nbt.putFloat(entry.getKey(), (float) element.getAsDouble());
                    break;
                case Tag.TAG_DOUBLE:
                    nbt.putDouble(entry.getKey(), element.getAsDouble());
                    break;
                //   case 7:
                //       return new NBTTagByteArray();
                //   break;
                case Tag.TAG_STRING:
                    nbt.putString(entry.getKey(), element.getAsString());
                    break;
                case Tag.TAG_LIST:
                    JsonArray array = element.getAsJsonArray();
                    ListTag tagList = new ListTag();
                    for (JsonElement e : array) {
                        if (e.isJsonObject()) {
                            tagList.add(tagList.size(), getTag(e.getAsJsonObject()));
                        } else {
                            tagList.add(tagList.size(), StringTag.valueOf(e.getAsString()));
                        }
                    }
                    nbt.put(entry.getKey(), tagList);
                    break;
                case Tag.TAG_COMPOUND:
                    nbt.put(entry.getKey(), getTag(element.getAsJsonObject()));
                    break;
                case Tag.TAG_INT_ARRAY:
                    array = element.getAsJsonArray();
                    int[] intArray = new int[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        intArray[i] = array.get(i).getAsInt();
                    }
                    nbt.put(entry.getKey(), new IntArrayTag(intArray));
                    break;
                default:
                    throw new IllegalArgumentException("NBT type no " + type + " is not supported by the Json to NBT converter!");
            }
        }
        return nbt;
    }
}
