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

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;

public class JEIMemoryEssenceCategory extends AbstractPNCCategory<JEIMemoryEssenceCategory.MemoryEssenceRecipe> {
    public JEIMemoryEssenceCategory() {
        super(ModCategoryUid.MEMORY_ESSENCE, MemoryEssenceRecipe.class,
                new FluidStack(ModFluids.MEMORY_ESSENCE.get(), 1000).getDisplayName(),
                guiHelper().createDrawable(Textures.GUI_JEI_MEMORY_ESSENCE, 0, 0, 146, 73),
                guiHelper().createDrawableIngredient(new ItemStack(ModItems.MEMORY_ESSENCE_BUCKET.get()))
        );
    }

    @Override
    public void setIngredients(MemoryEssenceRecipe recipe, IIngredients ingredients) {
        if (recipe.input2.isEmpty()) {
            ingredients.setInput(VanillaTypes.ITEM, recipe.input1);
        } else {
            ingredients.setInputs(VanillaTypes.ITEM, ImmutableList.of(recipe.input1, recipe.input2));
        }
        ingredients.setOutput(VanillaTypes.FLUID, new FluidStack(ModFluids.MEMORY_ESSENCE.get(), 1000));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MemoryEssenceRecipe recipe, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 53, 28);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));

        if (!recipe.input2.isEmpty()) {
            recipeLayout.getItemStacks().init(1, true, 75, 28);
            recipeLayout.getItemStacks().set(1, ingredients.getInputs(VanillaTypes.ITEM).get(1));
        }

        recipeLayout.getFluidStacks().init(0, false, 112, 29);
        recipeLayout.getFluidStacks().set(0, new FluidStack(ModFluids.MEMORY_ESSENCE.get(), 1000));

        recipeLayout.getItemStacks().addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            String tooltipKey = recipe.getTooltipKey(slotIndex);
            if (!tooltipKey.isEmpty()) {
                tooltip.addAll(PneumaticCraftUtils.splitStringComponent(TextFormatting.GREEN + I18n.get(tooltipKey)));
            }
        });
    }

    @Override
    public void draw(MemoryEssenceRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        FontRenderer fr = Minecraft.getInstance().font;
        int ratio = XPFluidManager.getInstance().getXPRatio(ModFluids.MEMORY_ESSENCE.get());
        String s = "1 XP = " + ratio + " mB";
        int w = fr.width(s);
        Minecraft.getInstance().font.draw(matrixStack, s, (getBackground().getWidth() - w) / 2f, 0, 0x404040);
    }

    static Collection<MemoryEssenceRecipe> getAllRecipes() {
        return ImmutableList.of(
                new MemoryEssenceRecipe(ModItems.MEMORY_STICK.get(), null),
                new MemoryEssenceRecipe(ModBlocks.AERIAL_INTERFACE.get(), EnumUpgrade.DISPENSER.getItem()),
                new MemoryEssenceRecipe(ModItems.DRONE.get(), ModItems.PROGRAMMING_PUZZLE.get())
                        .setTooltipKey(1, "pneumaticcraft.gui.jei.tooltip.droneImportOrbs")
        );
    }

    static class MemoryEssenceRecipe {
        final ItemStack input1;
        final ItemStack input2;
        final String[] tooltips = new String[] {"", ""};

        public MemoryEssenceRecipe(IItemProvider input1, IItemProvider input2) {
            this.input1 = new ItemStack(input1);
            this.input2 = input2 == null ? ItemStack.EMPTY : new ItemStack(input2);
        }

        public MemoryEssenceRecipe setTooltipKey(int slot, String tooltipKey) {
            tooltips[slot] = tooltipKey;
            return this;
        }

        public String getTooltipKey(int slot) {
            return slot >= 0 && slot <= 2 ? tooltips[slot] : "";
        }
    }
}
