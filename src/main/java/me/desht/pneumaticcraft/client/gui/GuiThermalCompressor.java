package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerThermalCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermalCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.List;

public class GuiThermalCompressor extends GuiPneumaticContainerBase<TileEntityThermalCompressor> {

    public GuiThermalCompressor(InventoryPlayer inv, TileEntityThermalCompressor te) {
        super(new ContainerThermalCompressor(inv, te), te, Textures.GUI_THERMAL_COMPRESSOR_LOCATION);
    }

    @Override
    public void initGui() {
        super.initGui();

        addWidget(new WidgetTemperatureSided(EnumFacing.NORTH, 63));
        addWidget(new WidgetTemperatureSided(EnumFacing.SOUTH, 73));

        addWidget(new WidgetTemperatureSided(EnumFacing.WEST, 88));
        addWidget(new WidgetTemperatureSided(EnumFacing.EAST, 98));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        fontRenderer.drawString("Upgr.", 28, 19, 4210752);
    }

    @Override
    protected Point getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + (int)(xSize * 0.82), yStart + ySize / 4 + 4);
    }

    private int getTemperatureDifferential(EnumFacing side) {
        return Math.abs(te.getHeatExchangerLogic(side).getTemperatureAsInt()
                - te.getHeatExchangerLogic(side.getOpposite()).getTemperatureAsInt());
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        int d = getTemperatureDifferential(EnumFacing.NORTH) + getTemperatureDifferential(EnumFacing.EAST);
        if (d == 0) {
            curInfo.add("\u00a7fNo temperature differential");
            curInfo.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Place a hot block on any side of the compressor, and a cold block on the opposite side."));
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);

        int d = getTemperatureDifferential(EnumFacing.NORTH) + getTemperatureDifferential(EnumFacing.EAST);
        if (d > 0 && d < 20) {
            curInfo.add("\u00a7fPoor temperature differential");
            curInfo.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Place a hot block on any side of the compressor, and a cold block on the opposite side."));
        }
    }

    private class WidgetTemperatureSided extends WidgetTemperature {
        private final EnumFacing side;

        WidgetTemperatureSided(EnumFacing side, int x) {
            super(side.getHorizontalIndex(), guiLeft + x, guiTop + 20, 0, 2000, ((IHeatExchanger) te).getHeatExchangerLogic(side));
            this.side = side;
        }

        @Override
        public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shift) {
            curTip.add(StringUtils.capitalize(side.getName()) + " Temperature: " + (logic.getTemperatureAsInt() - 273) + "\u00b0C");
        }
    }
}
