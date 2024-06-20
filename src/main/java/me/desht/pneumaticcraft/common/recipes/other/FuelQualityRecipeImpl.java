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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.recipe.FuelQualityRecipe;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

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
        return fuel.test(new FluidStack(inputFluid, 1000));
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

    public interface IFactory<T extends FuelQualityRecipe> {
        T create(FluidIngredient fluid, int airPerBucket, float burnRate);
    }

    public static class Serializer<T extends FuelQualityRecipe> implements RecipeSerializer<T> {
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf,T> streamCodec;

        public Serializer(IFactory<T> factory) {
            this.codec = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    FluidIngredient.CODEC_NON_EMPTY.fieldOf("fluid").forGetter(FuelQualityRecipe::getFuel),
                    ExtraCodecs.POSITIVE_INT.fieldOf("air_per_bucket").forGetter(FuelQualityRecipe::getAirPerBucket),
                    ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("burn_rate", 1f).forGetter(FuelQualityRecipe::getBurnRate)
            ).apply(builder, factory::create));
            this.streamCodec = StreamCodec.composite(
                    FluidIngredient.STREAM_CODEC, FuelQualityRecipe::getFuel,
                    ByteBufCodecs.INT, FuelQualityRecipe::getAirPerBucket,
                    ByteBufCodecs.FLOAT, FuelQualityRecipe::getBurnRate,
                    factory::create
            );
        }

        @Override
        public MapCodec<T> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return streamCodec;
        }
    }
}
