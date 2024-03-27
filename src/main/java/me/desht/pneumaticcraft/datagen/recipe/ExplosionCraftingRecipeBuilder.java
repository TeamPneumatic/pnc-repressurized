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

import me.desht.pneumaticcraft.common.recipes.machine.ExplosionCraftingRecipeImpl;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class ExplosionCraftingRecipeBuilder extends AbstractPNCRecipeBuilder {
    private final Ingredient input;
    private final int lossRate;
    private final List<ItemStack> outputs;

    public ExplosionCraftingRecipeBuilder(Ingredient input, int lossRate, ItemStack... outputs) {
        this.input = input;
        this.lossRate = lossRate;
        this.outputs = List.of(outputs);
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        recipeOutput.accept(id, new ExplosionCraftingRecipeImpl(input, lossRate, outputs), null);
    }
}
