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

package me.desht.pneumaticcraft.common.thirdparty.jei;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIPlasticSolidifyingCategory extends AbstractPNCCategory<JEIPlasticSolidifyingCategory.PlasticSolidifyingRecipe> {
    JEIPlasticSolidifyingCategory() {
        super(RecipeTypes.PLASTIC_SOLIDIFYING,
                xlate("pneumaticcraft.gui.jei.title.plasticSolidifying"),
                guiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 0, 0, 82, 18),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.PLASTIC.get()))
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PlasticSolidifyingRecipe recipe, IFocusGroup focuses) {
        recipe.input
                .ifLeft(ingredient -> builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                        .addIngredients(ingredient))
                .ifRight(fluidIngredient -> builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                        .addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(fluidIngredient.getStacks())));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 65, 1)
                .addItemStack(recipe.output);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, PlasticSolidifyingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.addAll(positionalTooltip(mouseX, mouseY, (x, y) -> x >= 23 && x <= 60, "pneumaticcraft.gui.jei.tooltip.plasticSolidifying"));
    }

    public static List<PlasticSolidifyingRecipe> getAllRecipes() {
        return ImmutableList.of(
                new PlasticSolidifyingRecipe(
                        Either.right(FluidIngredient.of(ModFluids.PLASTIC.get())),
                        new ItemStack(ModItems.PLASTIC.get())
                ),
                new PlasticSolidifyingRecipe(
                        Either.left(Ingredient.of(new ItemStack(ModItems.PLASTIC_BUCKET.get()))),
                        new ItemStack(ModItems.PLASTIC.get())
                )
        );
    }

    public record PlasticSolidifyingRecipe(Either<Ingredient,FluidIngredient> input, ItemStack output) {
    }
}
