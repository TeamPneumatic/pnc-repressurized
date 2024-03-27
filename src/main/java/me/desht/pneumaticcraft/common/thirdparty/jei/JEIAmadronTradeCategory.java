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

import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIAmadronTradeCategory extends AbstractPNCCategory<AmadronRecipe> {
    private final IDrawable limitedIcon;

    JEIAmadronTradeCategory() {
        super(RecipeTypes.AMADRON_TRADE,
                xlate(ModItems.AMADRON_TABLET.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.WIDGET_AMADRON_OFFER, 0, 0, 73, 35),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.AMADRON_TABLET.get()))
        );

        limitedIcon = guiHelper()
                .drawableBuilder(Textures.GUI_OK_LOCATION, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AmadronRecipe recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 6, 15);
        recipe.getInput().accept(
                inputSlot::addItemStack,
                fluidStack -> inputSlot.addIngredient(NeoForgeTypes.FLUID_STACK, fluidStack)
                        .setOverlay(new FluidTextOverlay(fluidStack), 0, 0)
        );
        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 51, 15);
        recipe.getOutput().accept(
                outputSlot::addItemStack,
                fluidStack -> outputSlot.addIngredient(NeoForgeTypes.FLUID_STACK, fluidStack)
                        .setOverlay(new FluidTextOverlay(fluidStack), 0, 0)
        );
    }

    @Override
    public void draw(AmadronRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        Font fr = Minecraft.getInstance().font;
        int x = (getBackground().getWidth() - fr.width(recipe.getVendorName())) / 2;
        if (recipe.isLocationLimited()) {
            limitedIcon.draw(graphics, 60, -4);
        }
        graphics.drawString(fr, recipe.getVendorName(), x, 3, 0xFF404040, false);
    }

    @Override
    public List<Component> getTooltipStrings(AmadronRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> res = new ArrayList<>();
        if (recipe instanceof AmadronOffer offer && mouseX >= 22 && mouseX <= 51) {
            res.addAll(WidgetAmadronOffer.makeTooltip(offer, -1));
        }
        return res;
    }

    private static class FluidTextOverlay implements IDrawable {
        private final String text;

        FluidTextOverlay(FluidStack stack) {
            this.text = stack.getAmount() / 1000 + "B";
        }

        @Override
        public int getWidth() {
            return 16;
        }

        @Override
        public int getHeight() {
            return 16;
        }

        @Override
        public void draw(GuiGraphics graphics, int x, int y) {
            Font fr = Minecraft.getInstance().font;
            graphics.drawString(fr, text, x + getWidth() - fr.width(text), y + getHeight() - fr.lineHeight, 0xFFFFFFFF, false);
        }
    }
}
