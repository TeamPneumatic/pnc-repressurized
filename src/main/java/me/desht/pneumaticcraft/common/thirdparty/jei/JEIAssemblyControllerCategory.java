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
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIAssemblyControllerCategory extends AbstractPNCCategory<AssemblyRecipe> {
    private final IDrawableAnimated progressBar;

    JEIAssemblyControllerCategory() {
        super(ModCategoryUid.ASSEMBLY_CONTROLLER, AssemblyRecipe.class,
                xlate(ModBlocks.ASSEMBLY_CONTROLLER.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.GUI_JEI_ASSEMBLY_CONTROLLER, 5, 11, 158, 98),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.ASSEMBLY_CONTROLLER.get()))
        );
        IDrawableStatic d = guiHelper().createDrawable(Textures.GUI_JEI_ASSEMBLY_CONTROLLER, 173, 0, 24, 17);
        progressBar = guiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setIngredients(AssemblyRecipe recipe, IIngredients ingredients) {
        List<Ingredient> input = new ArrayList<>();
        input.add(recipe.getInput());
        input.add(Ingredient.of(ItemAssemblyProgram.fromProgramType(recipe.getProgramType())));
        Arrays.stream(getMachinesFromEnum(AssemblyProgram.fromRecipe(recipe).getRequiredMachines()))
                .map(Ingredient::of)
                .forEach(input::add);
        ingredients.setInputIngredients(input);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, AssemblyRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 28, 55);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        recipeLayout.getItemStacks().init(2, true, 132, 21);
        recipeLayout.getItemStacks().set(2, ingredients.getInputs(VanillaTypes.ITEM).get(1));

        int l = ingredients.getInputs(VanillaTypes.ITEM).size() - 2;  // -2 for the input item & program
        for (int i = 0; i < l; i++) {
            recipeLayout.getItemStacks().init(i + 3, true, 5 + i * 18, 25);
            recipeLayout.getItemStacks().set(i + 3, ingredients.getInputs(VanillaTypes.ITEM).get(i + 2));
        }

        recipeLayout.getItemStacks().init(1, false, 95, 55);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }

    @Override
    public void draw(AssemblyRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
        progressBar.draw(matrixStack, 68, 65);
        Font fontRenderer = Minecraft.getInstance().font;
        fontRenderer.draw(matrixStack, "Required Machines", 5, 15, 0xFF404040);
        fontRenderer.draw(matrixStack, "Prog.", 129, 9, 0xFF404040);
    }

    private ItemStack[] getMachinesFromEnum(AssemblyProgram.EnumMachine[] requiredMachines) {
        ItemStack[] machineStacks = new ItemStack[requiredMachines.length];
        for (int i = 0; i < requiredMachines.length; i++) {
            machineStacks[i] = new ItemStack(requiredMachines[i].getMachineBlock());
        }
        return machineStacks;
    }
}
