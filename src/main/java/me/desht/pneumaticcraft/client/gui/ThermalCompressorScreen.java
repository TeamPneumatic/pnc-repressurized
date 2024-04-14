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
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.compressor.ThermalCompressorBlockEntity;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ThermalCompressorMenu;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ThermalCompressorScreen extends AbstractPneumaticCraftContainerScreen<ThermalCompressorMenu, ThermalCompressorBlockEntity> {
    private final WidgetTemperatureSided[] tempWidgets = new WidgetTemperatureSided[4];

    public ThermalCompressorScreen(ThermalCompressorMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        for (Direction d : DirectionUtil.HORIZONTALS) {
            addRenderableWidget(tempWidgets[d.get2DDataValue()] = new WidgetTemperatureSided(d).setDrawText(false));
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_THERMAL_COMPRESSOR;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        return new PointXY(xStart + (int) (imageWidth * 0.82), yStart + imageHeight / 4 + 4);
    }

    private int getTemperatureDifferential(Direction side) {
        IHeatExchangerLogic l1 = te.getHeatExchanger(side);
        IHeatExchangerLogic l2 = te.getHeatExchanger(side.getOpposite());
        return l1 != null && l2 != null ? Math.abs(l1.getTemperatureAsInt() - l2.getTemperatureAsInt()) : 0;
    }

    @Override
    protected void addPressureStatInfo(List<Component> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        double prod = te.airProduced(Direction.NORTH) + te.airProduced(Direction.EAST);
        if (prod > 0 && te.getRedstoneController().shouldRun()) {
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.producingAir",
                    PneumaticCraftUtils.roundNumberTo(prod, 1)).withStyle(ChatFormatting.BLACK));
        }
    }

    @Override
    protected void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);

        if (getTemperatureDifferential(Direction.NORTH) < 10 && getTemperatureDifferential(Direction.EAST) < 10) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.thermal_compressor.no_temp_diff"));
        }
    }

    @Override
    protected void addWarnings(List<Component> curInfo) {
        super.addWarnings(curInfo);

        int dns = getTemperatureDifferential(Direction.NORTH);
        int dew = getTemperatureDifferential(Direction.EAST);
        if ((dns < 20 && (dew >= 10 && dew < 20)) || (dew < 20 && (dns >= 10 && dns < 20))) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.thermal_compressor.poor_temp_diff"));
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (Direction d : DirectionUtil.HORIZONTALS) {
            int t = Objects.requireNonNull(te.getHeatExchanger(d)).getTemperatureAsInt();
            tempWidgets[d.get2DDataValue()].setTemperature(t);
            min = Math.min(min, t);
            max = Math.max(max, t);
        }
        int rounding = getRounding(max - min);
        TemperatureRange range = TemperatureRange.of(
                Math.max(0, WidgetTemperature.roundDownK(min - 1, rounding)),
                Math.min(2273, WidgetTemperature.roundUpK(max + 1, rounding))
        );
        int interval = WidgetTemperature.calcInterval(range.getMax() - range.getMin());
        for (WidgetTemperatureSided temp : tempWidgets) {
            temp.setTotalRange(range);
            temp.setTickInterval(interval);
        }
    }

    private int getRounding(int range) {
        if (range < 100) return 10;
        else if (range < 250) return 25;
        else if (range < 500) return 50;
        else return 100;
    }

    static int getWidgetX(Direction side) {
        return switch (side) {
            case SOUTH -> 56;
            case NORTH -> 66;
            case WEST -> 89;
            case EAST -> 99;
            default -> throw new IllegalArgumentException("invalid side " + side);
        };
    }

    private class WidgetTemperatureSided extends WidgetTemperature {
        private final Direction side;

        WidgetTemperatureSided(Direction side) {
            super(leftPos + getWidgetX(side), topPos + 20, TemperatureRange.of(0, 2000), 273, 200);
            this.side = side;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.renderWidget(graphics, mouseX, mouseY, partialTicks);

            String s = side.toString().substring(0, 1).toUpperCase();
            GuiUtils.drawScaledText(graphics, font, Component.literal(s), getX() + 8, getY() - 4, 0x404040, 0.5f, false);

            setTooltip(Tooltip.create(HeatUtil.formatHeatString(side, getTemperature())));
        }
    }
}
