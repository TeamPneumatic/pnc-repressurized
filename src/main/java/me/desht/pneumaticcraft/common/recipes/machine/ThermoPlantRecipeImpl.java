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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Optional;

// yeah yeah codecs
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ThermoPlantRecipeImpl extends ThermoPlantRecipe {
    private final Optional<FluidIngredient> inputFluid;
    private final FluidStack outputFluid;
    private final Optional<Ingredient> inputItem;
    private final float requiredPressure;
    private final float recipeSpeed;
    private final boolean exothermic;
    private final TemperatureRange operatingTemperature;
    private final ItemStack outputItem;
    private final float airUseMultiplier;

    public ThermoPlantRecipeImpl(
            Optional<FluidIngredient> inputFluid, Optional<Ingredient> inputItem,
            FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure,
            float recipeSpeed, float airUseMultiplier, boolean exothermic)
    {
        this.inputItem = inputItem;
        this.inputFluid = inputFluid;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.operatingTemperature = operatingTemperature;
        this.requiredPressure = requiredPressure;
        this.recipeSpeed = recipeSpeed;
        this.airUseMultiplier = airUseMultiplier;
        this.exothermic = exothermic;
    }

    @Override
    public boolean matches(FluidStack fluidStack, @Nonnull ItemStack itemStack) {
        boolean itemOK = inputItem.map(ingr -> ingr.test(itemStack)).orElse(itemStack.isEmpty());
        boolean fluidOK = inputFluid.map(ingr -> ingr.testFluid(fluidStack.getFluid())).orElse(fluidStack.isEmpty());

        return itemOK && fluidOK;

//        return (inputFluid.isEmpty() && fluidStack.isEmpty() || inputFluid.testFluid(fluidStack.getFluid()))
//                && (inputItem.isEmpty() && itemStack.isEmpty() || inputItem.test(itemStack));
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
    public Optional<FluidIngredient> getInputFluid() {
        return inputFluid;
    }

    @Nonnull
    @Override
    public Optional<Ingredient> getInputItem() {
        return inputItem;
    }

    @Override
    public FluidStack getOutputFluid() {
        return outputFluid;
    }

    @Override
    public ItemStack getOutputItem() {
        return outputItem;
    }

    @Override
    public boolean isExothermic() {
        return exothermic;
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

    public static class Serializer<T extends ThermoPlantRecipe> implements RecipeSerializer<T> {
        private final IFactory<T> factory;
        private final Codec<T> codec;

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
            this.codec = ExtraCodecs.validate(
                    RecordCodecBuilder.create(inst -> inst.group(
                            FluidIngredient.FLUID_CODEC.optionalFieldOf("fluid_input")
                                    .forGetter(ThermoPlantRecipe::getInputFluid),
                            Ingredient.CODEC.optionalFieldOf("item_input")
                                    .forGetter(ThermoPlantRecipe::getInputItem),
                            FluidStack.CODEC.optionalFieldOf("fluid_output", FluidStack.EMPTY)
                                    .forGetter(ThermoPlantRecipe::getOutputFluid),
                            ItemStack.ITEM_WITH_COUNT_CODEC.optionalFieldOf("item_output", ItemStack.EMPTY)
                                    .forGetter(ThermoPlantRecipe::getOutputItem),
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
                    ).apply(inst, factory::create)),
                    recipe -> recipe.getInputItem().isPresent() || recipe.getInputFluid().isPresent() ?
                            DataResult.success(recipe) :
                            DataResult.error(() -> "at least one of item_input or fluid_input must be present!", recipe)
            );
        }

        @Override
        public Codec<T> codec() {
            return codec;
        }

        @Override
        public T fromNetwork(FriendlyByteBuf buffer) {
            TemperatureRange range = TemperatureRange.read(buffer);
            float pressure = buffer.readFloat();
            Optional<Ingredient> input = buffer.readOptional(Ingredient::fromNetwork);
            Optional<FluidIngredient> fluidIn = buffer.readOptional(FluidIngredient::fluidFromNetwork);
            ItemStack itemOutput = buffer.readItem();
            FluidStack fluidOut = FluidStack.readFromPacket(buffer);
            float recipeSpeed = buffer.readFloat();
            float airUseMultiplier = buffer.readFloat();
            boolean exothermic = buffer.readBoolean();
            return factory.create(fluidIn, input, fluidOut, itemOutput, range, pressure, recipeSpeed, airUseMultiplier, exothermic);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            recipe.getOperatingTemperature().write(buffer);
            buffer.writeFloat(recipe.getRequiredPressure());
            buffer.writeOptional(recipe.getInputItem(), (b, ingredient) -> ingredient.toNetwork(b));
            buffer.writeOptional(recipe.getInputFluid(), (b, ingredient) -> ingredient.fluidToNetwork(b));
            buffer.writeItem(recipe.getOutputItem());
            recipe.getOutputFluid().writeToPacket(buffer);
            buffer.writeFloat(recipe.getRecipeSpeed());
            buffer.writeFloat(recipe.getAirUseMultiplier());
            buffer.writeBoolean(recipe.isExothermic());
        }

        public interface IFactory <T extends ThermoPlantRecipe> {
            T create(Optional<FluidIngredient> inputFluid, Optional<Ingredient> inputItem,
                     FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure,
                     float recipeSpeed, float airUseMultiplier, boolean exothermic);
        }
    }
}
