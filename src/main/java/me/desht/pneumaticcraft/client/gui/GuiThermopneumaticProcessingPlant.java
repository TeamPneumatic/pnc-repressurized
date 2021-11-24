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

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant.TPProblem;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GuiThermopneumaticProcessingPlant extends
        GuiPneumaticContainerBase<ContainerThermopneumaticProcessingPlant,TileEntityThermopneumaticProcessingPlant> {

    private WidgetTemperature tempWidget;
    private WidgetButtonExtended dumpButton;
    private int nExposedFaces;

    public GuiThermopneumaticProcessingPlant(ContainerThermopneumaticProcessingPlant container, PlayerInventory inv, ITextComponent displayString) {
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

        addButton(new WidgetTank(leftPos + 13, topPos + 19, te.getInputTank()));
        addButton(new WidgetTank(leftPos + 79, topPos + 19, te.getOutputTank()));

        tempWidget = new WidgetTemperature(leftPos + 105, topPos + 25, TemperatureRange.of(273, 673), 273, 50);
        addButton(tempWidget);

        dumpButton = new WidgetButtonExtended(leftPos + 14, topPos + 86, 14, 14, StringTextComponent.EMPTY)
                .withTag("dump");
        addButton(dumpButton);

        nExposedFaces = HeatUtil.countExposedFaces(Collections.singletonList(te));
    }

    @Override
    public void tick() {
        super.tick();

        if (te.maxTemperature > te.minTemperature && !te.getCurrentRecipeIdSynced().isEmpty()) {
            tempWidget.setOperatingRange(TemperatureRange.of(te.minTemperature, te.maxTemperature));
        } else {
            tempWidget.setOperatingRange(null);
        }
        tempWidget.setTemperature(te.getHeatExchanger().getTemperatureAsInt());
        tempWidget.autoScaleForTemperature();

        if (hasShiftDown()) {
            dumpButton.setMessage(new StringTextComponent("X").withStyle(TextFormatting.RED));
            dumpButton.setTooltipKey("pneumaticcraft.gui.thermopneumatic.dumpInput");
        } else {
            dumpButton.setMessage(new StringTextComponent(Symbols.TRIANGLE_RIGHT).withStyle(TextFormatting.DARK_AQUA));
            dumpButton.setTooltipKey("pneumaticcraft.gui.thermopneumatic.moveInput");
        }
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
        super.renderBg(matrixStack, partialTicks, x, y);

        // animated progress bar
        double progress = te.getCraftingPercentage();
        int progressWidth = (int) (progress * 48);
        bindGuiTexture();
        blit(matrixStack, leftPos + 30, topPos + 36, imageWidth, 0, progressWidth, 30);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int x, int y) {
        matrixStack.pushPose();
        matrixStack.scale(0.95f, 1f, 1f);
        font.draw(matrixStack, title.getVisualOrderText(), imageWidth / 2f - font.width(title) / 2.1f , 5, 0x404040);
        matrixStack.popPose();
        super.renderLabels(matrixStack, x, y);

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
    public void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);

        if (te.problem != null && te.problem != TPProblem.OK) {
            curInfo.addAll(GuiUtils.xlateAndSplit(te.problem.getTranslationKey()));
        }
    }

    @Override
    protected void addWarnings(List<ITextComponent> curInfo) {
        super.addWarnings(curInfo);

        if (nExposedFaces > 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.exposedFaces", nExposedFaces, 6));
        }
    }

    @Override
    public Collection<ItemStack> getTargetItems() {
        return getCurrentRecipe(PneumaticCraftRecipeType.THERMO_PLANT)
                .map(thermoPlantRecipe -> Collections.singletonList(thermoPlantRecipe.getOutputItem()))
                .orElse(Collections.emptyList());
    }

    @Override
    public Collection<FluidStack> getTargetFluids() {
        return getCurrentRecipe(PneumaticCraftRecipeType.THERMO_PLANT)
                .map(thermoPlantRecipe -> Collections.singletonList(thermoPlantRecipe.getOutputFluid()))
                .orElse(Collections.emptyList());
    }
}
