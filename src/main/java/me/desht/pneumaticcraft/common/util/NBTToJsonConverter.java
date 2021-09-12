package me.desht.pneumaticcraft.common.util;

import com.google.gson.*;
import net.minecraft.nbt.*;

import java.util.Set;

public class NBTToJsonConverter {
    private final CompoundNBT tag;

    public NBTToJsonConverter(CompoundNBT tag) {
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

    public static JsonObject getObject(CompoundNBT tag) {
        Set<String> keys = tag.getAllKeys();
        JsonObject jsonRoot = new JsonObject();
        for (String key : keys) {
            JsonObject keyObject = new JsonObject();
            jsonRoot.add(key, keyObject);
            INBT nbt = tag.get(key);

            keyObject.addProperty("type", nbt.getId());

            if (nbt instanceof CompoundNBT) {
                keyObject.add("value", getObject((CompoundNBT) nbt));
            } else if (nbt instanceof NumberNBT) {
                keyObject.addProperty("value", ((NumberNBT) nbt).getAsDouble());
            } else if (nbt instanceof StringNBT) {
                keyObject.addProperty("value", nbt.getAsString());
            } else if (nbt instanceof ListNBT) {
                JsonArray array = new JsonArray();
                ListNBT tagList = (ListNBT) nbt;
                for (int i = 0; i < tagList.size(); i++) {
                    array.add(getObject(tagList.getCompound(i)));
                }
                keyObject.add("value", array);
            } else if (nbt instanceof IntArrayNBT) {
                JsonArray array = new JsonArray();
                IntArrayNBT intArray = (IntArrayNBT) nbt;
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
