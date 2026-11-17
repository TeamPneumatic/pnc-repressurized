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
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;

public class CompressorUpgradeRecipeBuilder extends PNCShapedRecipeBuilder {
    public CompressorUpgradeRecipeBuilder(HolderGetter<Item> items, ItemLike result, int count) {
        super(items, RecipeCategory.MISC, result, count);
    }

    public static CompressorUpgradeRecipeBuilder shapedRecipe(HolderGetter<Item> items, ItemLike result) {
        return shapedRecipe(items, result, 1);
    }

    public static CompressorUpgradeRecipeBuilder shapedRecipe(HolderGetter<Item> items, ItemLike result, int count) {
        return new CompressorUpgradeRecipeBuilder(items, result, count);
    }

    @Override
    protected ShapedRecipe makeRecipe() {
        return new CompressorUpgradeCrafting(super.makeRecipe());
    }
}
