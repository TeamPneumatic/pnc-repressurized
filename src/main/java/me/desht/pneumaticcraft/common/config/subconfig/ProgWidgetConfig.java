package me.desht.pneumaticcraft.common.config.subconfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class ProgWidgetConfig extends AuxConfigJson {
    private final Set<ResourceLocation> blacklistedPieces = new HashSet<>();

    public static final ProgWidgetConfig INSTANCE = new ProgWidgetConfig();

    private ProgWidgetConfig() {
        super(false);
    }

    public boolean isWidgetBlacklisted(ProgWidgetType<?> widgetType) {
        return blacklistedPieces.contains(widgetType.getRegistryName());
    }

    @Override
    public String getConfigFilename() {
        return "ProgrammingPuzzleBlacklist";
    }

    @Override
    protected void writeToJson(JsonObject json) {
        json.addProperty("description",
                "In the 'blacklist' tag you can add any progwidget registry names you wish to blacklist from this instance. " +
                        "When they were used in existing programs already they will be deleted. " +
                        "A reference list of all known programming puzzle names can be seen in 'allWidgets'.");

        JsonArray array = new JsonArray();
        for (ResourceLocation name : blacklistedPieces) {
            array.add(new JsonPrimitive(name.toString()));
        }
        json.add("blacklist", array);

        JsonArray allArray = new JsonArray();
        for (ProgWidgetType<?> name : ModProgWidgets.PROG_WIDGETS.get().getValues()) {
            allArray.add(new JsonPrimitive(name.getRegistryName().toString()));
        }
        json.add("allWidgets", allArray);
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
