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

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;

import java.util.Set;

public class ArmorFeatureStatus extends AuxConfigJson {
    private final Set<ResourceLocation> activeUpgrades = Sets.newHashSet();

    public static final ArmorFeatureStatus INSTANCE = new ArmorFeatureStatus();

    private ArmorFeatureStatus() {
        super(true);
    }

    @Override
    public String getConfigFilename() {
        return "ArmorFeatureStatus";
    }

    @Override
    protected void writeToJson(JsonObject json) {
        json.addProperty("description", "Tracks the active upgrades for the Pneumatic Armor (clientside)");
        JsonArray array = new JsonArray();
        if (activeUpgrades.isEmpty()) {
            // armor "master switch" is on by default
            activeUpgrades.add(ArmorUpgradeRegistry.getInstance().coreComponentsHandler.getID());
        }
        for (ResourceLocation s : activeUpgrades) {
            array.add(s.toString());
        }
        json.add("active", array);
    }

    @Override
    protected void readFromJson(JsonObject json) {
        JsonArray array = json.get("active").getAsJsonArray();
        activeUpgrades.clear();
        for (JsonElement element : array) {
            try {
                activeUpgrades.add(new ResourceLocation(element.getAsString()));
            } catch (ResourceLocationException e) {
                Log.error("ignoring " + element.getAsString() + " in ArmorFeatureStatus.json: " + e.getMessage());
            }
        }
    }

    public void setUpgradeEnabled(ResourceLocation upgradeID, boolean enabled) {
        if (enabled) {
            activeUpgrades.add(upgradeID);
        } else {
            activeUpgrades.remove(upgradeID);
        }
    }

    public boolean isUpgradeEnabled(ResourceLocation upgradeID) {
        return activeUpgrades.contains(upgradeID);
    }
}
