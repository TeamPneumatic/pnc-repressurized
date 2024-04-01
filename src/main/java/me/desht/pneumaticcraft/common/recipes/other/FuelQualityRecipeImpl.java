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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.FuelQualityRecipe;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;

public class FuelQualityRecipeImpl extends FuelQualityRecipe {
    private final FluidIngredient fuel;
    private final int airPerBucket;
    private final float burnRate;

    public FuelQualityRecipeImpl(FluidIngredient fuel, int airPerBucket, float burnRate) {
//        Validate.isTrue(fuel.getAmount() > 0);

        this.fuel = fuel;
        this.airPerBucket = airPerBucket;// * (1000 / fuel.getAmount());
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
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FUEL_QUALITY.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.FUEL_QUALITY.get();
    }

    public static class Serializer<T extends FuelQualityRecipe> implements RecipeSerializer<T> {
        private final IFactory<T> factory;
        private final Codec<T> codec;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
            this.codec = RecordCodecBuilder.create(builder -> builder.group(
                    FluidIngredient.FLUID_CODEC_NON_EMPTY.fieldOf("fluid").forGetter(FuelQualityRecipe::getFuel),
                    ExtraCodecs.POSITIVE_INT.fieldOf("air_per_bucket").forGetter(FuelQualityRecipe::getAirPerBucket),
                    ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("burn_rate", 1f).forGetter(FuelQualityRecipe::getBurnRate)
            ).apply(builder, factory::create));
        }

        @Override
        public Codec<T> codec() {
            return codec;
        }

        @Override
        public T fromNetwork(FriendlyByteBuf buffer) {
            FluidIngredient fluidIn = FluidIngredient.fluidFromNetwork(buffer);
            int airPerBucket = buffer.readInt();
            float burnRate = buffer.readFloat();

            return factory.create(fluidIn, airPerBucket, burnRate);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            recipe.getFuel().fluidToNetwork(buffer);
            buffer.writeInt(recipe.getAirPerBucket());
            buffer.writeFloat(recipe.getBurnRate());
        }

        public interface IFactory<T extends FuelQualityRecipe> {
            T create(FluidIngredient fluid, int airPerBucket, float burnRate);
        }
    }
}
