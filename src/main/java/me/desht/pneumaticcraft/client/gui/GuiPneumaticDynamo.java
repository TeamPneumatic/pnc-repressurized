package me.desht.pneumaticcraft.client.gui;

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

        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(storage -> addButton(new WidgetEnergy(leftPos + 20, topPos + 20, storage)));
        addButton(tempWidget = new WidgetTemperature(leftPos + 97, topPos + 20, TemperatureRange.of(273, 673), 273, 50)
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
        tempWidget.setTemperature(te.getHeatExchanger().getTemperatureAsInt());
        tempWidget.autoScaleForTemperature();
    }

    private List<ITextComponent> getOutputStat() {
        List<ITextComponent> textList = new ArrayList<>();
        textList.add(xlate("pneumaticcraft.gui.tab.status.pneumaticDynamo.maxEnergyProduction").withStyle(TextFormatting.GRAY));
        textList.add(new StringTextComponent(te.getRFRate() + " FE/t").withStyle(TextFormatting.BLACK));
        textList.add(xlate("pneumaticcraft.gui.tab.status.pneumaticDynamo.maxOutputRate").withStyle(TextFormatting.GRAY));
        textList.add(new StringTextComponent(te.getRFRate() * 2 + " FE/t").withStyle(TextFormatting.BLACK));
        textList.add(xlate("pneumaticcraft.gui.tab.status.fluxCompressor.storedEnergy").withStyle(TextFormatting.GRAY));
        textList.add(new StringTextComponent(te.getInfoEnergyStored() + " FE").withStyle(TextFormatting.BLACK));
        return textList;
    }

    @Override
    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.maxUsage", te.getAirRate()).withStyle(TextFormatting.BLACK));
    }

    @Override
    public void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);
        if (te.getEfficiency() < 100) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%"));
        }
    }

    @Override
    protected PointXY getGaugeLocation() {
        return super.getGaugeLocation().add(10, 0);
    }
}
