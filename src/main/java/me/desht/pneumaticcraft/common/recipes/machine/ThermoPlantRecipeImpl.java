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

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModRecipeSerializers;
import me.desht.pneumaticcraft.common.core.ModRecipeTypes;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ThermoPlantRecipeImpl extends ThermoPlantRecipe {
    private final FluidIngredient inputFluid;
    private final FluidStack outputFluid;
    private final Ingredient inputItem;
    private final float requiredPressure;
    private final float recipeSpeed;
    private final boolean exothermic;
    private final TemperatureRange operatingTemperature;
    private final ItemStack outputItem;
    private final float airUseMultiplier;

    public ThermoPlantRecipeImpl(
            ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nonnull Ingredient inputItem,
            FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure,
            float recipeSpeed, float airUseMultiplier, boolean exothermic)
    {
        super(id);

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
        return (inputFluid.isEmpty() && fluidStack.isEmpty() || inputFluid.testFluid(fluidStack.getFluid()))
                && (inputItem.isEmpty() && itemStack.isEmpty() || inputItem.test(itemStack));
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
    public FluidIngredient getInputFluid() {
        return inputFluid;
    }

    @Nonnull
    @Override
    public Ingredient getInputItem() {
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
    public void write(FriendlyByteBuf buffer) {
        operatingTemperature.write(buffer);
        buffer.writeFloat(requiredPressure);
        inputItem.toNetwork(buffer);
        inputFluid.toNetwork(buffer);
        outputFluid.writeToPacket(buffer);
        buffer.writeItem(outputItem);
        buffer.writeFloat(recipeSpeed);
        buffer.writeFloat(airUseMultiplier);
        buffer.writeBoolean(exothermic);
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

        public Serializer(IFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T fromJson(ResourceLocation recipeId, JsonObject json) {
            if (!json.has("item_input") && !json.has("fluid_input")) {
                throw new JsonSyntaxException("Must have at least one of item_input and/or fluid_input!");
            }
            if (!json.has("item_output") && !json.has("fluid_output")) {
                throw new JsonSyntaxException("Must have at least one of item_output and/or fluid_output!");
            }

            Ingredient itemInput = json.has("item_input") ?
                    Ingredient.fromJson(json.get("item_input")) :
                    Ingredient.EMPTY;
            Ingredient fluidInput = json.has("fluid_input") ?
                    FluidIngredient.fromJson(json.get("fluid_input")) :
                    FluidIngredient.EMPTY;

            FluidStack fluidOutput = json.has("fluid_output") ?
                    ModCraftingHelper.fluidStackFromJson(json.getAsJsonObject("fluid_output")):
                    FluidStack.EMPTY;
            ItemStack itemOutput = json.has("item_output") ?
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "item_output")) :
                    ItemStack.EMPTY;

            TemperatureRange range = json.has("temperature") ?
                    TemperatureRange.fromJson(json.getAsJsonObject("temperature")) :
                    TemperatureRange.any();

            float pressure = GsonHelper.getAsFloat(json, "pressure", 0f);

            boolean exothermic = GsonHelper.getAsBoolean(json, "exothermic", false);

            float recipeSpeed = GsonHelper.getAsFloat(json, "speed", 1.0f);

            float airUseMultiplier = GsonHelper.getAsFloat(json, "air_use_multiplier", 1.0f);

            return factory.create(recipeId, (FluidIngredient) fluidInput, itemInput, fluidOutput, itemOutput, range, pressure, recipeSpeed, airUseMultiplier, exothermic);
        }

        @Nullable
        @Override
        public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            TemperatureRange range = TemperatureRange.read(buffer);
            float pressure = buffer.readFloat();
            Ingredient input = Ingredient.fromNetwork(buffer);
            FluidIngredient fluidIn = (FluidIngredient) Ingredient.fromNetwork(buffer);
            FluidStack fluidOut = FluidStack.readFromPacket(buffer);
            ItemStack itemOutput = buffer.readItem();
            float recipeSpeed = buffer.readFloat();
            float airUseMultiplier = buffer.readFloat();
            boolean exothermic = buffer.readBoolean();
            return factory.create(recipeId, fluidIn, input, fluidOut, itemOutput, range, pressure, recipeSpeed, airUseMultiplier, exothermic);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, T recipe) {
            recipe.write(buffer);
        }

        public interface IFactory <T extends ThermoPlantRecipe> {
            T create(ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nonnull Ingredient inputItem,
                     FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure,
                     float recipeSpeed, float airUseMultiplier, boolean exothermic);
        }
    }
}
