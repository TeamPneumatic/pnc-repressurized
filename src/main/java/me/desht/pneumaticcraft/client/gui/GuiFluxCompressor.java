package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetEnergy;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerFluxCompressor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluxCompressor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

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

        tempWidget.setTemperature(te.getHeatExchanger().getTemperatureAsInt());
        tempWidget.autoScaleForTemperature();
    }

    private List<ITextComponent> getOutputStat() {
        List<ITextComponent> textList = new ArrayList<>();
        textList.add(xlate("pneumaticcraft.gui.tab.status.fluxCompressor.maxEnergyUsage").mergeStyle(TextFormatting.GRAY));
        textList.add(new StringTextComponent(te.getInfoEnergyPerTick() + " FE/t").mergeStyle(TextFormatting.BLACK));
        textList.add(xlate("pneumaticcraft.gui.tab.status.fluxCompressor.maxInputRate").mergeStyle(TextFormatting.GRAY));
        textList.add(new StringTextComponent(te.getInfoEnergyPerTick() * 2 + " FE/t").mergeStyle(TextFormatting.BLACK));
        textList.add(xlate("pneumaticcraft.gui.tab.status.fluxCompressor.storedEnergy").mergeStyle(TextFormatting.GRAY));
        textList.add(new StringTextComponent(te.getInfoEnergyStored() + " FE").mergeStyle(TextFormatting.BLACK));
        return textList;
    }

    @Override
    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.maxProduction",
                PneumaticCraftUtils.roundNumberTo(te.getAirRate(), 2)));
    }

    @Override
    protected void addProblems(List<ITextComponent> textList) {
        super.addProblems(textList);
        if (te.getInfoEnergyPerTick() > te.getInfoEnergyStored()) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.fluxCompressor.noRF"));
        }
    }

    @Override
    protected void addWarnings(List<ITextComponent> curInfo) {
        super.addWarnings(curInfo);
        if (te.getHeatEfficiency() < 100) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.advancedAirCompressor.efficiency", te.getHeatEfficiency() + "%"));
        }
    }
}
