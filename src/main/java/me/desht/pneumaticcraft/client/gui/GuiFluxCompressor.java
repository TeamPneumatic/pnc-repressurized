package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetEnergy;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerEnergy;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluxCompressor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;

public class GuiFluxCompressor extends GuiPneumaticContainerBase<TileEntityFluxCompressor> {
    private GuiAnimatedStat inputStat;

    public GuiFluxCompressor(Container container, TileEntityFluxCompressor te) {
        super(container, te, Textures.GUI_4UPGRADE_SLOTS);
    }

    public GuiFluxCompressor(InventoryPlayer inventoryPlayer, TileEntityFluxCompressor te) {
        super(new ContainerEnergy(inventoryPlayer, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui() {
        super.initGui();
        inputStat = addAnimatedStat("Input", Textures.GUI_BUILDCRAFT_ENERGY, 0xFF555555, false);

        IEnergyStorage storage = te.getCapability(CapabilityEnergy.ENERGY, null);
        addWidget(new WidgetEnergy(guiLeft + 20, guiTop + 20, storage));
        addWidget(new WidgetTemperature(0, guiLeft + 87, guiTop + 20, 273, 675,
                te.getHeatExchangerLogic(null), 325, 625));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        inputStat.setText(getOutputStat());
    }

    private List<String> getOutputStat() {
        List<String> textList = new ArrayList<String>();
        textList.add(TextFormatting.GRAY + "Maximum RF usage:");
        textList.add(TextFormatting.BLACK.toString() + te.getInfoEnergyPerTick() + " RF/tick");
        textList.add(TextFormatting.GRAY + "Maximum input rate:");
        textList.add(TextFormatting.BLACK.toString() + te.getInfoEnergyPerTick() * 2 + " RF/tick");
        textList.add(TextFormatting.GRAY + "Current stored RF:");
        textList.add(TextFormatting.BLACK.toString() + te.getInfoEnergyStored() + " RF");
        return textList;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        pressureStatText.add("\u00a77Max Production:");
        pressureStatText.add("\u00a70" + te.getAirRate() + " mL/tick.");
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        if (te.getInfoEnergyPerTick() > te.getInfoEnergyStored()) {
            textList.add("gui.tab.problems.fluxCompressor.noRF");
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);
        if (te.getEfficiency() < 100) {
            curInfo.add(I18n.format("gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }
}
