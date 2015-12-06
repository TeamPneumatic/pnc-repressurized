package pneumaticCraft.common.util;

import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonToNBTConverter{
    private final String jsonString;

    public JsonToNBTConverter(String jsonString){
        this.jsonString = jsonString;
    }

    public NBTTagCompound convert(){
        JsonParser parser = new JsonParser();
        JsonElement el = parser.parse(jsonString);
        return getTag((JsonObject)el);
    }

    private NBTTagCompound getTag(JsonObject object){
        NBTTagCompound nbt = new NBTTagCompound();
        for(Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonObject keyObject = entry.getValue().getAsJsonObject();
            int type = keyObject.get("type").getAsInt();
            JsonElement element = keyObject.get("value");

            switch(type){
                case 1:
                    nbt.setByte(entry.getKey(), (byte)element.getAsDouble());
                    break;
                case 2:
                    nbt.setShort(entry.getKey(), (short)element.getAsDouble());

                case 3:
                    nbt.setInteger(entry.getKey(), (int)element.getAsDouble());
                    break;
                case 4:
                    nbt.setLong(entry.getKey(), (long)element.getAsDouble());
                    break;
                case 5:
                    nbt.setFloat(entry.getKey(), (float)element.getAsDouble());
                    break;
                case 6:
                    nbt.setDouble(entry.getKey(), element.getAsDouble());
                    break;
                //   case 7:
                //       return new NBTTagByteArray();
                //   break;
                case 8:
                    nbt.setString(entry.getKey(), element.getAsString());
                    break;
                case 9:
                    JsonArray array = element.getAsJsonArray();
                    NBTTagList tagList = new NBTTagList();
                    for(JsonElement e : array) {
                        tagList.appendTag(getTag(e.getAsJsonObject()));
                    }
                    nbt.setTag(entry.getKey(), tagList);
                    break;
                case 10:
                    nbt.setTag(entry.getKey(), getTag(element.getAsJsonObject()));
                    break;
                case 11:
                    array = element.getAsJsonArray();
                    int[] intArray = new int[array.size()];
                    for(int i = 0; i < array.size(); i++) {
                        intArray[i] = array.get(i).getAsInt();
                    }
                    nbt.setTag(entry.getKey(), new NBTTagIntArray(intArray));
                    break;
                default:
                    throw new IllegalArgumentException("NBT type no " + type + " is not supported by the Json to NBT converter!");
            }
        }
        return nbt;
    }
}
