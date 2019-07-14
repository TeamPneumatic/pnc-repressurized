package me.desht.pneumaticcraft.common.thirdparty.ic2;

import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class GuiElectricCompressor extends GuiPneumaticContainerBase<TileEntityElectricCompressor> {
    private GuiAnimatedStat inputStat;

    public GuiElectricCompressor(PlayerInventory inventory, TileEntityElectricCompressor te) {
        super(new ContainerElectricCompressor(inventory, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui() {
        super.initGui();

        inputStat = addAnimatedStat("Input", IC2.glassFibreCable, 0xFF555555, false);

        addWidget(new WidgetTemperature(0, guiLeft + 87, guiTop + 20, 273, 675,
                te.getHeatExchangerLogic(null), 325, 625));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 53, 19, 0x404040);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        inputStat.setText(getOutputStat());
    }

    private List<String> getOutputStat() {
        List<String> textList = new ArrayList<>();
        textList.add(TextFormatting.GRAY + "Max power input:");
        textList.add(TextFormatting.BLACK.toString() + te.getMaxSafeInput() + " EU/tick");
        return textList;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        pressureStatText.add("\u00a77Currently producing:");
        pressureStatText.add("\u00a70" + te.lastEnergyProduction + " mL/tick.");
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        if (te.lastEnergyProduction == 0) {
            textList.add(TextFormatting.GRAY + "There is no EU input!");
            textList.add(TextFormatting.BLACK + "Add a (bigger) EU supply to the network.");
        }
        if (te.getEfficiency() < 100) {
            textList.add(I18n.format("gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }
}
