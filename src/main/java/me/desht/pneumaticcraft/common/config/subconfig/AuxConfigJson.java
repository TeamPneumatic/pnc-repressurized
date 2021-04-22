package me.desht.pneumaticcraft.common.config.subconfig;

import com.google.common.base.Charsets;
import com.google.gson.*;
import me.desht.pneumaticcraft.lib.Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public abstract class AuxConfigJson implements IAuxConfig {
    protected File file;
    private final boolean inPreInit;

    AuxConfigJson(boolean inPreInit) {
        this.inPreInit = inPreInit;
    }

    @Override
    public void preInit(File file) throws IOException {
        this.file = file;
        if (inPreInit) {
            processFile();
        }
    }

    @Override
    public void postInit(File file) throws IOException {
        this.file = file;
        if (!inPreInit) {
            processFile();
        }
    }

    private void processFile() throws IOException {
        if (file.exists()) {
            readFromFile();
            writeToFile();
        } else {
            if (file.createNewFile()) {
                clear();
                writeToFile();
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

    public void tryWriteToFile() {
        try {
            writeToFile();
        } catch (IOException e) {
            Log.stacktrace("Failed to save config", e);
        }
    }

    private void readFromFile() throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject root = (JsonObject) parser.parse(FileUtils.readFileToString(file, Charsets.UTF_8));
        readFromJson(root);
    }

    protected abstract void writeToJson(JsonObject json);

    protected abstract void readFromJson(JsonObject json);
}
