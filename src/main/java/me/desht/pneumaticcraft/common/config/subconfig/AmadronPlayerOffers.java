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
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.resources.ResourceLocation;

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
            array.add(offer.toJson());
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
                playerOffers.put(offer.getOfferId(), offer);
            } catch (JsonSyntaxException e) {
                Log.error("can't load amadron player offer: {}", e.getMessage());
            }
        }
        // need to get the newly-read offers into the offer manager, which has already loaded static & villager offers
        AmadronOfferManager.getInstance().addPlayerOffers();
    }

    @Override
    public boolean useWorldSpecificDir() {
        return true;
    }

    @Override
    public Sidedness getSidedness() {
        return Sidedness.SERVER;
    }
}
