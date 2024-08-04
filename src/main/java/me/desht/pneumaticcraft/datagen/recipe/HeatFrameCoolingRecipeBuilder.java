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

import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidContainerIngredient;
import me.desht.pneumaticcraft.common.recipes.machine.HeatFrameCoolingRecipeImpl;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class HeatFrameCoolingRecipeBuilder extends AbstractPNCRecipeBuilder {
    private final Either<Ingredient, FluidContainerIngredient> input;
    private final int temperature;
    private final ItemStack outputItem;
    private final float bonusMultiplier;
    private final float bonusLimit;

    protected HeatFrameCoolingRecipeBuilder(Either<Ingredient, FluidContainerIngredient> input, int temperature, ItemStack output) {
        this(input, temperature, output, 0f, 0f);
    }

    public HeatFrameCoolingRecipeBuilder(Either<Ingredient, FluidContainerIngredient> input, int temperature, ItemStack output, float bonusMultiplier, float bonusLimit) {
        this.input = input;
        this.temperature = temperature;
        this.outputItem = output;
        this.bonusMultiplier = bonusMultiplier;
        this.bonusLimit = bonusLimit;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        output.accept(id, new HeatFrameCoolingRecipeImpl(input, temperature, outputItem, bonusMultiplier, bonusLimit), null);
    }
}
