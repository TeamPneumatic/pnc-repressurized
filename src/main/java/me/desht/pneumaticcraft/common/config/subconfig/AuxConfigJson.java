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

package me.desht.pneumaticcraft.common.config.subconfig;

import com.google.common.base.Charsets;
import com.google.gson.*;
import me.desht.pneumaticcraft.lib.Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

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

        JsonElement el = JsonParser.parseString(jsonString);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileUtils.write(file, gson.toJson(el), Charsets.UTF_8);
    }

    public void tryWriteToFile() {
        try {
            writeToFile();
        } catch (IOException | NoSuchElementException e) {
            Log.stacktrace("Failed to save config", e);
        }
    }

    private void readFromFile() throws IOException {
        JsonObject root = (JsonObject) JsonParser.parseString(FileUtils.readFileToString(file, Charsets.UTF_8));
        readFromJson(root);
    }

    protected abstract void writeToJson(JsonObject json);

    protected abstract void readFromJson(JsonObject json);
}
