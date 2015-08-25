package pneumaticCraft.common.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class JsonConfig implements ISubConfig{
    protected File file;
    private final boolean inInit;

    public JsonConfig(boolean inInit){
        this.inInit = inInit;
    }

    @Override
    public void init(File file) throws IOException{
        this.file = file;
        if(inInit) {
            if(file.exists()) {
                readFromFile();
                writeToFile();//Write back to the file so tags that weren't there in last version are included.
            } else {
                file.createNewFile();
                writeToFile();
            }
        }
    }

    @Override
    public void postInit() throws IOException{
        if(!inInit) {
            if(file.exists()) {
                readFromFile();
                writeToFile();
            } else {
                file.createNewFile();
                writeToFile();
            }
        }
    }

    public void writeToFile() throws IOException{
        JsonObject root = new JsonObject();
        writeToJson(root);
        String jsonString = root.toString();

        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonElement el = parser.parse(jsonString);
        FileUtils.write(file, gson.toJson(el));
    }

    private void readFromFile() throws IOException{
        JsonParser parser = new JsonParser();
        JsonObject root = (JsonObject)parser.parse(FileUtils.readFileToString(file));
        readFromJson(root);
    }

    protected abstract void writeToJson(JsonObject json);

    protected abstract void readFromJson(JsonObject json);
}
