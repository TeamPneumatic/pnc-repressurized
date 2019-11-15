package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.recipe.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer.TradeResource;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;

public class PneumaticRecipeRegistry implements IPneumaticRecipeRegistry {

    private static final PneumaticRecipeRegistry INSTANCE = new PneumaticRecipeRegistry();

    public static PneumaticRecipeRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerDefaultStaticAmadronOffer(TradeResource input, TradeResource output) {
        AmadronOffer offer = new AmadronOffer(input, output);
        AmadronOfferManager.getInstance().addStaticOffer(offer);
    }

    @Override
    public void registerDefaultPeriodicAmadronOffer(TradeResource input, TradeResource output) {
        AmadronOffer offer = new AmadronOffer(input, output);
        AmadronOfferManager.getInstance().addPeriodicOffer(offer);
    }
}
