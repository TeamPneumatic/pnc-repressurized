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
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.SolarCompressorBlockEntity;
import me.desht.pneumaticcraft.common.inventory.SolarCompressorMenu;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SolarCompressorScreen extends AbstractPneumaticCraftContainerScreen<SolarCompressorMenu, SolarCompressorBlockEntity> {
    private WidgetTemperature tempWidget;

    public SolarCompressorScreen(SolarCompressorMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(tempWidget = new WidgetTemperature(leftPos + 97, topPos + 20, TemperatureRange.of(223, ((int) te.MAX_TEMPERATURE + 25)), 273, 50)
                .setOperatingRange(TemperatureRange.of(312, ((int) te.MAX_TEMPERATURE + 25)))
                .setShowOperatingRange(false));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        if (te.canGenerateAir()) {
            return Textures.GUI_SOLAR_COMPRESSOR_ACTIVE;
        }
        else if (te.isBroken()) {
            return Textures.GUI_SOLAR_COMPRESSOR_BROKEN;
        }
        else {
            return Textures.GUI_SOLAR_COMPRESSOR_INACTIVE;
        }
    }

    @Override
    protected PointXY getGaugeLocation() {
        return super.getGaugeLocation().add(10, 0);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        tempWidget.setTemperature(te.getHeatExchanger().getTemperatureAsInt());
        tempWidget.autoScaleForTemperature();
    }

    @Override
    protected void addPressureStatInfo(List<Component> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.maxProduction",
                PneumaticCraftUtils.roundNumberTo(te.getAirRate(), 2)));
    }

    @Override
    protected void addProblems(List<Component> textList) {
        super.addProblems(textList);

        // Adds problem if the compressor is broken
        if (te.isBroken()) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.solar_compressor.broken"));
        }

        // Adds problem if the compressor is unable to see the sun
        else if (!te.getCanSeeSunlight()) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.solar_compressor.noSunlight"));
        }
    }

    @Override
    protected void addWarnings(List<Component> curInfo) {
        super.addWarnings(curInfo);

        // Only displays warnings if the compressor is actually running
        if (te.canGenerateAir()) {
            // Adds warning if the compressor is not at max efficiency
            if (te.getPercentHeatEfficiency() < 100) {
                curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.solar_compressor.efficiency", te.getPercentHeatEfficiency() + "%",  (int)(te.MAX_TEMPERATURE - 273.15)));
            }

            // Adds warning if the compressor is getting too hot
            else if (te.getTemperature() > te.MAX_TEMPERATURE - 15) {
                curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.solar_compressor.overheat", (int)(te.MAX_TEMPERATURE - 273.15)));
            }
        }
    }
}
