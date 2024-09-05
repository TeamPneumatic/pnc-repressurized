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

package me.desht.pneumaticcraft.common.recipes.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ThermoPlantRecipeImpl extends ThermoPlantRecipe {
    private final Inputs inputs;
    private final Outputs outputs;
    private final float requiredPressure;
    private final float recipeSpeed;
    private final boolean exothermic;
    private final TemperatureRange operatingTemperature;
    private final float airUseMultiplier;

    public ThermoPlantRecipeImpl(
            Inputs inputs, Outputs outputs,
            TemperatureRange operatingTemperature, float requiredPressure,
            float recipeSpeed, float airUseMultiplier, boolean exothermic)
    {
        this.inputs = inputs;
        this.outputs = outputs;
        this.operatingTemperature = operatingTemperature;
        this.requiredPressure = requiredPressure;
        this.recipeSpeed = recipeSpeed;
        this.airUseMultiplier = airUseMultiplier;
        this.exothermic = exothermic;
    }

    @Override
    public boolean matches(FluidStack fluidStack, @Nonnull ItemStack itemStack) {
        // note: input fluid is a SizedFluidIngredient but we're not interested in amount at this point
        boolean itemOK = inputs.inputItem().map(ingr -> ingr.test(itemStack)).orElse(itemStack.isEmpty());
        boolean fluidOK = inputs.inputFluid().map(ingr -> ingr.ingredient().test(fluidStack)).orElse(fluidStack.isEmpty());

        return itemOK && fluidOK;
    }

    @Override
    public TemperatureRange getOperatingTemperature() {
        return operatingTemperature;
    }

    @Override
    public float getRequiredPressure() {
        return requiredPressure;
    }

    @Override
    public Optional<SizedFluidIngredient> getInputFluid() {
        return inputs.inputFluid();
    }

    @Nonnull
    @Override
    public Optional<Ingredient> getInputItem() {
        return inputs.inputItem();
    }

    @Override
    public FluidStack getOutputFluid() {
        return outputs.outputFluid();
    }

    @Override
    public ItemStack getOutputItem() {
        return outputs.outputItem();
    }

    @Override
    public boolean isExothermic() {
        return exothermic;
    }

    @Override
    public Inputs inputs() {
        return inputs;
    }

    @Override
    public Outputs outputs() {
        return outputs;
    }

    @Override
    public float getRecipeSpeed() {
        return recipeSpeed;
    }

    @Override
    public float getAirUseMultiplier() {
        return airUseMultiplier;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.THERMO_PLANT.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.THERMO_PLANT.get();
    }

    @Override
    public String getGroup() {
        return Names.MOD_ID + ":thermo_plant";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get());
    }

    public interface IFactory <T extends ThermoPlantRecipe> {
        T create(Inputs inputs, Outputs outputs, TemperatureRange operatingTemperature,
                 float requiredPressure, float recipeSpeed, float airUseMultiplier, boolean exothermic);
    }

    public static class Serializer<T extends ThermoPlantRecipe> implements RecipeSerializer<T> {
        private final MapCodec<T> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

        public Serializer(IFactory<T> factory) {
            this.codec = RecordCodecBuilder.<T>mapCodec(inst -> inst.group(
                            Inputs.CODEC.fieldOf("inputs")
                                    .forGetter(ThermoPlantRecipe::inputs),
                            Outputs.CODEC.fieldOf("outputs")
                                    .forGetter(ThermoPlantRecipe::outputs),
                            TemperatureRange.CODEC.optionalFieldOf("temperature", TemperatureRange.any())
                                    .forGetter(ThermoPlantRecipe::getOperatingTemperature),
                            Codec.FLOAT.optionalFieldOf("pressure", 0f)
                                    .forGetter(ThermoPlantRecipe::getRequiredPressure),
                            Codec.FLOAT.optionalFieldOf("speed", 1f)
                                    .forGetter(ThermoPlantRecipe::getRecipeSpeed),
                            Codec.FLOAT.optionalFieldOf("air_use_multiplier", 1f)
                                    .forGetter(ThermoPlantRecipe::getAirUseMultiplier),
                            Codec.BOOL.optionalFieldOf("exothermic", false)
                                    .forGetter(ThermoPlantRecipe::isExothermic)
                    ).apply(inst, factory::create))
                    .validate(recipe -> recipe.getInputItem().isPresent() || recipe.getInputFluid().isPresent() ?
                            DataResult.success(recipe) :
                            DataResult.error(() -> "at least one of item_input or fluid_input must be present!", recipe)
                    );

            this.streamCodec = NeoForgeStreamCodecs.composite(
                    Inputs.STREAM_CODEC, ThermoPlantRecipe::inputs,
                    Outputs.STREAM_CODEC, ThermoPlantRecipe::outputs,
                    TemperatureRange.STREAM_CODEC, ThermoPlantRecipe::getOperatingTemperature,
                    ByteBufCodecs.FLOAT, ThermoPlantRecipe::getRequiredPressure,
                    ByteBufCodecs.FLOAT, ThermoPlantRecipe::getRecipeSpeed,
                    ByteBufCodecs.FLOAT, ThermoPlantRecipe::getAirUseMultiplier,
                    ByteBufCodecs.BOOL, ThermoPlantRecipe::isExothermic,
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
