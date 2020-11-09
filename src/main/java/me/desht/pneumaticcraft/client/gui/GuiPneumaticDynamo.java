package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetEnergy;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticDynamo;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDynamo;
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

public class GuiPneumaticDynamo extends GuiPneumaticContainerBase<ContainerPneumaticDynamo,TileEntityPneumaticDynamo> {
    private WidgetAnimatedStat inputStat;
    private WidgetTemperature tempWidget;

    public GuiPneumaticDynamo(ContainerPneumaticDynamo container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        inputStat = addAnimatedStat(new StringTextComponent("Output"), Textures.GUI_BUILDCRAFT_ENERGY, 0xFF555555, false);

        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(storage -> addButton(new WidgetEnergy(guiLeft + 20, guiTop + 20, storage)));
        addButton(tempWidget = new WidgetTemperature(guiLeft + 97, guiTop + 20, TemperatureRange.of(273, 673), 273, 50)
                .setOperatingRange(TemperatureRange.of(323, 625)).setShowOperatingRange(false));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_4UPGRADE_SLOTS;
    }

    @Override
    public void tick() {
        super.tick();

        inputStat.setText(getOutputStat());
        te.getCapability(PNCCapabilities.HEAT_EXCHANGER_CAPABILITY).ifPresent(h -> tempWidget.setTemperature(h.getTemperatureAsInt()));
        tempWidget.autoScaleForTemperature();
    }

    private List<ITextComponent> getOutputStat() {
        List<ITextComponent> textList = new ArrayList<>();
        textList.add(xlate("pneumaticcraft.gui.tab.status.pneumaticDynamo.maxEnergyProduction").mergeStyle(TextFormatting.GRAY));
        textList.add(new StringTextComponent(te.getRFRate() + " FE/t").mergeStyle(TextFormatting.BLACK));
        textList.add(xlate("pneumaticcraft.gui.tab.status.pneumaticDynamo.maxOutputRate").mergeStyle(TextFormatting.GRAY));
        textList.add(new StringTextComponent(te.getRFRate() * 2 + " FE/t").mergeStyle(TextFormatting.BLACK));
        textList.add(xlate("pneumaticcraft.gui.tab.status.fluxCompressor.storedEnergy").mergeStyle(TextFormatting.GRAY));
        textList.add(new StringTextComponent(te.getInfoEnergyStored() + " FE").mergeStyle(TextFormatting.BLACK));
        return textList;
    }

    @Override
    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.maxUsage", te.getAirRate()).mergeStyle(TextFormatting.BLACK));
    }

    @Override
    public void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);
        if (te.getEfficiency() < 100) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }

    @Override
    protected PointXY getGaugeLocation() {
        return super.getGaugeLocation().add(10, 0);
    }
}
