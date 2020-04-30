package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class AmadronRecipeBuilder extends PneumaticCraftRecipeBuilder<AmadronRecipeBuilder> {
    private final AmadronTradeResource input;
    private final AmadronTradeResource output;
    private final boolean isStatic;
    private final int level;

    public AmadronRecipeBuilder(AmadronTradeResource input, AmadronTradeResource output, boolean isStatic, int level) {
        super(RL(PneumaticCraftRecipeTypes.AMADRON_OFFERS));
        this.input = input;
        this.output = output;
        this.isStatic = isStatic;
        this.level = level;
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
        public void serialize(JsonObject json) {
            json.addProperty("static", isStatic);
            json.add("input", input.toJson());
            json.add("output", output.toJson());
            json.addProperty("static", isStatic);
            json.addProperty("level", level);
        }
    }
}
