package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidCompressor;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GuiAdvancedLiquidCompressor extends GuiLiquidCompressor {

    public GuiAdvancedLiquidCompressor(ContainerLiquidCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        addButton(new WidgetTemperature(guiLeft + 92, guiTop + 20, 273, 675, ((IHeatExchanger) te).getHeatExchangerLogic(null), 325, 625));
    }

    @Override
    protected int getFluidOffset() {
        return 72;
    }

    @Override
    public void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);
        if (te.getEfficiency() < 100) {
            curInfo.add(I18n.format("gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }
}
