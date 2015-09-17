package pneumaticCraft.common.config;

import java.util.Collection;

import pneumaticCraft.common.recipes.AmadronOffer;
import pneumaticCraft.common.recipes.AmadronOfferCustom;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class AmadronOfferConfig extends JsonConfig{

    public AmadronOfferConfig(){
        super(false);
    }

    protected abstract Collection<AmadronOffer> getOffers();

    protected abstract String getComment();

    @Override
    protected final void writeToJson(JsonObject json){
        JsonArray array = new JsonArray();
        for(AmadronOffer offer : getOffers()) {
            array.add(offer.toJson());
        }
        json.addProperty("description", getComment());
        writeToJsonCustom(json);
        json.add("offers", array);
    }

    @Override
    protected final void readFromJson(JsonObject json){
        readFromJsonCustom(json);
        JsonArray array = (JsonArray)json.get("offers");
        Collection<AmadronOffer> offers = getOffers();
        offers.clear();
        for(JsonElement element : array) {
            AmadronOffer offer = ((JsonObject)element).has("inStock") ? AmadronOfferCustom.fromJson((JsonObject)element) : AmadronOffer.fromJson((JsonObject)element);
            if(offer != null) {
                offers.add(offer);
            }
        }
    }

    protected void readFromJsonCustom(JsonObject json){}

    protected void writeToJsonCustom(JsonObject json){}

}
