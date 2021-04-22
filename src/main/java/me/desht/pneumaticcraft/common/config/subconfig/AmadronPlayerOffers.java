package me.desht.pneumaticcraft.common.config.subconfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class AmadronPlayerOffers extends AuxConfigJson {
    public static final AmadronPlayerOffers INSTANCE = new AmadronPlayerOffers();

    private static final String DESC =
            "Stores all the current player-to-player Amadron trades,"
            + "along with stock information, pending payments etc.";

    private final Map<ResourceLocation, AmadronPlayerOffer> playerOffers = new HashMap<>();

    private AmadronPlayerOffers() {
        super(false);
    }

    @Override
    public String getConfigFilename() {
        return "AmadronPlayerOffers";
    }

    public Map<ResourceLocation, AmadronPlayerOffer> getPlayerOffers() {
        return playerOffers;
    }

    public static void save() {
        INSTANCE.tryWriteToFile();
    }

    @Override
    public void clear() {
        playerOffers.clear();
    }

    @Override
    protected void writeToJson(JsonObject json) {
        JsonArray array = new JsonArray();
        for (AmadronPlayerOffer offer : playerOffers.values()) {
            array.add(offer.toJson(new JsonObject()));
        }
        json.addProperty("description", DESC);
        json.add("offers", array);
    }

    @Override
    protected void readFromJson(JsonObject json) {
        JsonArray array = (JsonArray) json.get("offers");
        playerOffers.clear();
        for (JsonElement element : array) {
            try {
                AmadronPlayerOffer offer = AmadronPlayerOffer.fromJson((JsonObject) element);
                playerOffers.put(offer.getId(), offer);
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }
        // need to get the newly-read offers into the offer manager, which has already loaded static & villager offers
        AmadronOfferManager.getInstance().addPlayerOffers();
    }

    @Override
    public boolean useWorldSpecificDir() {
        return true;
    }
}
