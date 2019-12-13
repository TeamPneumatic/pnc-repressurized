package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetEnergy;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerFluxCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluxCompressor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.ArrayList;
import java.util.List;

public class GuiFluxCompressor extends GuiPneumaticContainerBase<ContainerFluxCompressor,TileEntityFluxCompressor> {
    private WidgetAnimatedStat inputStat;

    public GuiFluxCompressor(ContainerFluxCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        inputStat = addAnimatedStat("Input", Textures.GUI_BUILDCRAFT_ENERGY, 0xFF555555, false);
        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(storage -> addButton(new WidgetEnergy(guiLeft + 20, guiTop + 20, storage)));
        addButton(new WidgetTemperature(guiLeft + 87, guiTop + 20, 273, 675, te.getHeatExchangerLogic(null), 325, 625));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        font.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_4UPGRADE_SLOTS;
    }

    @Override
    public void tick() {
        super.tick();
        inputStat.setText(getOutputStat());
    }

    private List<String> getOutputStat() {
        List<String> textList = new ArrayList<>();
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
