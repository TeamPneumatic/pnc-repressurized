package pneumaticCraft.common.config;

import java.util.Collection;

import net.minecraft.util.MathHelper;
import pneumaticCraft.common.recipes.AmadronOffer;
import pneumaticCraft.common.recipes.AmadronOfferManager;

import com.google.gson.JsonObject;

public class AmadronOfferPeriodicConfig extends AmadronOfferConfig{
    public static int timesPerDay = 1;
    public static int offersPer = 20;

    @Override
    public String getFolderName(){
        return "AmadronOffersPeriodic";
    }

    @Override
    protected String getComment(){
        return "Offers in here are periodic. Every 1 in [timesPerDay] Minecraft days [offersPer] random offers are selected from here.";
    }

    @Override
    protected void writeToJsonCustom(JsonObject object){
        object.addProperty("timesPerDay", timesPerDay);
        object.addProperty("offersPer", offersPer);
    }

    @Override
    protected void readFromJsonCustom(JsonObject object){
        timesPerDay = object.get("timesPerDay").getAsInt();
        offersPer = object.get("offersPer").getAsInt();
        timesPerDay = MathHelper.clamp_int(timesPerDay, 1, 24000);
    }

    @Override
    protected Collection<AmadronOffer> getOffers(){
        return AmadronOfferManager.getInstance().getPeriodicOffers();
    }

}
