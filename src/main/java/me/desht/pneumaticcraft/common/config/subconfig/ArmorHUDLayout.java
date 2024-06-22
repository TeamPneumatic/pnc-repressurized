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
import com.mojang.serialization.JsonOps;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.StatPanelLayout;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

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
        layouts.forEach((id, layout) -> sub.add(id.toString(), StatPanelLayout.CODEC.encodeStart(JsonOps.INSTANCE, layout).result().orElseThrow()));
        json.add(HUD_LAYOUT, sub);
    }

    @Override
    protected void readFromJson(JsonObject json) {
        // note: dedicated server not have "layouts" data
        // this information is only saved client-side

        if (json.has(HUD_LAYOUT)) {
            JsonObject sub = json.getAsJsonObject(HUD_LAYOUT);
            sub.entrySet().forEach(entry -> {
                try {
                    ResourceLocation id = ResourceLocation.parse(entry.getKey());
                    layouts.put(id, StatPanelLayout.CODEC.parse(JsonOps.INSTANCE, entry.getValue().getAsJsonObject()).result().orElseThrow());
                } catch (ResourceLocationException e) {
                    Log.error("invalid stat panel key (not a resource location) {} in {}}!", entry.getKey(), getConfigFilename());
                } catch (NoSuchElementException e) {
                    Log.error("invalid json for key {} in {}!", entry.getKey(), getConfigFilename());
                }
            });
        }
    }

    @Override
    public String getConfigFilename() {
        return "PneumaticArmorHUDLayout";
    }

    @Override
    public Sidedness getSidedness() {
        return Sidedness.CLIENT;
    }

    public void updateLayout(ResourceLocation id, float x, float y, boolean leftSided, boolean hidden) {
        layouts.put(id, new StatPanelLayout(x, y, leftSided, hidden));
        tryWriteToFile();
    }

    public StatPanelLayout getLayoutFor(ResourceLocation upgradeID, StatPanelLayout defaultStatLayout) {
        return layouts.computeIfAbsent(upgradeID, k -> defaultStatLayout);
    }
}
