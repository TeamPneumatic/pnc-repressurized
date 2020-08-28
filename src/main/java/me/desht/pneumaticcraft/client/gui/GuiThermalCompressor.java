package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerThermalCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermalCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class GuiThermalCompressor extends GuiPneumaticContainerBase<ContainerThermalCompressor,TileEntityThermalCompressor> {
    private final WidgetTemperatureSided[] tempWidgets = new WidgetTemperatureSided[4];

    public GuiThermalCompressor(ContainerThermalCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addButton(tempWidgets[0] = new WidgetTemperatureSided(Direction.SOUTH, 56).setDrawText(false));
        addButton(tempWidgets[1] = new WidgetTemperatureSided(Direction.NORTH, 66).setDrawText(false));

        addButton(tempWidgets[2] = new WidgetTemperatureSided(Direction.WEST, 89).setDrawText(true));
        addButton(tempWidgets[3] = new WidgetTemperatureSided(Direction.EAST, 99).setDrawText(false));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_THERMAL_COMPRESSOR;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + (int)(xSize * 0.82), yStart + ySize / 4 + 4);
    }

    private int getTemperatureDifferential(Direction side) {
        int temp1 = te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, side)
                .orElseThrow(RuntimeException::new).getTemperatureAsInt();
        int temp2 = te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, side.getOpposite())
                .orElseThrow(RuntimeException::new).getTemperatureAsInt();
        return Math.abs(temp1 - temp2);
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        double prod = te.airProduced(Direction.NORTH) + te.airProduced(Direction.EAST);
        if (prod > 0 && redstoneAllows) {
            pressureStatText.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tooltip.producingAir",
                    PneumaticCraftUtils.roundNumberTo(prod, 1)));
        }
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        if (getTemperatureDifferential(Direction.NORTH) < 10 && getTemperatureDifferential(Direction.EAST) < 10) {
            curInfo.add(I18n.format("pneumaticcraft.gui.tab.problems.thermal_compressor.no_temp_diff"));
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);

        int dns = getTemperatureDifferential(Direction.NORTH);
        int dew = getTemperatureDifferential(Direction.EAST);
        if ((dns < 20 && (dew >= 10 && dew < 20)) || (dew < 20 && (dns >= 10 && dns < 20))) {
            curInfo.add(I18n.format("pneumaticcraft.gui.tab.problems.thermal_compressor.poor_temp_diff"));
        }
    }

    @Override
    public void tick() {
        super.tick();

        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (Direction d : PneumaticCraftUtils.HORIZONTALS) {
            int t = te.getHeatExchanger(d).getTemperatureAsInt();
            tempWidgets[d.getHorizontalIndex()].setTemperature(t);
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
//            temp.autoScaleForTemperature();
        }
    }

    private int getRounding(int range) {
        if (range < 100) return 10;
        else if (range < 250) return 25;
        else if (range < 500) return 50;
        else return 100;
    }

    private class WidgetTemperatureSided extends WidgetTemperature {
        private final Direction side;

        WidgetTemperatureSided(Direction side, int x) {
            super(guiLeft + x, guiTop + 20, TemperatureRange.of(0, 2000), 273, 200);
            this.side = side;
        }

        @Override
        public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shift) {
            curTip.add(HeatUtil.formatHeatString(side, getTemperature()).getFormattedText());
        }

        @Override
        public void renderButton(int mouseX, int mouseY, float partialTicks) {
            super.renderButton(mouseX, mouseY, partialTicks);

            String s = side.toString().substring(0, 1).toUpperCase();
            GuiUtils.drawScaledText(font, s, x + 8, y - 4, 0x404040, 0.5f);
        }
    }
}
