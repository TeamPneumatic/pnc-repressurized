package me.desht.pneumaticcraft.common.config;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;

public class AmadronOfferPeriodicConfig extends AmadronOfferConfig {
    public static int timesPerDay = 1;
    public static int offersPer = 20;

    @Override
    public String getFolderName() {
        return "AmadronOffersPeriodic";
    }

    @Override
    protected String getComment() {
        return "Offers in here are periodic. Every 1 in [timesPerDay] Minecraft days [offersPer] random offers are selected from here.";
    }

    @Override
    protected void writeToJsonCustom(JsonObject object) {
        object.addProperty("timesPerDay", timesPerDay);
        object.addProperty("offersPer", offersPer);
    }

    @Override
    protected void readFromJsonCustom(JsonObject object) {
        timesPerDay = object.get("timesPerDay").getAsInt();
        offersPer = object.get("offersPer").getAsInt();
        timesPerDay = MathHelper.clamp(timesPerDay, 1, 24000);
    }

    @Override
    protected Collection<AmadronOffer> getOffers() {
        return AmadronOfferManager.getInstance().getPeriodicOffers();
    }

}
