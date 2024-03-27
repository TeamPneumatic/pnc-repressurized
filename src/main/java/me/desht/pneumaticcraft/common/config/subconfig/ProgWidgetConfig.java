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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.registry.ModProgWidgets;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class ProgWidgetConfig extends AuxConfigJson {
    private final Set<ResourceLocation> blacklistedPieces = new HashSet<>();

    public static final ProgWidgetConfig INSTANCE = new ProgWidgetConfig();

    private ProgWidgetConfig() {
        super(false);
    }

    public boolean isWidgetBlacklisted(ProgWidgetType<?> widgetType) {
        ResourceLocation regName = PneumaticCraftUtils.getRegistryName(ModProgWidgets.PROG_WIDGETS_REGISTRY, widgetType).orElseThrow();
        return blacklistedPieces.contains(regName);
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
        for (ResourceLocation entry : ModProgWidgets.PROG_WIDGETS_REGISTRY.keySet()) {
            allArray.add(new JsonPrimitive(entry.toString()));
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

    @Override
    public Sidedness getSidedness() {
        return Sidedness.BOTH;
    }
}
