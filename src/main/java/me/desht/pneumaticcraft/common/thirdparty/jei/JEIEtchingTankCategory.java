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

import me.desht.pneumaticcraft.common.block.entity.processing.UVLightBoxBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIEtchingTankCategory extends AbstractPNCCategory<JEIEtchingTankCategory.EtchingTankRecipe> {
    private final IDrawableAnimated progressBar;

    JEIEtchingTankCategory() {
        super(RecipeTypes.ETCHING_TANK,
                xlate(ModBlocks.ETCHING_TANK.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.GUI_JEI_ETCHING_TANK, 0, 0, 83, 42),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ETCHING_TANK.get()))
        );
        IDrawableStatic d = guiHelper().createDrawable(Textures.GUI_JEI_ETCHING_TANK, 83, 0, 42, 42);
        progressBar = guiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EtchingTankRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 13)
                .addIngredients(recipe.input);
        builder.addSlot(RecipeIngredientRole.INPUT, 26, 13)
                .addIngredients(NeoForgeTypes.FLUID_STACK, Collections.singletonList(new FluidStack(ModFluids.ETCHING_ACID.get(), 1000)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 66, 1)
                .addItemStack(recipe.output);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 66, 25)
                .addItemStack(recipe.failed);
    }

    @Override
    public void draw(EtchingTankRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        progressBar.draw(graphics, 20, 0);
    }

    static List<EtchingTankRecipe> getAllRecipes() {
        ItemStack[] input = new ItemStack[4];
        for (int i = 0; i < input.length; i++) {
            input[i] = new ItemStack(ModItems.EMPTY_PCB.get());
            UVLightBoxBlockEntity.setExposureProgress(input[i], 25 + 25 * i);
        }

        return Collections.singletonList(new EtchingTankRecipe(
                Ingredient.of(input),
                new ItemStack(ModItems.UNASSEMBLED_PCB.get()),
                new ItemStack(ModItems.FAILED_PCB.get()))
        );
    }

    // pseudo-recipe
    public record EtchingTankRecipe(Ingredient input, ItemStack output, ItemStack failed) {
    }
}
