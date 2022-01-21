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
import me.desht.pneumaticcraft.api.crafting.recipe.ExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIExplosionCraftingCategory extends AbstractPNCCategory<ExplosionCraftingRecipe> {
    JEIExplosionCraftingCategory() {
        super(ModCategoryUid.EXPLOSION_CRAFTING, ExplosionCraftingRecipe.class,
                xlate("pneumaticcraft.gui.nei.title.explosionCrafting"),
                guiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 0, 0, 82, 18),
                guiHelper()
                        .drawableBuilder(Textures.JEI_EXPLOSION, 0, 0, 16, 16)
                        .setTextureSize(16, 16)
                        .build()
        );
    }

    @Override
    public void setIngredients(ExplosionCraftingRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getInput()));
        ingredients.setOutputs(VanillaTypes.ITEM, recipe.getOutputs());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ExplosionCraftingRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 0);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        recipeLayout.getItemStacks().init(1, false, 64, 0);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public void draw(ExplosionCraftingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
        getIcon().draw(matrixStack, 30, 0);
    }

    @Override
    public List<Component> getTooltipStrings(ExplosionCraftingRecipe recipe, double mouseX, double mouseY) {
        List<Component> res = new ArrayList<>();
        if (mouseX >= 23 && mouseX <= 60) {
            res.addAll(PneumaticCraftUtils.splitStringComponent(I18n.get("pneumaticcraft.gui.nei.recipe.explosionCrafting", recipe.getLossRate())));
        }
        return res;
    }
}
