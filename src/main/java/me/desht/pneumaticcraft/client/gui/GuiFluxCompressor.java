package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetEnergy;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerFluxCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluxCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.ArrayList;
import java.util.List;

public class GuiFluxCompressor extends GuiPneumaticContainerBase<ContainerFluxCompressor,TileEntityFluxCompressor> {
    private WidgetAnimatedStat inputStat;
    private WidgetTemperature tempWidget;

    public GuiFluxCompressor(ContainerFluxCompressor container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        inputStat = addAnimatedStat(new StringTextComponent("Input"), Textures.GUI_BUILDCRAFT_ENERGY, 0xFF555555, false);
        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(storage -> addButton(new WidgetEnergy(guiLeft + 20, guiTop + 20, storage)));
        addButton(tempWidget = new WidgetTemperature(guiLeft + 97, guiTop + 20, TemperatureRange.of(223, 673), 273, 50)
                .setOperatingRange(TemperatureRange.of(323, 625)).setShowOperatingRange(false));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_4UPGRADE_SLOTS;
    }

    @Override
    protected PointXY getGaugeLocation() {
        return super.getGaugeLocation().add(10, 0);
    }

    @Override
    public void tick() {
        super.tick();

        inputStat.setText(getOutputStat());

        te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY).ifPresent(h -> tempWidget.setTemperature(h.getTemperatureAsInt()));
        tempWidget.autoScaleForTemperature();
    }

    private List<String> getOutputStat() {
        List<String> textList = new ArrayList<>();
        textList.add(TextFormatting.GRAY + I18n.format("pneumaticcraft.gui.tab.status.fluxCompressor.maxEnergyUsage"));
        textList.add(TextFormatting.BLACK.toString() + te.getInfoEnergyPerTick() + " FE/t");
        textList.add(TextFormatting.GRAY + I18n.format("pneumaticcraft.gui.tab.status.fluxCompressor.maxInputRate"));
        textList.add(TextFormatting.BLACK.toString() + te.getInfoEnergyPerTick() * 2 + " FE/t");
        textList.add(TextFormatting.GRAY + I18n.format("pneumaticcraft.gui.tab.status.fluxCompressor.storedEnergy"));
        textList.add(TextFormatting.BLACK.toString() + te.getInfoEnergyStored() + " FE");
        return textList;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        pressureStatText.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tooltip.maxProduction",
                PneumaticCraftUtils.roundNumberTo(te.getAirRate(), 2)));
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        if (te.getInfoEnergyPerTick() > te.getInfoEnergyStored()) {
            textList.add("pneumaticcraft.gui.tab.problems.fluxCompressor.noRF");
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);
        if (te.getHeatEfficiency() < 100) {
            curInfo.add(I18n.format("pneumaticcraft.gui.tab.problems.advancedAirCompressor.efficiency", te.getHeatEfficiency() + "%%"));
        }
    }
}
