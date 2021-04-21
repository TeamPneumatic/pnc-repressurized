package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerThermalCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermalCompressor;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Objects;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiThermalCompressor extends GuiPneumaticContainerBase<ContainerThermalCompressor, TileEntityThermalCompressor> {
    private final WidgetTemperatureSided[] tempWidgets = new WidgetTemperatureSided[4];

    public GuiThermalCompressor(ContainerThermalCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        for (Direction d : DirectionUtil.HORIZONTALS) {
            addButton(tempWidgets[d.getHorizontalIndex()] = new WidgetTemperatureSided(d).setDrawText(false));
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_THERMAL_COMPRESSOR;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + (int) (xSize * 0.82), yStart + ySize / 4 + 4);
    }

    private int getTemperatureDifferential(Direction side) {
        IHeatExchangerLogic l1 = te.getHeatExchanger(side);
        IHeatExchangerLogic l2 = te.getHeatExchanger(side.getOpposite());
        return l1 != null && l2 != null ? Math.abs(l1.getTemperatureAsInt() - l2.getTemperatureAsInt()) : 0;
    }

    @Override
    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        double prod = te.airProduced(Direction.NORTH) + te.airProduced(Direction.EAST);
        if (prod > 0 && te.getRedstoneController().shouldRun()) {
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.producingAir",
                    PneumaticCraftUtils.roundNumberTo(prod, 1)).mergeStyle(TextFormatting.BLACK));
        }
    }

    @Override
    protected void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);

        if (getTemperatureDifferential(Direction.NORTH) < 10 && getTemperatureDifferential(Direction.EAST) < 10) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.thermal_compressor.no_temp_diff"));
        }
    }

    @Override
    protected void addWarnings(List<ITextComponent> curInfo) {
        super.addWarnings(curInfo);

        int dns = getTemperatureDifferential(Direction.NORTH);
        int dew = getTemperatureDifferential(Direction.EAST);
        if ((dns < 20 && (dew >= 10 && dew < 20)) || (dew < 20 && (dns >= 10 && dns < 20))) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.thermal_compressor.poor_temp_diff"));
        }
    }

    @Override
    public void tick() {
        super.tick();

        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (Direction d : DirectionUtil.HORIZONTALS) {
            int t = Objects.requireNonNull(te.getHeatExchanger(d)).getTemperatureAsInt();
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
        }
    }

    private int getRounding(int range) {
        if (range < 100) return 10;
        else if (range < 250) return 25;
        else if (range < 500) return 50;
        else return 100;
    }

    static int getWidgetX(Direction side) {
        switch (side) {
            case SOUTH:
                return 56;
            case NORTH:
                return 66;
            case WEST:
                return 89;
            case EAST:
                return 99;
            default:
                throw new IllegalArgumentException("invalid side " + side);
        }
    }

    private class WidgetTemperatureSided extends WidgetTemperature {
        private final Direction side;

        WidgetTemperatureSided(Direction side) {
            super(guiLeft + getWidgetX(side), guiTop + 20, TemperatureRange.of(0, 2000), 273, 200);
            this.side = side;
        }

        @Override
        public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shift) {
            curTip.add(HeatUtil.formatHeatString(side, getTemperature()));
        }

        @Override
        public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            super.renderButton(matrixStack, mouseX, mouseY, partialTicks);

            String s = side.toString().substring(0, 1).toUpperCase();
            GuiUtils.drawScaledText(matrixStack, font, s, x + 8, y - 4, 0x404040, 0.5f);
        }
    }
}
