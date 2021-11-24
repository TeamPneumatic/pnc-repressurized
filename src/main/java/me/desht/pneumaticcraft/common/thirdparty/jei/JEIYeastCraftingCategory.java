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
import net.minecraft.client.resources.I18n;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIYeastCraftingCategory extends AbstractPNCCategory<JEIYeastCraftingCategory.YeastCraftingRecipe> {
    public JEIYeastCraftingCategory() {
        super(ModCategoryUid.YEAST_CRAFTING, YeastCraftingRecipe.class,
                xlate("pneumaticcraft.gui.jei.title.yeastCrafting"),
                guiHelper().createDrawable(Textures.GUI_JEI_YEAST_CRAFTING, 0, 0, 128, 40),
                guiHelper().createDrawableIngredient(new ItemStack(ModItems.YEAST_CULTURE_BUCKET.get()))
        );
    }


    @Override
    public void setIngredients(YeastCraftingRecipe recipe, IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, recipe.itemInput);
        ingredients.setInputs(VanillaTypes.FLUID, ImmutableList.of(
                new FluidStack(ModFluids.YEAST_CULTURE.get(), 1000),
                new FluidStack(Fluids.WATER, 1000)
        ));
        ingredients.setOutput(VanillaTypes.FLUID, recipe.output);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, YeastCraftingRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 0);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        recipeLayout.getFluidStacks().init(0, true, 16, 16);
        recipeLayout.getFluidStacks().set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));

        recipeLayout.getFluidStacks().init(1, true, 32, 16);
        recipeLayout.getFluidStacks().set(1, ingredients.getInputs(VanillaTypes.FLUID).get(1));

        recipeLayout.getFluidStacks().init(2, false, 80, 16);
        recipeLayout.getFluidStacks().set(2, ingredients.getOutputs(VanillaTypes.FLUID).get(0));

        recipeLayout.getFluidStacks().init(3, false, 96, 16);
        recipeLayout.getFluidStacks().set(3, ingredients.getOutputs(VanillaTypes.FLUID).get(0));
    }

    public static Collection<?> getAllRecipes() {
        return Collections.singletonList(new YeastCraftingRecipe(
                        new ItemStack(Items.SUGAR),
                        FluidIngredient.of(1000, ModFluids.YEAST_CULTURE.get()),
                        new FluidStack(ModFluids.YEAST_CULTURE.get(), 1000)
                )
        );
    }

    @Override
    public List<ITextComponent> getTooltipStrings(YeastCraftingRecipe recipe, double mouseX, double mouseY) {
        List<ITextComponent> res = new ArrayList<>();
        if (mouseX >= 48 && mouseX <= 80) {
            res.addAll(PneumaticCraftUtils.splitStringComponent(I18n.get("pneumaticcraft.gui.jei.tooltip.yeastCrafting")));
        }
        return res;
    }

    static class YeastCraftingRecipe {
        final ItemStack itemInput;
        final FluidIngredient fluidInput;
        final FluidStack output;

        YeastCraftingRecipe(ItemStack itemInput, FluidIngredient fluidInput, FluidStack output) {
            this.itemInput = itemInput;
            this.fluidInput = fluidInput;
            this.output = output;
        }
    }
}
