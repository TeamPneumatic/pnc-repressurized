package me.desht.pneumaticcraft.common.util;

import com.google.gson.*;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.util.Set;

public class NBTToJsonConverter {
    private final NBTTagCompound tag;

    public NBTToJsonConverter(NBTTagCompound tag) {
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

    public static JsonObject getObject(NBTTagCompound tag) {
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
                switch (nbt.getId()) {
                    case Constants.NBT.TAG_BYTE:
                        keyObject.addProperty("value", ((NBTPrimitive) nbt).getByte()); break;
                    case Constants.NBT.TAG_INT:
                        keyObject.addProperty("value", ((NBTPrimitive) nbt).getInt()); break;
                    case Constants.NBT.TAG_SHORT:
                        keyObject.addProperty("value", ((NBTPrimitive) nbt).getShort()); break;
                    case Constants.NBT.TAG_LONG:
                        keyObject.addProperty("value", ((NBTPrimitive) nbt).getLong()); break;
                    case Constants.NBT.TAG_FLOAT:
                        keyObject.addProperty("value", ((NBTPrimitive) nbt).getFloat()); break;
                    case Constants.NBT.TAG_DOUBLE:
                        keyObject.addProperty("value", ((NBTPrimitive) nbt).getDouble()); break;
                }
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
