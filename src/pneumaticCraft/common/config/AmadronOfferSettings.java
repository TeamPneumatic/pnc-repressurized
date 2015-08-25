package pneumaticCraft.common.config;

import com.google.gson.JsonObject;

public class AmadronOfferSettings extends JsonConfig{

    public static int maxTradesPerPlayer = 50;
    public static boolean notifyOfTradeAddition = true;
    public static boolean notifyOfTradeRemoval = true;
    public static boolean notifyOfDealMade = true;

    public AmadronOfferSettings(){
        super(true);
    }

    @Override
    public String getFolderName(){
        return "AmadronTradingSettings";
    }

    @Override
    protected void writeToJson(JsonObject json){
        json.addProperty("description", "Various options to limit the ability of the trading system, to manage spam/abuse on public servers. Most can be changed client-side as well to for example stop notifications if the client wishes. Limitations apply to non-OP players only.");
        json.addProperty("maxTradesPerPlayer", maxTradesPerPlayer);
        json.addProperty("notifyOfTradeAddition", notifyOfTradeAddition);
        json.addProperty("notifyOfTradeRemoval", notifyOfTradeRemoval);
        json.addProperty("notifyOfDealMade", notifyOfDealMade);
    }

    @Override
    protected void readFromJson(JsonObject json){
        maxTradesPerPlayer = json.get("maxTradesPerPlayer").getAsInt();
        notifyOfTradeAddition = json.get("notifyOfTradeAddition").getAsBoolean();
        notifyOfTradeRemoval = json.get("notifyOfTradeRemoval").getAsBoolean();
        notifyOfDealMade = json.get("notifyOfDealMade").getAsBoolean();
    }

}
