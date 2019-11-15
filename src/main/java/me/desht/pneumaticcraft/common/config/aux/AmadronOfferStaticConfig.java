package me.desht.pneumaticcraft.common.config.aux;

import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;

import java.util.Collection;

public class AmadronOfferStaticConfig extends AmadronOfferConfig {
    public static final AmadronOfferStaticConfig INSTANCE = new AmadronOfferStaticConfig();

    @Override
    public String getConfigFilename() {
        return "AmadronOffersStatic";
    }

    @Override
    protected String getComment() {
        return "Offers in here are static, meaning they will always exist to be traded with, unlike periodic offers.";
    }

    @Override
    protected Collection<AmadronOffer> getOffers() {
        return AmadronOfferManager.getInstance().getStaticOffers();
    }

}
