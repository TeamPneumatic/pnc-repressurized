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

package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

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
