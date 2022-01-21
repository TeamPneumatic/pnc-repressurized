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
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIPlasticSolidifyingCategory extends AbstractPNCCategory<JEIPlasticSolidifyingCategory.PlasticSolidifyingRecipe> {
    JEIPlasticSolidifyingCategory() {
        super(ModCategoryUid.PLASTIC_SOLIDIFYING, PlasticSolidifyingRecipe.class,
                xlate("pneumaticcraft.gui.jei.title.plasticSolidifying"),
                guiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 0, 0, 82, 18),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModItems.PLASTIC.get()))
        );
    }

    @Override
    public void setIngredients(PlasticSolidifyingRecipe recipe, IIngredients ingredients) {
        if (recipe.input instanceof FluidIngredient) {
            ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(((FluidIngredient)recipe.input).getFluidStacks()));
        } else {
            ingredients.setInputIngredients(Collections.singletonList(recipe.input));
        }
        ingredients.setOutput(VanillaTypes.ITEM, recipe.output);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, PlasticSolidifyingRecipe recipe, IIngredients ingredients) {
        if (recipe.input instanceof FluidIngredient) {
            recipeLayout.getFluidStacks().init(0, true, 1, 1);
            recipeLayout.getFluidStacks().set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));
        } else {
            recipeLayout.getItemStacks().init(0, true, 0, 0);
            recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        }
        recipeLayout.getItemStacks().init(1, false, 64, 0);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public List<Component> getTooltipStrings(PlasticSolidifyingRecipe recipe, double mouseX, double mouseY) {
        List<Component> res = new ArrayList<>();
        if (mouseX >= 23 && mouseX <= 60) {
            res.addAll(PneumaticCraftUtils.splitStringComponent(I18n.get("pneumaticcraft.gui.jei.tooltip.plasticSolidifying")));
        }
        return res;
    }

    public static Collection<PlasticSolidifyingRecipe> getAllRecipes() {
        return ImmutableList.of(
                new PlasticSolidifyingRecipe(
                        FluidIngredient.of(1000, ModFluids.PLASTIC.get()),
                        new ItemStack(ModItems.PLASTIC.get())
                ),
                new PlasticSolidifyingRecipe(
                        Ingredient.of(new ItemStack(ModItems.PLASTIC_BUCKET.get())),
                        new ItemStack(ModItems.PLASTIC.get())
                )
        );
    }

    static class PlasticSolidifyingRecipe {
        final Ingredient input;
        final ItemStack output;

        PlasticSolidifyingRecipe(Ingredient input, ItemStack output) {
            this.input = input;
            this.output = output;
        }
    }
}
