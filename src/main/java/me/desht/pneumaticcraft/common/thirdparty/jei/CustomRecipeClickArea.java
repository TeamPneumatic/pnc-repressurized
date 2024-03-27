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
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftContainerScreen;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Custom JEI recipe click area which also shows the current recipe outputs if any (and the current recipe ID
 * if advanced tooltips are on - F3+H)
 */
public class CustomRecipeClickArea {
    static <T extends AbstractPneumaticCraftContainerScreen<?,?>> void add(IGuiHandlerRegistration reg, Class<? extends T> guiContainerClass, int xPos, int yPos, int width, int height, RecipeType<?>... recipeTypes) {
        reg.addGuiContainerHandler(guiContainerClass, new IGuiContainerHandler<T>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(T gui, double mouseX, double mouseY) {
                return Collections.singletonList(createClickableArea(gui, xPos, yPos, width, height, recipeTypes));
            }
        });
    }

    private static <T extends AbstractPneumaticCraftContainerScreen<?,?>> IGuiClickableArea createClickableArea(T gui, int xPos, int yPos, int width, int height, RecipeType<?>... recipeTypes) {
        Rect2i area = new Rect2i(xPos, yPos, width, height);
        List<RecipeType<?>> recipeCategoryUidList = ImmutableList.copyOf(recipeTypes);
        return new IGuiClickableArea() {
            @Override
            public Rect2i getArea() {
                return area;
            }

            @Override
            public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
                recipesGui.showTypes(recipeCategoryUidList);
            }

            @Override
            public List<Component> getTooltipStrings() {
                Collection<ItemStack> items = gui.getTargetItems();
                Collection<FluidStack> fluids = gui.getTargetFluids();
                ImmutableList.Builder<Component> builder = ImmutableList.builder();
                if (!items.isEmpty() || !fluids.isEmpty()) {
                    builder.add(Component.literal("Current Recipe:").withStyle(ChatFormatting.GRAY));
                    for (ItemStack stack : items) {
                        if (!stack.isEmpty()) {
                            builder.add(Component.literal(Symbols.ARROW_RIGHT + " ").append(stack.getHoverName())
                                    .withStyle(ChatFormatting.YELLOW));
                        }
                    }
                    for (FluidStack stack : fluids) {
                        if (!stack.isEmpty()) {
                            builder.add(Component.literal(Symbols.ARROW_RIGHT + " ").append(stack.getDisplayName())
                                    .withStyle(ChatFormatting.AQUA));
                        }
                    }
                    if (Minecraft.getInstance().options.advancedItemTooltips) {
                        builder.add(Component.literal(gui.te.getCurrentRecipeIdSynced()).withStyle(ChatFormatting.DARK_GRAY));
                    }
                    builder.add(Component.empty());
                }
                builder.add(Component.translatable("jei.tooltip.show.recipes"));
                return builder.build();
            }
        };
    }
}
