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

import me.desht.pneumaticcraft.api.crafting.recipe.ExplosionCraftingRecipe;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIExplosionCraftingCategory extends AbstractPNCCategory<ExplosionCraftingRecipe> {
    JEIExplosionCraftingCategory() {
        super(RecipeTypes.EXPLOSION_CRAFTING,
                xlate("pneumaticcraft.gui.nei.title.explosionCrafting"),
                guiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 0, 0, 82, 18),
                guiHelper()
                        .drawableBuilder(Textures.JEI_EXPLOSION, 0, 0, 16, 16)
                        .setTextureSize(16, 16)
                        .build()
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ExplosionCraftingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(recipe.getInput().ingredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 65, 1).addItemStacks(recipe.getOutputs());
    }

    @Override
    public void draw(ExplosionCraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        getIcon().draw(graphics, 30, 0);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ExplosionCraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.addAll(positionalTooltip(mouseX, mouseY, (x, y) -> x >= 23 && x <= 60,
                "pneumaticcraft.gui.nei.recipe.explosionCrafting", recipe.getLossRate()));
    }
}
