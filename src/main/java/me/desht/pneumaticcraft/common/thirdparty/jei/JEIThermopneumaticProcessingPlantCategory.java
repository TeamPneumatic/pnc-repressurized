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

import me.desht.pneumaticcraft.api.crafting.TemperatureRange.TemperatureScale;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIThermopneumaticProcessingPlantCategory extends AbstractPNCCategory<ThermoPlantRecipe> {
    private final ITickTimer tickTimer;
    private final Map<ResourceLocation, WidgetTemperature> tempWidgets = new HashMap<>();
    private final IDrawableAnimated progressBar;

    JEIThermopneumaticProcessingPlantCategory() {
        super(RecipeTypes.THERMO_PLANT,
                xlate(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.GUI_JEI_THERMOPNEUMATIC_PROCESSING_PLANT, 0, 0, 166, 70),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get()))
        );
        tickTimer = guiHelper().createTickTimer(60, 60, false);
        IDrawableStatic d = guiHelper().createDrawable(Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT, 176, 0, 48, 30);
        progressBar = guiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ThermoPlantRecipe recipe, IFocusGroup focuses) {
        int inH = 64, outH = 64;
        int inputAmount = recipe.getInputFluid().getAmount();
        int outputAmount = recipe.getOutputFluid().getAmount();
        if (outputAmount > 0) {
            if (inputAmount > outputAmount) {
                outH = Math.min(64, outputAmount * 64 / inputAmount);
            } else {
                inH = Math.min(64, inputAmount * 64 / outputAmount);
            }
        }

        if (inputAmount > 0) {
            builder.addSlot(RecipeIngredientRole.INPUT, 8, 3 + (64 - inH))
                    .addIngredients(ForgeTypes.FLUID_STACK, recipe.getInputFluid().getFluidStacks())
                    .setFluidRenderer(inputAmount, false, 16, inH)
                    .setOverlay(Helpers.makeTankOverlay(inH), 0, 0);
        }
        if (!recipe.getInputItem().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 33, 3)
                    .addIngredients(recipe.getInputItem());
        }
        if (outputAmount > 0) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 74, 3 + (64 - outH))
                    .addIngredient(ForgeTypes.FLUID_STACK, recipe.getOutputFluid())
                    .setFluidRenderer(outputAmount, false, 16, outH)
                    .setOverlay(Helpers.makeTankOverlay(outH), 0, 0);
        }
        if (!recipe.getOutputItem().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 51)
                    .addItemStack(recipe.getOutputItem());
        }
    }

    @Override
    public void draw(ThermoPlantRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        if (recipe.getRequiredPressure() != 0) {
            float pressure = recipe.getRequiredPressure() * ((float) tickTimer.getValue() / tickTimer.getMaxValue());
            PressureGaugeRenderer2D.drawPressureGauge(graphics, Minecraft.getInstance().font, -1,
                    PressureTier.TIER_ONE_HALF.getCriticalPressure(), PressureTier.TIER_ONE_HALF.getDangerPressure(),
                    recipe.getRequiredPressure(), pressure, 141, 42);
        }

        if (!recipe.getOperatingTemperature().isAny()) {
            WidgetTemperature w = tempWidgets.computeIfAbsent(recipe.getId(),
                    id -> WidgetTemperature.fromOperatingRange(100, 12, recipe.getOperatingTemperature()));
            w.setTemperature(w.getTotalRange().getMin() + (w.getTotalRange().getMax() - w.getTotalRange().getMin()) * tickTimer.getValue() / tickTimer.getMaxValue());
            w.render(graphics, (int) mouseX, (int) mouseY, 0f);
        }
        progressBar.draw(graphics, 25, 20);
    }

    @Override
    public List<Component> getTooltipStrings(ThermoPlantRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> res = new ArrayList<>();
        WidgetTemperature w = tempWidgets.get(recipe.getId());
        if (w != null && w.isMouseOver(mouseX, mouseY)) {
            res.add(HeatUtil.formatHeatString(recipe.getOperatingTemperature().asString(TemperatureScale.CELSIUS)));
        }
        if (recipe.getRequiredPressure() > 0 && mouseX >= 116 && mouseY >= 22 && mouseX <= 156 && mouseY <= 62) {
            res.add(xlate("pneumaticcraft.gui.tooltip.pressure", recipe.getRequiredPressure()));
            if (recipe.getAirUseMultiplier() != 1f) {
                res.add(xlate("pneumaticcraft.gui.tab.info.pneumatic_armor.usage").append(" x")
                        .append(String.format("%.1f", recipe.getAirUseMultiplier())).withStyle(ChatFormatting.GRAY));
            }
        }
        return res;
    }
}
