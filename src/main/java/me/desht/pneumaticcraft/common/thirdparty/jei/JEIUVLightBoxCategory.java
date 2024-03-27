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

import me.desht.pneumaticcraft.common.block.entity.UVLightBoxBlockEntity;
import me.desht.pneumaticcraft.common.recipes.machine.UVLightBoxRecipe;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

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
        super(RecipeTypes.UV_LIGHT_BOX,
                xlate(ModBlocks.UV_LIGHT_BOX.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 0, 0, 82, 18),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.UV_LIGHT_BOX.get()))
        );
        IDrawableStatic d = guiHelper().createDrawable(Textures.GUI_JEI_MISC_RECIPES, 82, 0, 38, 17);
        progressBar = guiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, UVLightBoxRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(recipe.in());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 65, 1).addItemStack(recipe.out());
    }

    @Override
    public void draw(UVLightBoxRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        progressBar.draw(graphics, 22, 0);
        getIcon().draw(graphics, 30, -2);
    }

    @Override
    public List<Component> getTooltipStrings(UVLightBoxRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return positionalTooltip(mouseX, mouseY, (x, y) -> x >= 23 && x <= 60, "pneumaticcraft.gui.nei.recipe.uvLightBox");
    }

    static List<UVLightBoxRecipe> getAllRecipes() {
        return UV_LIGHT_BOX_RECIPES;
    }
}
