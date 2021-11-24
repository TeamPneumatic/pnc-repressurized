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

package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.*;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe.AssemblyProgramType;
import me.desht.pneumaticcraft.common.recipes.machine.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public enum PneumaticRecipeRegistry implements IPneumaticRecipeRegistry {
    INSTANCE;

    public static PneumaticRecipeRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public AssemblyRecipe assemblyLaserRecipe(ResourceLocation id, @Nonnull Ingredient input, @Nonnull ItemStack output) {
        return new AssemblyRecipeImpl(id, input, output, AssemblyProgramType.LASER);
    }

    @Override
    public AssemblyRecipe assemblyDrillRecipe(ResourceLocation id, @Nonnull Ingredient input, @Nonnull ItemStack output) {
        return new AssemblyRecipeImpl(id, input, output, AssemblyProgramType.DRILL);
    }

    @Override
    public ExplosionCraftingRecipe explosionCraftingRecipe(ResourceLocation id, Ingredient input, int lossRate, ItemStack... outputs) {
        return new ExplosionCraftingRecipeImpl(id, input, lossRate, outputs);
    }

    @Override
    public HeatFrameCoolingRecipe heatFrameCoolingRecipe(ResourceLocation id, Ingredient input, int temperature, ItemStack output, float bonusMultiplier, float bonusLimit) {
        return new HeatFrameCoolingRecipeImpl(id, input, temperature, output, bonusMultiplier, bonusLimit);
    }

    @Override
    public HeatFrameCoolingRecipe heatFrameCoolingRecipe(ResourceLocation id, Ingredient input, int temperature, ItemStack output) {
        return new HeatFrameCoolingRecipeImpl(id, input, temperature, output);
    }

    @Override
    public PressureChamberRecipe pressureChamberRecipe(ResourceLocation id, List<Ingredient> inputs, float pressureRequired, ItemStack... outputs) {
        return new PressureChamberRecipeImpl(id, inputs, pressureRequired, outputs);
    }

    @Override
    public RefineryRecipe refineryRecipe(ResourceLocation id, FluidIngredient input, TemperatureRange operatingTemp, FluidStack... outputs) {
        return new RefineryRecipeImpl(id, input, operatingTemp, outputs);
    }

    @Override
    public ThermoPlantRecipe thermoPlantRecipe(ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nullable Ingredient inputItem, FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure, float speed) {
        return new ThermoPlantRecipeImpl(id, inputFluid, inputItem, outputFluid, outputItem, operatingTemperature, requiredPressure, speed, false);
    }

    @Override
    public ThermoPlantRecipe exothermicThermoPlantRecipe(ResourceLocation id, @Nonnull FluidIngredient inputFluid, @Nullable Ingredient inputItem, FluidStack outputFluid, ItemStack outputItem, TemperatureRange operatingTemperature, float requiredPressure, float speed) {
        return new ThermoPlantRecipeImpl(id, inputFluid, inputItem, outputFluid, outputItem, operatingTemperature, requiredPressure, speed, true);
    }
}
