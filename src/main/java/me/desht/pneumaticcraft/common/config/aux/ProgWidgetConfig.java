package me.desht.pneumaticcraft.common.config.aux;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class ProgWidgetConfig extends AuxConfigJson {
    public static final Set<ResourceLocation> blacklistedPieces = new HashSet<>();

    public static final ProgWidgetConfig INSTANCE = new ProgWidgetConfig();

    public ProgWidgetConfig() {
        super(true);
    }

    @Override
    public String getConfigFilename() {
        return "ProgrammingPuzzleBlacklist";
    }

    @Override
    protected void writeToJson(JsonObject json) {
        json.addProperty("description", "In the 'blacklist' tag you can put the programming puzzle names that need to blacklisted from this instance. When they were used in existing programs already they will be deleted. A list of all programming puzzle names can be seen in 'allWidgets'.");
//        JsonArray array = new JsonArray();
//        List<String> names = new ArrayList<>(WidgetRegistrator.getAllWidgetNames());
//        Collections.sort(names);
//        for (String name : names) {
//            array.add(new JsonPrimitive(name));
//        }
//        json.add("allWidgets", array);
        JsonArray array = new JsonArray();
        for (ResourceLocation name : blacklistedPieces) {
            array.add(new JsonPrimitive(name.toString()));
        }
        json.add("blacklist", array);
    }

    @Override
    protected void readFromJson(JsonObject json) {
        JsonArray array = json.get("blacklist").getAsJsonArray();
        blacklistedPieces.clear();
        for (JsonElement element : array) {
            blacklistedPieces.add(new ResourceLocation(element.getAsString()));
        }
    }

}
