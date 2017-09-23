package me.desht.pneumaticcraft.common.config;

import com.google.common.base.Charsets;
import com.google.gson.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public abstract class JsonConfig implements ISubConfig {
    protected File file;
    private final boolean inInit;

    public JsonConfig(boolean inInit) {
        this.inInit = inInit;
    }

    @Override
    public void init(File file) throws IOException {
        this.file = file;
        if (inInit) {
            if (file.exists()) {
                readFromFile();
                writeToFile();//Write back to the file so tags that weren't there in last version are included.
            } else {
                file.createNewFile();
                writeToFile();
            }
        }
    }

    @Override
    public void postInit() throws IOException {
        if (!inInit) {
            if (file.exists()) {
                readFromFile();
                writeToFile();
            } else {
                if (file.createNewFile()) {
                    writeToFile();
                }
            }
        }
    }

    public void writeToFile() throws IOException {
        JsonObject root = new JsonObject();
        writeToJson(root);
        String jsonString = root.toString();

        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonElement el = parser.parse(jsonString);
        FileUtils.write(file, gson.toJson(el), Charsets.UTF_8);
    }

    private void readFromFile() throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject root = (JsonObject) parser.parse(FileUtils.readFileToString(file, Charsets.UTF_8));
        readFromJson(root);
    }

    protected abstract void writeToJson(JsonObject json);

    protected abstract void readFromJson(JsonObject json);
}
