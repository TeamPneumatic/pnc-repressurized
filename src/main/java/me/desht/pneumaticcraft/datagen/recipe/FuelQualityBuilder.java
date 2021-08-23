package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class FuelQualityBuilder extends PneumaticCraftRecipeBuilder<FuelQualityBuilder> {
    private final FluidIngredient fuel;
    private final int airPerBucket;
    private final float burnRate;

    public FuelQualityBuilder(FluidIngredient fuel, int airPerBucket, float burnRate) {
        super(RL(PneumaticCraftRecipeTypes.FUEL_QUALITY));

        this.fuel = fuel;
        this.airPerBucket = airPerBucket;
        this.burnRate = burnRate;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new FuelQualityRecipeResult(id);
    }

    public class FuelQualityRecipeResult extends RecipeResult {
        FuelQualityRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("fluid", fuel.toJson());
            json.addProperty("air_per_bucket", airPerBucket);
            json.addProperty("burn_rate", burnRate);
        }
    }
}
