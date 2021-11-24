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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIHeatFrameCoolingCategory extends AbstractPNCCategory<HeatFrameCoolingRecipe> {
    private final IDrawable bonusIcon;
    private final IDrawableAnimated progressBar;

    JEIHeatFrameCoolingCategory() {
        super(ModCategoryUid.HEAT_FRAME_COOLING, HeatFrameCoolingRecipe.class,
                xlate("pneumaticcraft.gui.nei.title.heatFrameCooling"),
                guiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 0, 0, 82, 18),
                guiHelper().createDrawableIngredient(new ItemStack(ModItems.HEAT_FRAME.get()))
        );
        IDrawableStatic d = guiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 82, 0, 38, 17);
        progressBar = guiHelper().createAnimatedDrawable(d, 30, IDrawableAnimated.StartDirection.LEFT, false);
        bonusIcon = guiHelper()
                .drawableBuilder(Textures.GUI_JEI_BONUS, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    @Override
    public void draw(HeatFrameCoolingRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        progressBar.draw(matrixStack, 22, 0);
        if (recipe.getBonusMultiplier() > 0f) {
            bonusIcon.draw(matrixStack, 30, 0);
        }
    }

    @Override
    public void setIngredients(HeatFrameCoolingRecipe recipe, IIngredients ingredients) {
        ingredients.setInputIngredients(Collections.singletonList(recipe.getInput()));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, HeatFrameCoolingRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 0, 0);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        recipeLayout.getItemStacks().init(1, false, 64, 0);
        recipeLayout.getItemStacks().set(1, recipe.getOutput());
    }

    @Override
    public List<ITextComponent> getTooltipStrings(HeatFrameCoolingRecipe recipe, double mouseX, double mouseY) {
        List<ITextComponent> res = new ArrayList<>();
        if (mouseX >= 23 && mouseX <= 60) {
            res.addAll(PneumaticCraftUtils.splitStringComponent(I18n.get("pneumaticcraft.gui.nei.recipe.heatFrameCooling", recipe.getThresholdTemperature() - 273)));
            if (recipe.getBonusMultiplier() > 0f) {
                String bonus = TextFormatting.YELLOW + I18n.get("pneumaticcraft.gui.nei.recipe.heatFrameCooling.bonus", recipe.getBonusMultiplier() * 100, recipe.getOutput().getHoverName().getString(), recipe.getThresholdTemperature() - 273, recipe.getBonusLimit() + 1);
                res.addAll(PneumaticCraftUtils.splitStringComponent(bonus));
            }
        }
        return res;
    }
}
