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

import me.desht.pneumaticcraft.common.core.ModRecipes;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class ShapedNoMirrorRecipeBuilder extends ShapedRecipeBuilder {
    public ShapedNoMirrorRecipeBuilder(IItemProvider resultIn, int countIn) {
        super(resultIn, countIn);
    }

    public static ShapedNoMirrorRecipeBuilder shapedRecipe(IItemProvider resultIn) {
        return shapedRecipe(resultIn, 1);
    }

    public static ShapedNoMirrorRecipeBuilder shapedRecipe(IItemProvider resultIn, int countIn) {
        return new ShapedNoMirrorRecipeBuilder(resultIn, countIn);
    }

    public void save(Consumer<IFinishedRecipe> consumerIn, ResourceLocation id) {
        Consumer<IFinishedRecipe> c = (finishedRecipe) -> consumerIn.accept(new WrappedBuilderResult(finishedRecipe, ModRecipes.CRAFTING_SHAPED_NO_MIRROR));
        super.save(c, id);
    }
}
