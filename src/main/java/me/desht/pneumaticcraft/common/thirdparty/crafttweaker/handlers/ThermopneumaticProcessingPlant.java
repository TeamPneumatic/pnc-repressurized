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

package me.desht.pneumaticcraft.common.thirdparty.crafttweaker.handlers;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import me.desht.pneumaticcraft.common.recipes.machine.ThermoPlantRecipeImpl;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.thirdparty.crafttweaker.CTUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

import java.util.Optional;

@Document("mods/PneumaticCraft/ThermopneumaticProcessingPlant")
@ZenCodeType.Name("mods.pneumaticcraft.thermopneumaticprocessingplant")
@ZenRegister
public class ThermopneumaticProcessingPlant implements IRecipeManager<ThermoPlantRecipe> {
    @ZenCodeType.Method
    public void addRecipe(String name, CTFluidIngredient inputFluid, IIngredient inputItem, IFluidStack outputFluid, IItemStack outputItem,
                          float pressure, int minTemp, @ZenCodeType.OptionalInt(Integer.MAX_VALUE) int maxTemp,
                          @ZenCodeType.OptionalFloat(1f) float recipeSpeed, @ZenCodeType.OptionalFloat(1f) float airUseMultiplier,
                          @ZenCodeType.OptionalBoolean() boolean exothermic) {
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this,
                new RecipeHolder<>(new ResourceLocation("crafttweaker", fixRecipeName(name)),
                new ThermoPlantRecipeImpl(
                        Optional.ofNullable(CTUtils.toFluidIngredient(inputFluid)),
                        Optional.ofNullable(inputItem.asVanillaIngredient()),
                        outputFluid.getImmutableInternal(),
                        outputItem.getImmutableInternal(),
                        TemperatureRange.of(minTemp, maxTemp),
                        pressure,
                        recipeSpeed,
                        airUseMultiplier,
                        exothermic)
                )
        ));
    }

    @Override
    public RecipeType<ThermoPlantRecipe> getRecipeType() {
        return ModRecipeTypes.THERMO_PLANT.get();
    }
}
