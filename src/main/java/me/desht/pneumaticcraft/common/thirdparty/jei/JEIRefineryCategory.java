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
import me.desht.pneumaticcraft.api.crafting.TemperatureRange.TemperatureScale;
import me.desht.pneumaticcraft.api.crafting.recipe.RefineryRecipe;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIRefineryCategory extends AbstractPNCCategory<RefineryRecipe> {
    private final ITickTimer tickTimer;
    private final Map<ResourceLocation, WidgetTemperature> tempWidgets = new HashMap<>();

    JEIRefineryCategory() {
        super(ModCategoryUid.REFINERY, RefineryRecipe.class,
                xlate(ModBlocks.REFINERY.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.GUI_REFINERY, 6, 15, 166, 79),
                guiHelper().createDrawableIngredient(new ItemStack(ModBlocks.REFINERY.get()))
        );
        tickTimer = JEIPlugin.jeiHelpers.getGuiHelper().createTickTimer(60, 60, false);
    }

    @Override
    public void setIngredients(RefineryRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getInput().getFluidStacks()));
        ingredients.setOutputs(VanillaTypes.FLUID, recipe.getOutputs());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RefineryRecipe recipe, IIngredients ingredients) {
        FluidStack in = ingredients.getInputs(VanillaTypes.FLUID).get(0).get(0);

        recipeLayout.getFluidStacks().init(0, true, 2, 10, 16, 64, in.getAmount(), true, Helpers.makeTankOverlay(64));
        recipeLayout.getFluidStacks().set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));

        int n = 1;
        for (List<FluidStack> out : ingredients.getOutputs(VanillaTypes.FLUID)) {
            int h = out.get(0).getAmount() * 64 / in.getAmount();
            int yOff = 64 - h;
            recipeLayout.getFluidStacks().init(n, false, 69 + n * 20, 18 - n * 4 + yOff, 16, h, out.get(0).getAmount(), true, Helpers.makeTankOverlay(h));
            recipeLayout.getFluidStacks().set(n, out);
            n++;
        }
    }

    @Override
    public void draw(RefineryRecipe recipe, MatrixStack matrixStack, double mouseX, double mouseY) {
        WidgetTemperature w = tempWidgets.computeIfAbsent(recipe.getId(),
                id -> WidgetTemperature.fromOperatingRange(26, 18, recipe.getOperatingTemp()));
        w.setTemperature(w.getTotalRange().getMin() + (w.getTotalRange().getMax() - w.getTotalRange().getMin()) * tickTimer.getValue() / tickTimer.getMaxValue());
        w.renderButton(matrixStack, (int)mouseX, (int)mouseY, 0f);
    }

    @Override
    public List<ITextComponent> getTooltipStrings(RefineryRecipe recipe, double mouseX, double mouseY) {
        WidgetTemperature w = tempWidgets.get(recipe.getId());
        if (w != null && w.isMouseOver(mouseX, mouseY)) {
            return ImmutableList.of(HeatUtil.formatHeatString(recipe.getOperatingTemp().asString(TemperatureScale.CELSIUS)));
        }
        return Collections.emptyList();
    }
}
