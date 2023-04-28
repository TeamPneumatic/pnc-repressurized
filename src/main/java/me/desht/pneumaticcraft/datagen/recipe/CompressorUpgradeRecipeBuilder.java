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

import me.desht.pneumaticcraft.common.core.ModRecipeSerializers;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class CompressorUpgradeRecipeBuilder extends ShapedRecipeBuilder {
	public CompressorUpgradeRecipeBuilder(ItemLike pResult, int pCount) {
		super(pResult, pCount);
	}

    public static CompressorUpgradeRecipeBuilder shapedRecipe(ItemLike resultIn) {
        return shapedRecipe(resultIn, 1);
    }

    public static CompressorUpgradeRecipeBuilder shapedRecipe(ItemLike resultIn, int countIn) {
        return new CompressorUpgradeRecipeBuilder(resultIn, countIn);
    }

    public void save(Consumer<FinishedRecipe> consumerIn, ResourceLocation id) {
        Consumer<FinishedRecipe> c = (finishedRecipe) -> consumerIn.accept(new WrappedBuilderResult(finishedRecipe, ModRecipeSerializers.COMPRESSOR_UPGRADE_CRAFTING));
        super.save(c, id);
    }
}
