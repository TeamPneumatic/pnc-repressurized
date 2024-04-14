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

import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.recipe.RefineryRecipe;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.entity.processing.RefineryControllerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.processing.RefineryOutputBlockEntity;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.RefineryMenu;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RefineryControllerScreen extends AbstractPneumaticCraftContainerScreen<RefineryMenu, RefineryControllerBlockEntity> {
    private List<RefineryOutputBlockEntity> outputs;
    private WidgetTemperature widgetTemperature;
    private int nExposedFaces;

    public RefineryControllerScreen(RefineryMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageHeight = 189;
    }

    @Override
    public void init() {
        super.init();

        widgetTemperature = new WidgetTemperature(leftPos + 32, topPos + 32, TemperatureRange.of(273, 673), 273, 50);
        addRenderableWidget(widgetTemperature);

        addRenderableWidget(new WidgetTank(leftPos + 8, topPos + 25, te.getInputTank()));

        int x = leftPos + 95;
        int y = topPos + 29;

        // "te" always refers to the master refinery; the bottom block of the stack
        outputs = new ArrayList<>();
        BlockEntity te1 = te.findAdjacentOutput();
        if (te1 != null) {
            int i = 0;
            do {
                RefineryOutputBlockEntity teRO = (RefineryOutputBlockEntity) te1;
                if (outputs.size() < 4) addRenderableWidget(new WidgetTank(x, y, te.outputsSynced[i++]));
                x += 20;
                y -= 4;
                outputs.add(teRO);
                te1 = te1.getLevel().getBlockEntity(te1.getBlockPos().above());
            } while (te1 instanceof RefineryOutputBlockEntity);
        }

        if (outputs.size() < 2 || outputs.size() > 4) {
            problemTab.openStat();
        }

        nExposedFaces = HeatUtil.countExposedFaces(outputs);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (te.maxTemp > te.minTemp && !te.getCurrentRecipeIdSynced().isEmpty()) {
            widgetTemperature.setOperatingRange(TemperatureRange.of(te.minTemp, te.maxTemp));
        } else {
            widgetTemperature.setOperatingRange(null);
        }
        widgetTemperature.setTemperature(te.getHeatExchanger().getTemperatureAsInt());
        widgetTemperature.autoScaleForTemperature();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float f, int x, int y) {
        super.renderBg(graphics, f, x, y);
        if (outputs.size() < 4) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            graphics.fill(leftPos + 155, topPos + 17, leftPos + 171, topPos + 81, 0x40FF0000);
            if (outputs.size() < 3) {
                graphics.fill(leftPos + 135, topPos + 21, leftPos + 151, topPos + 85, 0x40FF0000);
            }
            if (outputs.size() < 2) {
                graphics.fill(leftPos + 115, topPos + 25, leftPos + 131, topPos + 89, 0x40FF0000);
            }
            if (outputs.size() < 1) {
                graphics.fill(leftPos + 95, topPos + 29, leftPos + 111, topPos + 93, 0x40FF0000);
            }
            RenderSystem.disableBlend();
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_REFINERY;
    }

    @Override
    public void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);

        if (te.getHeatExchanger().getTemperatureAsInt() < te.minTemp) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.notEnoughHeat"));
        }
        if (te.getInputTank().getFluidAmount() < 10) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.refinery.noOil"));
        }
        if (outputs.size() < 2) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.refinery.notEnoughRefineries"));
        } else if (outputs.size() > 4) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.refinery.tooManyRefineries"));
        }
    }

    @Override
    protected void addWarnings(List<Component> curInfo) {
        super.addWarnings(curInfo);

        if (te.isBlocked()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.refinery.outputBlocked"));
        }
        if (nExposedFaces > 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.exposedFaces", nExposedFaces, outputs.size() * 6));
        }
    }

    @Override
    protected boolean shouldAddUpgradeTab() {
        return false;
    }

    @Override
    public Collection<FluidStack> getTargetFluids() {
        return getCurrentRecipe(ModRecipeTypes.REFINERY.get())
                .map(RecipeHolder::value)
                .map(RefineryRecipe::getOutputs)
                .orElse(Collections.emptyList());
    }
}
