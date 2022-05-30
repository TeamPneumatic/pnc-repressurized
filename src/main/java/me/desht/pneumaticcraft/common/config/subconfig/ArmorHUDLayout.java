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

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.StatPanelLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.*;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ArmorHUDLayout extends AuxConfigJson {
    public static final ArmorHUDLayout INSTANCE = new ArmorHUDLayout();
    private static final String HUD_LAYOUT = "hud_layout";

    private final Map<ResourceLocation, StatPanelLayout> layouts = new HashMap<>();

    private ArmorHUDLayout() {
        super(true);
    }

    @Override
    protected void writeToJson(JsonObject json) {
        json.addProperty("Description", "Stores the layout of Pneumatic Armor HUD elements");
        JsonObject sub = new JsonObject();
        layouts.forEach((id, layout) -> sub.add(id.toString(), layout.toJson()));
        json.add(HUD_LAYOUT, sub);
    }

    @Override
    protected void readFromJson(JsonObject json) {
        // note: dedicated server will have neither old "stats" or new "layouts" data
        // this information is only saved client-side

        if (json.has("stats")) {
            // TODO remove in 1.19
            loadLegacy(json.getAsJsonObject("stats"));
        } else if (json.has(HUD_LAYOUT)) {
            JsonObject sub = json.getAsJsonObject(HUD_LAYOUT);
            sub.entrySet().forEach(entry -> {
                try {
                    ResourceLocation id = new ResourceLocation(entry.getKey());
                    layouts.put(id, StatPanelLayout.fromJson(entry.getValue().getAsJsonObject()));
                } catch (IllegalArgumentException e) {
                    Log.error("invalid stat panel key (not a resource location) %s in %s!", entry.getKey(), getConfigFilename());
                } catch (IllegalStateException | NullPointerException e) {
                    Log.error("invalid json for key %s in %s!", entry.getKey(), getConfigFilename());
                }
            });
        }
    }

    private void loadLegacy(JsonObject json) {
        maybeAddLegacy(json, CoreComponentsHandler.ID, "power");
        maybeAddLegacy(json, CoreComponentsHandler.getMessageID(), "message");
        maybeAddLegacy(json, BlockTrackerHandler.ID, "blockTracker");
        maybeAddLegacy(json, EntityTrackerHandler.ID, "entityTracker");
        maybeAddLegacy(json, SearchHandler.ID, "itemSearch");
        maybeAddLegacy(json, AirConHandler.ID, "airCon");
        maybeAddLegacy(json, JetBootsHandler.ID, "jetBoots");
    }
    private void maybeAddLegacy(JsonObject json, ResourceLocation id, String fieldName) {
        if (json.has(fieldName)) layouts.put(id, StatPanelLayout.fromJson(json.get(fieldName).getAsJsonObject()));
    }

    @Override
    public String getConfigFilename() {
        return "PneumaticArmorHUDLayout";
    }

    public void updateLayout(ResourceLocation id, float x, float y, boolean leftSided) {
        layouts.put(id, new StatPanelLayout(x, y, leftSided));
        tryWriteToFile();
    }

    public StatPanelLayout getLayoutFor(ResourceLocation upgradeID, StatPanelLayout defaultStatLayout) {
        return layouts.computeIfAbsent(upgradeID, k -> defaultStatLayout);
    }
}
