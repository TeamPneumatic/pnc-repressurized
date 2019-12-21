package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerThermalCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermalCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.StringUtils;

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
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        font.drawString("Upgr.", 28, 19, 4210752);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_THERMAL_COMPRESSOR_LOCATION;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + (int)(xSize * 0.82), yStart + ySize / 4 + 4);
    }

    private int getTemperatureDifferential(Direction side) {
        return Math.abs(te.getHeatExchangerLogic(side).getTemperatureAsInt()
                - te.getHeatExchangerLogic(side.getOpposite()).getTemperatureAsInt());
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        int d = getTemperatureDifferential(Direction.NORTH) + getTemperatureDifferential(Direction.EAST);
        if (d == 0) {
            curInfo.add("\u00a7fNo temperature differential");
            curInfo.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Place a hot block on any side of the compressor, and a cold block on the opposite side."));
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);

        int d = getTemperatureDifferential(Direction.NORTH) + getTemperatureDifferential(Direction.EAST);
        if (d > 0 && d < 20) {
            curInfo.add("\u00a7fPoor temperature differential");
            curInfo.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Place a hot block on any side of the compressor, and a cold block on the opposite side."));
        }
    }

    private class WidgetTemperatureSided extends WidgetTemperature {
        private final Direction side;

        WidgetTemperatureSided(Direction side, int x) {
            super(guiLeft + x, guiTop + 20, 0, 2000, ((IHeatExchanger) te).getHeatExchangerLogic(side));
            this.side = side;
        }

        @Override
        public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shift) {
            curTip.add(StringUtils.capitalize(side.getName()) + " Temperature: " + (logic.getTemperatureAsInt() - 273) + "\u00b0C");
        }
    }
}
