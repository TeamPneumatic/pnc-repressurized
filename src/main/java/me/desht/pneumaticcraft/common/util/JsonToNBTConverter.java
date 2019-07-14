package me.desht.pneumaticcraft.common.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public class JsonToNBTConverter {
    private final String jsonString;

    public JsonToNBTConverter(String jsonString) {
        this.jsonString = jsonString;
    }

    public CompoundNBT convert() {
        JsonParser parser = new JsonParser();
        JsonElement el = parser.parse(jsonString);
        return getTag((JsonObject) el);
    }

    public static CompoundNBT getTag(JsonObject object) {
        CompoundNBT nbt = new CompoundNBT();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonObject keyObject = entry.getValue().getAsJsonObject();
            int type = keyObject.get("type").getAsInt();
            JsonElement element = keyObject.get("value");

            switch (type) {
                case Constants.NBT.TAG_BYTE:
                    nbt.putByte(entry.getKey(), (byte) element.getAsDouble());
                    break;
                case Constants.NBT.TAG_SHORT:
                    nbt.putShort(entry.getKey(), (short) element.getAsDouble());
                    break;
                case Constants.NBT.TAG_INT:
                    nbt.putInt(entry.getKey(), (int) element.getAsDouble());
                    break;
                case Constants.NBT.TAG_LONG:
                    nbt.putLong(entry.getKey(), (long) element.getAsDouble());
                    break;
                case Constants.NBT.TAG_FLOAT:
                    nbt.putFloat(entry.getKey(), (float) element.getAsDouble());
                    break;
                case Constants.NBT.TAG_DOUBLE:
                    nbt.putDouble(entry.getKey(), element.getAsDouble());
                    break;
                //   case 7:
                //       return new NBTTagByteArray();
                //   break;
                case Constants.NBT.TAG_STRING:
                    nbt.putString(entry.getKey(), element.getAsString());
                    break;
                case Constants.NBT.TAG_LIST:
                    JsonArray array = element.getAsJsonArray();
                    ListNBT tagList = new ListNBT();
                    for (JsonElement e : array) {
                        tagList.add(tagList.size(), getTag(e.getAsJsonObject()));
                    }
                    nbt.put(entry.getKey(), tagList);
                    break;
                case Constants.NBT.TAG_COMPOUND:
                    nbt.put(entry.getKey(), getTag(element.getAsJsonObject()));
                    break;
                case Constants.NBT.TAG_INT_ARRAY:
                    array = element.getAsJsonArray();
                    int[] intArray = new int[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        intArray[i] = array.get(i).getAsInt();
                    }
                    nbt.put(entry.getKey(), new IntArrayNBT(intArray));
                    break;
                default:
                    throw new IllegalArgumentException("NBT type no " + type + " is not supported by the Json to NBT converter!");
            }
        }
        return nbt;
    }
}
