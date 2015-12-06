package pneumaticCraft.common.util;

import java.util.Set;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class NBTToJsonConverter{
    private final NBTTagCompound tag;

    public NBTToJsonConverter(NBTTagCompound tag){
        this.tag = tag;
    }

    public String convert(){
        JsonObject json = getObject(tag);
        String jsonString = json.toString();

        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonElement el = parser.parse(jsonString);
        return gson.toJson(el); // done
    }

    private JsonObject getObject(NBTTagCompound tag){
        Set<String> keys = tag.func_150296_c();
        JsonObject jsonRoot = new JsonObject();
        for(String key : keys) {
            JsonObject keyObject = new JsonObject();
            jsonRoot.add(key, keyObject);
            NBTBase nbt = tag.getTag(key);

            keyObject.addProperty("type", nbt.getId());

            if(nbt instanceof NBTTagCompound) {
                keyObject.add("value", getObject((NBTTagCompound)nbt));
            } else if(nbt instanceof NBTPrimitive) {
                keyObject.addProperty("value", ((NBTPrimitive)nbt).func_150286_g());
            } else if(nbt instanceof NBTTagString) {
                keyObject.addProperty("value", ((NBTTagString)nbt).func_150285_a_());
            } else if(nbt instanceof NBTTagList) {
                JsonArray array = new JsonArray();
                NBTTagList tagList = (NBTTagList)nbt;
                for(int i = 0; i < tagList.tagCount(); i++) {
                    array.add(getObject(tagList.getCompoundTagAt(i)));
                }
                keyObject.add("value", array);
            } else if(nbt instanceof NBTTagIntArray) {
                JsonArray array = new JsonArray();
                NBTTagIntArray intArray = (NBTTagIntArray)nbt;
                for(int i : intArray.func_150302_c()) {
                    array.add(new JsonPrimitive(i));
                }
                keyObject.add("value", array);
            } else {
                throw new IllegalArgumentException("NBT to JSON converter doesn't support the nbt tag: " + NBTBase.NBTTypes[nbt.getId()] + ", tag: " + nbt);
            }
        }
        return jsonRoot;
    }
}
