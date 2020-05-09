package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
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
    public GuiThermalCompressor(ContainerThermalCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetTemperatureSided(Direction.NORTH, 63));
        addButton(new WidgetTemperatureSided(Direction.SOUTH, 73));

        addButton(new WidgetTemperatureSided(Direction.WEST, 88));
        addButton(new WidgetTemperatureSided(Direction.EAST, 98));
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
            pressureStatText.add(TextFormatting.BLACK + I18n.format("gui.tooltip.producingAir",
                    PneumaticCraftUtils.roundNumberTo(prod, 1)));
        }
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        if (getTemperatureDifferential(Direction.NORTH) < 10 && getTemperatureDifferential(Direction.EAST) < 10) {
            curInfo.add(I18n.format("gui.tab.problems.thermal_compressor.no_temp_diff"));
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);

        int dns = getTemperatureDifferential(Direction.NORTH);
        int dew = getTemperatureDifferential(Direction.EAST);
        if ((dns < 20 && (dew >= 10 && dew < 20)) || (dew < 20 && (dns >= 10 && dns < 20))) {
            curInfo.add(I18n.format("gui.tab.problems.thermal_compressor.poor_temp_diff"));
        }
    }

    private class WidgetTemperatureSided extends WidgetTemperature {
        private final Direction side;

        WidgetTemperatureSided(Direction side, int x) {
            super(guiLeft + x, guiTop + 20, 0, 2000,
                    te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY, side));
            this.side = side;
        }

        @Override
        public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shift) {
            int temp = logic.map(IHeatExchangerLogic::getTemperatureAsInt).orElseThrow(RuntimeException::new);
            curTip.add(HeatUtil.formatHeatString(side, temp).getFormattedText());
        }
    }
}
