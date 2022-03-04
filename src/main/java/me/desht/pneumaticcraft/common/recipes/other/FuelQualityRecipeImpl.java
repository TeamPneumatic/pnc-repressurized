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

package me.desht.pneumaticcraft.common.recipes.other;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.FuelQualityRecipe;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;

public class FuelQualityRecipeImpl extends FuelQualityRecipe {
    private final FluidIngredient fuel;
    private final int airPerBucket;
    private final float burnRate;

    public FuelQualityRecipeImpl(ResourceLocation id, FluidIngredient fuel, int airPerBucket, float burnRate) {
        super(id);

        Validate.isTrue(fuel.getAmount() > 0);

        this.fuel = fuel;
        this.airPerBucket = airPerBucket * (1000 / fuel.getAmount());
        this.burnRate = burnRate;
    }

    @Override
    public boolean matchesFluid(Fluid inputFluid) {
        return fuel.testFluid(inputFluid);
    }

    @Override
    public FluidIngredient getFuel() {
        return fuel;
    }

    @Override
    public int getAirPerBucket() {
        return airPerBucket;
    }

    @Override
    public float getBurnRate() {
        return burnRate;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        fuel.toNetwork(buffer);
        buffer.writeInt(airPerBucket);
        buffer.writeFloat(burnRate);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FUEL_QUALITY.get();
    }

    @Override
    public RecipeType<?> getType() {
        return PneumaticCraftRecipeType.fuelQuality;
    }

    public static class Serializer<T extends FuelQualityRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {
        private final IFactory<T> factory;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient fluidInput = FluidIngredient.fromJson(json.get("fluid"));
            int airPerBucket = GsonHelper.getAsInt(json, "air_per_bucket");
            float burnRate = GsonHelper.getAsFloat(json, "burn_rate", 1f);

            return factory.create(recipeId, (FluidIngredient) fluidInput, airPerBucket, burnRate);
        }

        @Nullable
        @Override
        public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            FluidIngredient fluidIn = (FluidIngredient) Ingredient.fromNetwork(buffer);
            int airPerBucket = buffer.readInt();
            float burnRate = buffer.readFloat();

            return factory.create(recipeId, fluidIn, airPerBucket, burnRate);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory <T extends FuelQualityRecipe> {
            T create(ResourceLocation id, FluidIngredient fluid, int airPerBucket, float burnRate);
        }
    }
}
