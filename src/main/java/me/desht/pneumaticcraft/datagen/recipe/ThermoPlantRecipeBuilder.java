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

import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.common.recipes.machine.ThermoPlantRecipeImpl;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

public class ThermoPlantRecipeBuilder extends AbstractPNCRecipeBuilder {
    private final FluidIngredient inputFluid;
    private final Ingredient inputItem;
    private final FluidStack outputFluid;
    private final ItemStack outputItem;
    private final TemperatureRange operatingTemperature;
    private final float requiredPressure;
    private final float recipeSpeed;
    private final float airUseMultiplier;
    private final boolean exothermic;

    public ThermoPlantRecipeBuilder(FluidIngredient inputFluid, Ingredient inputItem,
                                    FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure,
                                    float recipeSpeed, float airUseMultiplier, boolean exothermic) {
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
    public void save(RecipeOutput output, ResourceLocation id) {
        output.accept(id, new ThermoPlantRecipeImpl(Optional.ofNullable(inputFluid), Optional.ofNullable(inputItem),
                outputFluid, outputItem,
                operatingTemperature, requiredPressure, recipeSpeed, airUseMultiplier, exothermic), null);
    }
}
