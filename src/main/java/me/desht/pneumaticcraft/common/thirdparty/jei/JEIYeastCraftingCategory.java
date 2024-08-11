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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIYeastCraftingCategory extends AbstractPNCCategory<JEIYeastCraftingCategory.YeastCraftingRecipe> {
    public JEIYeastCraftingCategory() {
        super(RecipeTypes.YEAST_CRAFTING,
                xlate("pneumaticcraft.gui.jei.title.yeastCrafting"),
                guiHelper().createDrawable(Textures.GUI_JEI_YEAST_CRAFTING, 0, 0, 128, 40),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.YEAST_CULTURE_BUCKET.get()))
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, YeastCraftingRecipe recipe, IFocusGroup focuses) {
        List<FluidStack> yeastStack = List.of(recipe.fluidInput().getFluids());
        List<FluidStack> waterStack = List.of(new FluidStack(Fluids.WATER, 1000));

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addItemStack(recipe.itemInput);
        builder.addSlot(RecipeIngredientRole.CATALYST, 16, 16).addIngredients(NeoForgeTypes.FLUID_STACK, yeastStack);
        builder.addSlot(RecipeIngredientRole.INPUT, 32, 16).addIngredients(NeoForgeTypes.FLUID_STACK, waterStack);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 16).addIngredients(NeoForgeTypes.FLUID_STACK, yeastStack);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 96, 16).addIngredients(NeoForgeTypes.FLUID_STACK, yeastStack);
    }

    public static List<YeastCraftingRecipe> getAllRecipes() {
        return Collections.singletonList(new YeastCraftingRecipe(
                        new ItemStack(Items.SUGAR),
                        SizedFluidIngredient.of(ModFluids.YEAST_CULTURE.get(), 1000),
                        new FluidStack(ModFluids.YEAST_CULTURE.get(), 1000)
                )
        );
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, YeastCraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.addAll(positionalTooltip(mouseX, mouseY, (x, y) -> x >= 48 && x <= 80, "pneumaticcraft.gui.jei.tooltip.yeastCrafting"));
    }

    public record YeastCraftingRecipe(ItemStack itemInput, SizedFluidIngredient fluidInput, FluidStack output) {
    }
}
