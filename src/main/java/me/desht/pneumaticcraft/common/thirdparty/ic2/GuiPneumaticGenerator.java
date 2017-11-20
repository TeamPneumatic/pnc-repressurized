package me.desht.pneumaticcraft.common.thirdparty.ic2;

import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.Container4UpgradeSlots;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiPneumaticGenerator extends GuiPneumaticContainerBase<TileEntityPneumaticGenerator> {
    private GuiAnimatedStat outputStat;

    public GuiPneumaticGenerator(InventoryPlayer inventory, TileEntityPneumaticGenerator te) {
        super(new Container4UpgradeSlots(inventory, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui() {
        super.initGui();
        outputStat = addAnimatedStat("Output", IC2.glassFibreCable, 0xFF555555, false);
        addWidget(new WidgetTemperature(0, guiLeft + 87, guiTop + 20, 273, 675,
                ((IHeatExchanger) te).getHeatExchangerLogic(null), 325, 625));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 53, 19, 0xFF404040);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        outputStat.setText(getOutputStat());
    }

    private List<String> getOutputStat() {
        List<String> textList = new ArrayList<>();
        textList.add(TextFormatting.GRAY + "Output configuration");
        textList.add(TextFormatting.BLACK.toString() + te.getEnergyPacketSize() + " EU/tick");
        textList.add("\u00a77Currently producing:");
        textList.add("\u00a70" + te.curEnergyProduction + " EU/tick.");
        return textList;
    }

    @Override
    public void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (te.getEfficiency() < 100) {
            curInfo.add(I18n.format("gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
        if (te.getPressure() < PneumaticValues.MIN_PRESSURE_PNEUMATIC_GENERATOR) {
            curInfo.add(I18n.format("gui.tab.problems.notEnoughPressure"));
            curInfo.add(I18n.format("gui.tab.problems.applyPressure", PneumaticValues.MIN_PRESSURE_PNEUMATIC_GENERATOR));
        }
    }
}
