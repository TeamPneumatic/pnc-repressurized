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
import me.desht.pneumaticcraft.api.crafting.TemperatureRange.TemperatureScale;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JEIThermopneumaticProcessingPlantCategory extends AbstractPNCCategory<ThermoPlantRecipe> {
    private final ITickTimer tickTimer;
    private final Map<ResourceLocation, WidgetTemperature> tempWidgets = new HashMap<>();
    private final IDrawableAnimated progressBar;

    JEIThermopneumaticProcessingPlantCategory() {
        super(ModCategoryUid.THERMO_PLANT, ThermoPlantRecipe.class,
                xlate(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get().getDescriptionId()),
                guiHelper().createDrawable(Textures.GUI_JEI_THERMOPNEUMATIC_PROCESSING_PLANT, 0, 0, 166, 70),
                guiHelper().createDrawableIngredient(VanillaTypes.ITEM, new ItemStack(ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get()))
        );
        tickTimer = guiHelper().createTickTimer(60, 60, false);
        IDrawableStatic d = guiHelper().createDrawable(Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT, 176, 0, 48, 30);
        progressBar = guiHelper().createAnimatedDrawable(d, 60, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setIngredients(ThermoPlantRecipe recipe, IIngredients ingredients) {
        if (!recipe.getInputFluid().isEmpty()) {
            ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getInputFluid().getFluidStacks()));
        }
        if (!recipe.getInputItem().isEmpty()) {
            ingredients.setInputIngredients(Collections.singletonList(recipe.getInputItem()));
        }
        if (!recipe.getOutputFluid().isEmpty()) {
            ingredients.setOutput(VanillaTypes.FLUID, recipe.getOutputFluid());
        }
        if (!recipe.getOutputItem().isEmpty()) {
            ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutputItem());
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ThermoPlantRecipe recipe, IIngredients ingredients) {
        FluidStack in = ingredients.getInputs(VanillaTypes.FLUID).isEmpty() ? FluidStack.EMPTY : ingredients.getInputs(VanillaTypes.FLUID).get(0).get(0);

        int inH = 64, outH = 64;
        FluidStack out = FluidStack.EMPTY;
        if (!recipe.getOutputFluid().isEmpty()) {
            out = ingredients.getOutputs(VanillaTypes.FLUID).get(0).get(0);
            if (in.getAmount() > out.getAmount()) {
                outH = Math.min(64, out.getAmount() * 64 / in.getAmount());
            } else {
                inH = Math.min(64, in.getAmount() * 64 / out.getAmount());
            }
        }

        if (!recipe.getInputFluid().isEmpty()) {
            recipeLayout.getFluidStacks().init(0, true, 8, 3 + (64 - inH), 16, inH, in.getAmount(), false, Helpers.makeTankOverlay(inH));
            recipeLayout.getFluidStacks().set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));
        }
        if (!recipe.getInputItem().isEmpty()) {
            recipeLayout.getItemStacks().init(0, true, 32, 2);
            recipeLayout.getItemStacks().set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        }
        if (!recipe.getOutputFluid().isEmpty()) {
            recipeLayout.getFluidStacks().init(1, false, 74, 3 + (64 - outH), 16, outH, out.getAmount(), false, Helpers.makeTankOverlay(outH));
            recipeLayout.getFluidStacks().set(1, recipe.getOutputFluid());
        }
        if (!recipe.getOutputItem().isEmpty()) {
            recipeLayout.getItemStacks().init(1, false, 47, 50);
            recipeLayout.getItemStacks().set(1, recipe.getOutputItem());
        }
    }

    @Override
    public void draw(ThermoPlantRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {
        if (recipe.getRequiredPressure() != 0) {
            float pressure = recipe.getRequiredPressure() * ((float) tickTimer.getValue() / tickTimer.getMaxValue());
            PressureGaugeRenderer2D.drawPressureGauge(matrixStack, Minecraft.getInstance().font, -1,
                    PressureTier.TIER_ONE_HALF.getCriticalPressure(), PressureTier.TIER_ONE_HALF.getDangerPressure(),
                    recipe.getRequiredPressure(), pressure, 141, 42);
        }

        if (!recipe.getOperatingTemperature().isAny()) {
            WidgetTemperature w = tempWidgets.computeIfAbsent(recipe.getId(),
                    id -> WidgetTemperature.fromOperatingRange(100, 12, recipe.getOperatingTemperature()));
            w.setTemperature(w.getTotalRange().getMin() + (w.getTotalRange().getMax() - w.getTotalRange().getMin()) * tickTimer.getValue() / tickTimer.getMaxValue());
            w.render(matrixStack, (int) mouseX, (int) mouseY, 0f);
        }
        progressBar.draw(matrixStack, 25, 20);
    }

    @Override
    public List<Component> getTooltipStrings(ThermoPlantRecipe recipe, double mouseX, double mouseY) {
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
