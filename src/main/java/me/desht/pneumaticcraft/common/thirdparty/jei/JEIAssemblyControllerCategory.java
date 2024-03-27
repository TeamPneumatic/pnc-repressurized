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

import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.item.AssemblyProgramItem;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIAssemblyControllerCategory extends AbstractPNCCategory<AssemblyRecipe> {
    private final IDrawableAnimated progressBar;

    JEIAssemblyControllerCategory() {
        super(RecipeTypes.ASSEMBLY,
                xlate(ModBlocks.ASSEMBLY_CONTROLLER.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.GUI_JEI_ASSEMBLY_CONTROLLER, 5, 11, 158, 98),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ASSEMBLY_CONTROLLER.get()))
        );
        IDrawableStatic d = guiHelper().createDrawable(Textures.GUI_JEI_ASSEMBLY_CONTROLLER, 173, 0, 24, 17);
        progressBar = guiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AssemblyRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 29, 56)
                .addIngredients(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.CATALYST, 133, 22)
                .addItemStack(new ItemStack(AssemblyProgramItem.fromProgramType(recipe.getProgramType())));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 96, 56)
                .addItemStack(recipe.getOutput());

        int xPos = 5;
        for (AssemblyProgram.EnumMachine machine : AssemblyProgram.fromRecipe(recipe).getRequiredMachines()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, xPos, 25).addItemStack(new ItemStack(machine.getMachineBlock()));
            xPos += 18;
        }
    }

    @Override
    public void draw(AssemblyRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        progressBar.draw(graphics, 68, 65);
        Font fontRenderer = Minecraft.getInstance().font;
        graphics.drawString(fontRenderer, "Required Machines", 5, 15, 0xFF404040, false);
        graphics.drawString(fontRenderer, "Prog.", 129, 9, 0xFF404040, false);
    }
}
