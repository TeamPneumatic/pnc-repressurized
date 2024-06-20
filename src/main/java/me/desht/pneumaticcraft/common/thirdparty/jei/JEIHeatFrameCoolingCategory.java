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

import me.desht.pneumaticcraft.api.crafting.recipe.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIHeatFrameCoolingCategory extends AbstractPNCCategory<HeatFrameCoolingRecipe> {
    private final IDrawable bonusIcon;
    private final IDrawableAnimated progressBar;

    JEIHeatFrameCoolingCategory() {
        super(RecipeTypes.HEAT_FRAME_COOLING,
                xlate("pneumaticcraft.gui.nei.title.heatFrameCooling"),
                guiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 0, 0, 82, 18),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.HEAT_FRAME.get()))
        );
        IDrawableStatic d = guiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 82, 0, 38, 17);
        progressBar = guiHelper().createAnimatedDrawable(d, 30, IDrawableAnimated.StartDirection.LEFT, false);
        bonusIcon = guiHelper()
                .drawableBuilder(Textures.GUI_JEI_BONUS, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HeatFrameCoolingRecipe recipe, IFocusGroup focuses) {
        recipe.getInput()
                .ifLeft(ingredient -> builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                        .addIngredients(ingredient))
                .ifRight(sizedFluidIngredient -> builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                        .addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(sizedFluidIngredient.getFluids())));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 65, 1).addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(HeatFrameCoolingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        progressBar.draw(graphics, 22, 0);
        if (recipe.getBonusMultiplier() > 0f) {
            bonusIcon.draw(graphics, 30, 0);
        }
    }

    @Override
    public List<Component> getTooltipStrings(HeatFrameCoolingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> res = new ArrayList<>();
        if (mouseX >= 23 && mouseX <= 60) {
            res.addAll(PneumaticCraftUtils.splitStringComponent(I18n.get("pneumaticcraft.gui.nei.recipe.heatFrameCooling",
                    recipe.getThresholdTemperature() - 273
            )));
            if (recipe.getBonusMultiplier() > 0f) {
                String bonus = ChatFormatting.YELLOW + I18n.get("pneumaticcraft.gui.nei.recipe.heatFrameCooling.bonus",
                        recipe.getBonusMultiplier() * 100,
                        recipe.getOutput().getHoverName().getString(),
                        recipe.getThresholdTemperature() - 273,
                        recipe.getBonusLimit() + 1
                );
                res.addAll(PneumaticCraftUtils.splitStringComponent(bonus));
            }
        }
        return res;
    }
}
