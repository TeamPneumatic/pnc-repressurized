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
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.entity.EtchingTankBlockEntity;
import me.desht.pneumaticcraft.common.inventory.EtchingTankMenu;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class EtchingTankScreen extends AbstractPneumaticCraftContainerScreen<EtchingTankMenu, EtchingTankBlockEntity> {
    private WidgetTemperature tempWidget;

    public EtchingTankScreen(EtchingTankMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageHeight = 206;
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new WidgetTank(leftPos + 149, topPos + 18, te.getAcidTank()));

        addRenderableWidget(tempWidget = new WidgetTemperature(leftPos + 134, topPos + 18,
                TemperatureRange.of(273, 773), 323, 50, this::makeTooltip)
        );
    }

    @NotNull
    private List<Component> makeTooltip() {
        int interval = te.getTickInterval();
        int processTimeSecs = interval * 5;
        List<Component> res = new ArrayList<>();
        res.add(xlate("pneumaticcraft.gui.tooltip.etching_tank.process_time", processTimeSecs).withStyle(ChatFormatting.GREEN));
        if (tempWidget.getTemperature() > 323) {
            float usage = (30 - interval) / (5f * interval);
            res.add(xlate("pneumaticcraft.gui.tooltip.etching_tank.acid_usage", PneumaticCraftUtils.roundNumberTo(usage, 2)).withStyle(ChatFormatting.YELLOW));
        }
        return res;
    }

    @Override
    public void containerTick() {
        super.containerTick();

        tempWidget.setTemperature(te.getHeatExchanger().getTemperatureAsInt());
        tempWidget.autoScaleForTemperature();
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_ETCHING_TANK;
    }

    @Override
    protected void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);

        if (te.isOutputFull()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.etching_tank.output_full"));
        }
        if (te.isFailedOutputFull()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.etching_tank.failed_full"));
        }
        if (te.getAcidTank().getFluid().isEmpty()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.etching_tank.no_acid"));
        }
    }
}
