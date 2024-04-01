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

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ArmorFeatureStatus extends AuxConfigJson {
    // a tri-state (on, off, or as-yet unknown)
    private final Map<ResourceLocation, Boolean> activeUpgrades = Maps.newHashMap();
    private boolean changed = true;

    public static final ArmorFeatureStatus INSTANCE = new ArmorFeatureStatus();

    private ArmorFeatureStatus() {
        super(false);
    }

    @Override
    public String getConfigFilename() {
        return "ArmorFeatureStatus";
    }

    @Override
    protected void writeToJson(JsonObject json) {
        json.addProperty("description", "Tracks the active upgrades for the Pneumatic Armor (clientside)");
        JsonObject features = new JsonObject();
        activeUpgrades.forEach((id, val) -> features.addProperty(id.toString(), val));
        json.add("features", features);
    }

    @Override
    protected void readFromJson(JsonObject json) {
        if (json.has("features")) {
            JsonObject features = json.getAsJsonObject("features");
            for (var entry : features.entrySet()) {
                try {
                    activeUpgrades.put(new ResourceLocation(entry.getKey()), entry.getValue().getAsBoolean());
                } catch (ResourceLocationException | ClassCastException | IllegalStateException e) {
                    Log.error("ignoring invalid entry '{}' in ArmorFeatureStatus.json: {}", entry.getKey(), e.getMessage());
                }
            }
        }
    }

    public void setUpgradeEnabled(ResourceLocation upgradeID, boolean enabled) {
        activeUpgrades.put(upgradeID, enabled);
        changed = true;
    }

    public boolean isUpgradeEnabled(ResourceLocation upgradeID) {
        if (!activeUpgrades.containsKey(upgradeID)) {
            String[] parts = upgradeID.getPath().split("\\.");
            IArmorUpgradeClientHandler<?> handler = ClientArmorRegistry.getInstance().getClientHandler(new ResourceLocation(upgradeID.getNamespace(), parts[0]));
            if (handler == null) {
                Log.warning("attempt to retrieve enablement for unknown upgrade ID '{}'", upgradeID);
                return false;
            }
            if (parts.length == 1) {
                // the upgrade itself
                setUpgradeEnabled(upgradeID, handler.isEnabledByDefault());
            } else {
                // a sub-feature of an upgrade
                setUpgradeEnabled(upgradeID, handler.isEnabledByDefault(upgradeID.getPath()));
            }
        }
        return activeUpgrades.get(upgradeID);
    }

    @Override
    public Sidedness getSidedness() {
        return Sidedness.CLIENT;
    }

    public void saveIfChanged() {
        if (changed) {
            tryWriteToFile();
            changed = false;
        }
    }
}
