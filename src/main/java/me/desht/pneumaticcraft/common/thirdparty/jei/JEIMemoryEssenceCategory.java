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
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

public class JEIMemoryEssenceCategory extends AbstractPNCCategory<JEIMemoryEssenceCategory.MemoryEssenceRecipe> {
    public JEIMemoryEssenceCategory() {
        super(RecipeTypes.MEMORY_ESSENCE,
                new FluidStack(ModFluids.MEMORY_ESSENCE.get(), 1000).getHoverName(),
                guiHelper().createDrawable(Textures.GUI_JEI_MEMORY_ESSENCE, 0, 0, 146, 73),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.MEMORY_ESSENCE_BUCKET.get()))
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MemoryEssenceRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 54, 29)
                .addItemStack(recipe.input1)
                .addTooltipCallback(new Tooltip(recipe, 0));
        if (!recipe.input2.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 76, 29)
                    .addItemStack(recipe.input2)
                    .addTooltipCallback(new Tooltip(recipe, 1));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 29)
                .addIngredients(NeoForgeTypes.FLUID_STACK, Collections.singletonList(new FluidStack(ModFluids.MEMORY_ESSENCE.get(), 1000)));
    }

    @Override
    public void draw(MemoryEssenceRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        Font fr = Minecraft.getInstance().font;
        int ratio = XPFluidManager.getInstance().getXPRatio(ModFluids.MEMORY_ESSENCE.get());
        String s = "1 XP = " + ratio + " mB";
        int w = fr.width(s);
        graphics.drawString(fr, s, (getBackground().getWidth() - w) / 2, 0, 0x404040, false);
    }

    private record Tooltip(MemoryEssenceRecipe recipe, int slot) implements IRecipeSlotTooltipCallback {
        @SuppressWarnings("removal")
        @Override
        public void onTooltip(IRecipeSlotView recipeSlotView, List<Component> tooltip) {
        }

        @Override
        public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
            String tooltipKey = recipe.getTooltipKey(slot);
            if (!tooltipKey.isEmpty()) {
                tooltip.addAll(PneumaticCraftUtils.splitStringComponent(ChatFormatting.GREEN + I18n.get(tooltipKey)));
            }
        }
    }

    static List<MemoryEssenceRecipe> getAllRecipes() {
        return ImmutableList.of(
                new MemoryEssenceRecipe(ModItems.MEMORY_STICK.get(), null),
                new MemoryEssenceRecipe(ModBlocks.AERIAL_INTERFACE.get(), ModUpgrades.DISPENSER.get().getItem()),
                new MemoryEssenceRecipe(ModItems.DRONE.get(), ModItems.PROGRAMMING_PUZZLE.get())
                        .setTooltipKey(1, "pneumaticcraft.gui.jei.tooltip.droneImportOrbs")
        );
    }

    public static class MemoryEssenceRecipe {
        final ItemStack input1;
        final ItemStack input2;
        final String[] tooltips = new String[] {"", ""};

        public MemoryEssenceRecipe(ItemLike input1, ItemLike input2) {
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
