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
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ThermoPlantRecipeBuilder extends PneumaticCraftRecipeBuilder<ThermoPlantRecipeBuilder> {
    private final FluidIngredient inputFluid;
    @Nullable
    private final Ingredient inputItem;
    private final FluidStack outputFluid;
    private final ItemStack outputItem;
    private final TemperatureRange operatingTemperature;
    private final float requiredPressure;
    private final float recipeSpeed;
    private final float airUseMultiplier;
    private final boolean exothermic;

    public ThermoPlantRecipeBuilder(FluidIngredient inputFluid, @Nullable Ingredient inputItem,
                                    FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure,
                                    float recipeSpeed, float airUseMultiplier, boolean exothermic) {
        super(RL(PneumaticCraftRecipeTypes.THERMO_PLANT));

        this.inputFluid = inputFluid;
        this.inputItem = inputItem;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.operatingTemperature = operatingTemperature;
        this.requiredPressure = requiredPressure;
        this.recipeSpeed = recipeSpeed;
        this.airUseMultiplier = airUseMultiplier;
        this.exothermic = exothermic;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new ThermoPlantRecipeResult(id);
    }

    public class ThermoPlantRecipeResult extends RecipeResult {
        ThermoPlantRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (inputItem != Ingredient.EMPTY) json.add("item_input", inputItem.toJson());
            if (inputFluid != FluidIngredient.EMPTY) json.add("fluid_input", inputFluid.toJson());
            if (!outputItem.isEmpty()) json.add("item_output", SerializerHelper.serializeOneItemStack(outputItem));
            if (!outputFluid.isEmpty()) json.add("fluid_output", ModCraftingHelper.fluidStackToJson(outputFluid));
            if (!operatingTemperature.isAny()) json.add("temperature", operatingTemperature.toJson());
            if (requiredPressure != 0f) json.addProperty("pressure", requiredPressure);
            if (recipeSpeed != 1.0f) json.addProperty("speed", recipeSpeed);
            if (airUseMultiplier != 1.0f) json.addProperty("air_use_multiplier", airUseMultiplier);
            json.addProperty("exothermic", exothermic);
        }
    }
}
