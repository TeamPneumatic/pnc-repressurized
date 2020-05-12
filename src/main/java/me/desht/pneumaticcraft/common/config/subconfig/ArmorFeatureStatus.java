package me.desht.pneumaticcraft.common.config.subconfig;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Set;

public class ArmorFeatureStatus extends AuxConfigJson {
    private final Set<String> activeUpgrades = Sets.newHashSet();

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
            activeUpgrades.add("coreComponents");
        }
        for (String s : activeUpgrades) {
            array.add(s);
        }
        json.add("active", array);
    }

    @Override
    protected void readFromJson(JsonObject json) {
        JsonArray array = json.get("active").getAsJsonArray();
        activeUpgrades.clear();
        for (JsonElement element : array) {
            activeUpgrades.add(element.getAsString());
        }
    }

    public void setUpgradeEnabled(String upgradeID, boolean enabled) {
        if (enabled) {
            activeUpgrades.add(upgradeID);
        } else {
            activeUpgrades.remove(upgradeID);
        }
    }

    public boolean isUpgradeEnabled(String upgradeID) {
        return activeUpgrades.contains(upgradeID);
    }
}
