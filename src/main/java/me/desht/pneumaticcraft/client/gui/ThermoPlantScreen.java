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

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.ThermoPlantBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.ThermoPlantBlockEntity.TPProblem;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ThermoPlantMenu;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ThermoPlantScreen extends AbstractPneumaticCraftContainerScreen<ThermoPlantMenu, ThermoPlantBlockEntity> {
    private WidgetTemperature tempWidget;
    private WidgetButtonExtended dumpButton;
    private int nExposedFaces;

    public ThermoPlantScreen(ThermoPlantMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
        imageHeight = 212;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT;
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new WidgetTank(leftPos + 13, topPos + 19, te.getInputTank()));
        addRenderableWidget(new WidgetTank(leftPos + 79, topPos + 19, te.getOutputTank()));

        tempWidget = new WidgetTemperature(leftPos + 105, topPos + 25, TemperatureRange.of(273, 673), 273, 50);
        addRenderableWidget(tempWidget);

        dumpButton = new WidgetButtonExtended(leftPos + 14, topPos + 86, 14, 14, Component.empty())
                .withTag("dump")
                .withCustomTooltip(this::makeDumpButtonTooltip);
        addRenderableWidget(dumpButton);

        nExposedFaces = HeatUtil.countExposedFaces(Collections.singletonList(te));
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (te.maxTemperature > te.minTemperature && !te.getCurrentRecipeIdSynced().isEmpty()) {
            tempWidget.setOperatingRange(TemperatureRange.of(te.minTemperature, te.maxTemperature));
        } else {
            tempWidget.setOperatingRange(null);
        }
        tempWidget.setTemperature(te.getHeatExchanger().getTemperatureAsInt());
        tempWidget.autoScaleForTemperature();

        dumpButton.setMessage(hasShiftDown() ?
                Component.literal("X").withStyle(ChatFormatting.RED) :
                Component.literal(Symbols.TRIANGLE_RIGHT).withStyle(ChatFormatting.DARK_AQUA)
        );
    }

    private List<Component> makeDumpButtonTooltip() {
        return hasShiftDown() ?
                List.of(PneumaticCraftUtils.xlate("pneumaticcraft.gui.thermopneumatic.dumpInput")) :
                List.of(PneumaticCraftUtils.xlate("pneumaticcraft.gui.thermopneumatic.moveInput"));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        super.renderBg(graphics, partialTicks, x, y);

        // animated progress bar
        double progress = te.getCraftingPercentage();
        int progressWidth = (int) (progress * 48);
        graphics.blit(getGuiTexture(), leftPos + 30, topPos + 36, imageWidth, 0, progressWidth, 30);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().scale(0.95f, 1f, 1f);
        graphics.drawString(font, title.getVisualOrderText(), imageWidth / 2f - font.width(title) / 2.1f , 5, 0x404040, false);
        graphics.pose().popPose();
        super.renderLabels(graphics, x, y);

    }

    @Override
    protected PointXY getInvNameOffset() {
        return null;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        return new PointXY(xStart + imageWidth * 3 / 4 + 14, yStart + imageHeight / 4 - 2);
    }

    @Override
    public void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);

        if (te.problem != null && te.problem != TPProblem.OK) {
            curInfo.addAll(GuiUtils.xlateAndSplit(te.problem.getTranslationKey()));
        }
    }

    @Override
    protected void addWarnings(List<Component> curInfo) {
        super.addWarnings(curInfo);

        if (nExposedFaces > 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.exposedFaces", nExposedFaces, 6));
        }
    }

    @Override
    public Collection<ItemStack> getTargetItems() {
        return getCurrentRecipe(ModRecipeTypes.THERMO_PLANT.get())
                .map(thermoPlantRecipe -> Collections.singletonList(thermoPlantRecipe.value().getOutputItem()))
                .orElse(Collections.emptyList());
    }

    @Override
    public Collection<FluidStack> getTargetFluids() {
        return getCurrentRecipe(ModRecipeTypes.THERMO_PLANT.get())
                .map(thermoPlantRecipe -> Collections.singletonList(thermoPlantRecipe.value().getOutputFluid()))
                .orElse(Collections.emptyList());
    }
}
