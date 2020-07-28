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
