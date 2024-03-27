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

import me.desht.pneumaticcraft.common.recipes.special.CompressorUpgradeCrafting;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

public class CompressorUpgradeRecipeBuilder extends PNCShapedRecipeBuilder {
	public CompressorUpgradeRecipeBuilder(ItemLike pResult, int pCount) {
		super(RecipeCategory.MISC, pResult, pCount);
	}

    public static CompressorUpgradeRecipeBuilder shapedRecipe(ItemLike resultIn) {
        return shapedRecipe(resultIn, 1);
    }

    public static CompressorUpgradeRecipeBuilder shapedRecipe(ItemLike resultIn, int countIn) {
        return new CompressorUpgradeRecipeBuilder(resultIn, countIn);
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        super.save(output, id);
    }

    @Override
    protected @NotNull ShapedRecipe makeRecipe(ResourceLocation id) {
        return new CompressorUpgradeCrafting(super.makeRecipe(id));
    }
}
