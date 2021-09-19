package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.common.amadron.LocationFilter;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class AmadronRecipeBuilder extends PneumaticCraftRecipeBuilder<AmadronRecipeBuilder> {
    private final AmadronTradeResource input;
    private final AmadronTradeResource output;
    private final boolean isStatic;
    private final int level;
    private final int maxStock;
    private final LocationFilter whitelist;
    private final LocationFilter blacklist;

    public AmadronRecipeBuilder(AmadronTradeResource input, AmadronTradeResource output, boolean isStatic, int level,
                                int maxStock, LocationFilter whitelist, LocationFilter blacklist) {
        super(RL(PneumaticCraftRecipeTypes.AMADRON_OFFERS));
        this.input = input;
        this.output = output;
        this.isStatic = isStatic;
        this.level = level;
        this.maxStock = maxStock;
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }

    public AmadronRecipeBuilder(AmadronTradeResource input, AmadronTradeResource output, boolean isStatic, int level) {
        this(input, output, isStatic, level, -1, LocationFilter.YES, LocationFilter.NO);
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new AmadronRecipeResult(id);
    }

    public class AmadronRecipeResult extends RecipeResult {
        AmadronRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            new AmadronOffer(getId(), input, output, isStatic, level, maxStock, maxStock, whitelist, blacklist).toJson(json);
        }
    }
}
