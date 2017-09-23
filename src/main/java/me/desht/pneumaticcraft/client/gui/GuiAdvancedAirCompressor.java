package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerAdvancedAirCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAdvancedAirCompressor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

import java.util.List;

public class GuiAdvancedAirCompressor extends GuiAirCompressor {

    public GuiAdvancedAirCompressor(InventoryPlayer player, TileEntityAdvancedAirCompressor te) {
        super(new ContainerAdvancedAirCompressor(player, te), te, Textures.GUI_ADVANCED_AIR_COMPRESSOR_LOCATION);
    }

    @Override
    public void initGui() {
        super.initGui();
        addWidget(new WidgetTemperature(0, guiLeft + 87, guiTop + 20, 273, 675, ((IHeatExchanger) te).getHeatExchangerLogic(null), 325, 625));
    }

    @Override
    protected int getFuelSlotXOffset() {
        return 69;
    }

    @Override
    public void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (te.getEfficiency() < 100) {
            curInfo.add(I18n.format("gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }
}
