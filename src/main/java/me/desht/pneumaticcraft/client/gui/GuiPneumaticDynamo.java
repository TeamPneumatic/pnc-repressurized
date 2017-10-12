package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetEnergy;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerEnergy;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDynamo;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;

public class GuiPneumaticDynamo extends GuiPneumaticContainerBase<TileEntityPneumaticDynamo> {
    private GuiAnimatedStat inputStat;

    public GuiPneumaticDynamo(InventoryPlayer inventoryPlayer, TileEntityPneumaticDynamo te) {
        super(new ContainerEnergy(inventoryPlayer, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui() {
        super.initGui();
        inputStat = addAnimatedStat("Output", Textures.GUI_BUILDCRAFT_ENERGY, 0xFF555555, false);

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
        List<String> textList = new ArrayList<>();
        textList.add(TextFormatting.GRAY + "Maximum RF production:");
        textList.add(TextFormatting.BLACK.toString() + te.getRFRate() + " RF/tick");
        textList.add(TextFormatting.GRAY + "Maximum output rate:");
        textList.add(TextFormatting.BLACK.toString() + te.getRFRate() * 2 + " RF/tick");
        textList.add(TextFormatting.GRAY + "Current stored RF:");
        textList.add(TextFormatting.BLACK.toString() + te.getInfoEnergyStored() + " RF");
        return textList;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        pressureStatText.add("\u00a77Max Usage:");
        pressureStatText.add("\u00a70" + te.getAirRate() + " mL/tick.");
    }

    @Override
    public void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (te.getEfficiency() < 100) {
            curInfo.add(I18n.format("gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }
}
