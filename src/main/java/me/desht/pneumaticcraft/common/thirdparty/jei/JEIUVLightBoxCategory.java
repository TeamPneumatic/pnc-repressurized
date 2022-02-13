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

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.common.block.entity.UVLightBoxBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.machine.UVLightBoxRecipe;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIUVLightBoxCategory extends AbstractPNCCategory<UVLightBoxRecipe> {
    private final IDrawableAnimated progressBar;

    private static final List<UVLightBoxRecipe> UV_LIGHT_BOX_RECIPES;
    static {
        ItemStack out = new ItemStack(ModItems.EMPTY_PCB.get());
        UVLightBoxBlockEntity.setExposureProgress(out, 100);
        UVLightBoxRecipe recipe = new UVLightBoxRecipe(Ingredient.of(ModItems.EMPTY_PCB.get()), out);
        UV_LIGHT_BOX_RECIPES = Collections.singletonList(recipe);
    }

    JEIUVLightBoxCategory() {
        super(ModCategoryUid.UV_LIGHT_BOX, UVLightBoxRecipe.class,
                xlate(ModBlocks.UV_LIGHT_BOX.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 0, 0, 82, 18),
                guiHelper().createDrawableIngredient(new ItemStack(ModBlocks.UV_LIGHT_BOX.get()))
        );
        IDrawableStatic d = guiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 82, 0, 38, 17);
        progressBar = guiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setIngredients(UVLightBoxRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getIn()));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOut());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, UVLightBoxRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 0);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        recipeLayout.getItemStacks().init(1, false, 64, 0);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public void draw(UVLightBoxRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
        progressBar.draw(matrixStack, 22, 0);
        getIcon().draw(matrixStack, 30, -2);
    }

    @Override
    public List<Component> getTooltipStrings(UVLightBoxRecipe recipe, double mouseX, double mouseY) {
        List<Component> res = new ArrayList<>();
        if (mouseX >= 23 && mouseX <= 60) {
            res.addAll(PneumaticCraftUtils.splitStringComponent(I18n.get("pneumaticcraft.gui.nei.recipe.uvLightBox")));
        }
        return res;
    }

    static Collection<UVLightBoxRecipe> getAllRecipes() {
        return UV_LIGHT_BOX_RECIPES;
    }
}
