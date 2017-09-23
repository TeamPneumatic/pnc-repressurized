package me.desht.pneumaticcraft.common.util;

import com.google.gson.*;
import net.minecraft.nbt.*;

import java.util.Set;

public class NBTToJsonConverter {
    private final NBTTagCompound tag;

    public NBTToJsonConverter(NBTTagCompound tag) {
        this.tag = tag;
    }

    public String convert() {
        JsonObject json = getObject(tag);
        String jsonString = json.toString();

        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonElement el = parser.parse(jsonString);
        return gson.toJson(el); // done
    }

    private JsonObject getObject(NBTTagCompound tag) {
        Set<String> keys = tag.getKeySet();
        JsonObject jsonRoot = new JsonObject();
        for (String key : keys) {
            JsonObject keyObject = new JsonObject();
            jsonRoot.add(key, keyObject);
            NBTBase nbt = tag.getTag(key);

            keyObject.addProperty("type", nbt.getId());

            if (nbt instanceof NBTTagCompound) {
                keyObject.add("value", getObject((NBTTagCompound) nbt));
            } else if (nbt instanceof NBTPrimitive) {
                keyObject.addProperty("value", ((NBTPrimitive) nbt).getDouble());
            } else if (nbt instanceof NBTTagString) {
                keyObject.addProperty("value", ((NBTTagString) nbt).getString());
            } else if (nbt instanceof NBTTagList) {
                JsonArray array = new JsonArray();
                NBTTagList tagList = (NBTTagList) nbt;
                for (int i = 0; i < tagList.tagCount(); i++) {
                    array.add(getObject(tagList.getCompoundTagAt(i)));
                }
                keyObject.add("value", array);
            } else if (nbt instanceof NBTTagIntArray) {
                JsonArray array = new JsonArray();
                NBTTagIntArray intArray = (NBTTagIntArray) nbt;
                for (int i : intArray.getIntArray()) {
                    array.add(new JsonPrimitive(i));
                }
                keyObject.add("value", array);
            } else {
                throw new IllegalArgumentException("NBT to JSON converter doesn't support the nbt tag: " + NBTBase.NBT_TYPES[nbt.getId()] + ", tag: " + nbt);
            }
        }
        return jsonRoot;
    }
}
