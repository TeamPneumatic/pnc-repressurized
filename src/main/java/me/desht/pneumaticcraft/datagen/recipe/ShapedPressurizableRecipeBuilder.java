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

import me.desht.pneumaticcraft.common.recipes.special.ShapedPressurizableRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

public class ShapedPressurizableRecipeBuilder extends PNCShapedRecipeBuilder {
    public ShapedPressurizableRecipeBuilder(ItemLike resultIn, int countIn) {
        super(RecipeCategory.MISC, resultIn, countIn);
    }

    public static ShapedPressurizableRecipeBuilder shapedRecipe(ItemLike resultIn) {
        return shapedRecipe(resultIn, 1);
    }

    public static ShapedPressurizableRecipeBuilder shapedRecipe(ItemLike resultIn, int countIn) {
        return new ShapedPressurizableRecipeBuilder(resultIn, countIn);
    }

    @Override
    protected @NotNull ShapedRecipe makeRecipe(ResourceLocation id) {
        return new ShapedPressurizableRecipe(super.makeRecipe(id));
    }
}
