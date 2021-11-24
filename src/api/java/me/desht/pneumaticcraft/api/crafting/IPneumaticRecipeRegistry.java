/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.crafting;

import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.ingredient.StackedIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Get an instance of this via {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getRecipeRegistry()}.
 * <p>
 * Note that machine recipes are now loaded from datapacks.
 *
 * @author MineMaarten, desht
 */
public interface IPneumaticRecipeRegistry {
    /**
     * Create a standard item lasering recipe.  See {@link StackedIngredient} if
     * you need a recipe taking multiples of an input item.
     *
     * @param id a unique recipe ID
     * @param input the input ingredient
     * @param output the output item
     * @return a lasering recipe
     */
    AssemblyRecipe assemblyLaserRecipe(ResourceLocation id, @Nonnull Ingredient input, @Nonnull ItemStack output);

    /**
     * Create a standard item drilling recipe.  See {@link StackedIngredient} if
     * you need a recipe taking multiples of an input item.
     *
     * @param id a unique recipe ID
     * @param input the input ingredient
     * @param output the output item
     * @return a drilling recipe
     */
    AssemblyRecipe assemblyDrillRecipe(ResourceLocation id, @Nonnull Ingredient input, @Nonnull ItemStack output);

    /**
     * Create a basic explosion crafting recipe.  This uses in-world explosions to convert nearby items on the ground
     * (in item entity form) to one or more other items.  See {@link StackedIngredient}
     * if you need a recipe taking multiples of an input item.
     *
     * @param id unique ID for the recipe
     * @param input the input ingredient
     * @param lossRate the average item loss rate, as a percentage
     * @param outputs the output items
     * @return a basic Explosion Crafting recipe
     */
    ExplosionCraftingRecipe explosionCraftingRecipe(ResourceLocation id, Ingredient input, int lossRate, ItemStack... outputs);

    /**
     * Create a standard Heat Frame cooling recipe.
     *
     * @param id unique ID for the recipe
     * @param input the input ingredient
     * @param temperature the temperature (Kelvin) below which the cooling process occurs
     * @param output the output item
     * @return a basic Heat Frame cooling recipe
     */
    HeatFrameCoolingRecipe heatFrameCoolingRecipe(ResourceLocation id, Ingredient input, int temperature, ItemStack output);

    /**
     * Create a standard Heat Frame cooling recipe with potential output multiplier.
     *
     * @param id unique ID for the recipe
     * @param input the input ingredient
     * @param temperature the temperature (Kelvin) below which the cooling process occurs
     * @param output the output item
     * @param bonusMultiplier output multiplier; chance of extra output per degree below the threshold temperature
     * @param bonusLimit hard limit on the calculated output multiplier
     * @return a basic Heat Frame cooling recipe
     */
    HeatFrameCoolingRecipe heatFrameCoolingRecipe(ResourceLocation id, Ingredient input, int temperature, ItemStack output, float bonusMultiplier, float bonusLimit);

    /**
     * Create a standard Pressure Chamber recipe. See also {@link StackedIngredient}, which may be helpful if you
     * want to add a recipe taking multiples of the same input item.
     *
     * @param id unique recipe ID
     * @param inputs a list of input ingredients
     * @param pressureRequired the pressure require (this is a minimum if positive, and a maximum if negative)
     * @param outputs the output item(s)
     * @return a pressure chamber recipe
     */
    PressureChamberRecipe pressureChamberRecipe(ResourceLocation id, List<Ingredient> inputs, float pressureRequired, ItemStack... outputs);

    /**
     * Create a standard Refinery recipe.  Note that multiple recipes with the same input fluid may exist, provided that
     * each recipe has a different number of output fluids; the Refinery will use the recipe with the largest number
     * of outputs, limited by the number of output tanks in the Refinery multiblock.
     *
     * @param id unique ID for this recipe
     * @param input the input fluid
     * @param operatingTemp a temperature range required for the recipe to craft
     * @param outputs the output fluids
     * @return a basic Refinery recipe
     */
    RefineryRecipe refineryRecipe(ResourceLocation id, FluidIngredient input, TemperatureRange operatingTemp, FluidStack... outputs);

    /**
     * Create a standard Thermopneumatic Processing Plant recipe.  Such recipes generally have a minimum temperature
     * requirement. At least one of the input fluid and input item must be non-empty.
     *
     * @param id a unique ID for this recipe
     * @param inputFluid the input fluid, may be empty
     * @param inputItem the input ingredient, may be empty
     * @param outputFluid the output fluid
     * @param outputItem the output item
     * @param operatingTemperature the operating temperature range
     * @param requiredPressure the minimum pressure required (pass 0 if no specific pressure is required)
     * @param speed recipe speed multiplier (smaller values mean recipe takes longer to process)
     * @return a Thermopneumatic Processing Plant recipe (pass {@link TemperatureRange#any()} if no specific temperature
     * is required)
     */
    ThermoPlantRecipe thermoPlantRecipe(
            ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nullable Ingredient inputItem,
            FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure, float speed);

    /**
     * Create a standard exothermic Thermopneumatic Processing Plant recipe.  Exothermic recipes produce heat rather than
     * consume it.  See {@link ThermoPlantRecipe#isExothermic()}.  At least one of the input fluid
     * and input item must be non-empty.
     *
     * @param id a unique ID for this recipe
     * @param inputFluid the input fluid, may be empty
     * @param inputItem the input ingredient, may be empty
     * @param outputFluid the output fluid
     * @param outputItem the output item
     * @param operatingTemperature the operating temperature range
     * @param requiredPressure the minimum pressure required (pass 0 if no specific pressure is required)
     * @param speed recipe speed multiplier (smaller values mean recipe takes longer to process)
     * @return a Thermopneumatic Processing Plant recipe (pass {@link TemperatureRange#any()} if no specific temperature is required)
     */
    ThermoPlantRecipe exothermicThermoPlantRecipe(
            ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nullable Ingredient inputItem,
            FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure, float speed);
}
