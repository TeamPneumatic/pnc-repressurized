package me.desht.pneumaticcraft.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatapackHelper {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final int JSON_EXTENSION_LENGTH = ".json".length();

    /**
     * Load all JSON recipes from the given folder, searching all known datapacks in standard order. If an empty recipe
     * JSON is found for an existing recipe, it will be skipped; this is how datapacks can disable default recipes.
     *
     * @param resourceManager the resource manager
     * @param folder the datapack folder to look under
     * @param tag an informational string tag, for logging purposes
     * @return a map (recipeID -> json) of all discovered recipes for the category
     */
    public static Map<ResourceLocation, JsonObject> loadJSONFiles(IResourceManager resourceManager, String folder, String tag) {
        Map<ResourceLocation, JsonObject> map = new LinkedHashMap<>();
        for (ResourceLocation file : resourceManager.getAllResourceLocations(folder, r -> r.endsWith(".json"))) {
            String path = file.getPath();
            ResourceLocation id = new ResourceLocation(file.getNamespace(), path.substring(folder.length() + 1, path.length() - JSON_EXTENSION_LENGTH));
           try (IResource iresource = resourceManager.getResource(file);
                 InputStream inputstream = iresource.getInputStream();
                 Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))) {
                JsonObject jsonobject = JSONUtils.fromJson(GSON, reader, JsonObject.class);
                if (jsonobject != null) {
                    if (jsonobject.size() == 0) {
                        Log.debug("skipped %s: %s (empty JSON object)", tag, id);
                        map.remove(id);  // shouldn't be present already, but doesn't hurt to do this
                    } else {
                        JsonObject j = map.put(id, jsonobject);
                        Log.debug("loaded %s: %s", tag, id);
                        if (j != null) {
                            Log.error("duplicate %s discovered with ID %s", tag, id);
                        }
                    }
                } else {
                    Log.error("can't load %s '%s' from %s - null JSON object?", tag, id, file);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                Log.error("can't parse %s '%s' (%s) - stack trace follows:", tag, file, e.getMessage());
                Log.error(ExceptionUtils.getStackTrace(e));
            }
        }
        return map;
    }
}
