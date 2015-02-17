package pneumaticCraft.common.thirdparty.ic2;

import ic2.api.item.IC2Items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.client.gui.GuiPneumaticContainerBase;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.gui.widget.WidgetTemperature;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElectricCompressor extends GuiPneumaticContainerBase<TileEntityElectricCompressor>{
    private GuiAnimatedStat inputStat;

    public GuiElectricCompressor(InventoryPlayer player, TileEntityElectricCompressor te){
        super(new ContainerElectricCompressor(player, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui(){
        super.initGui();
        inputStat = addAnimatedStat("Input", IC2Items.getItem("glassFiberCableItem"), 0xFF555555, false);
        addWidget(new WidgetTemperature(0, guiLeft + 87, guiTop + 20, 273, 675, ((IHeatExchanger)te).getHeatExchangerLogic(ForgeDirection.UNKNOWN), 325, 625));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        inputStat.setText(getOutputStat());
    }

    private List<String> getOutputStat(){
        List<String> textList = new ArrayList<String>();
        textList.add(EnumChatFormatting.GRAY + "Max power input:");
        textList.add(EnumChatFormatting.BLACK.toString() + te.getMaxSafeInput() + " EU/tick");
        return textList;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText){
        super.addPressureStatInfo(pressureStatText);
        pressureStatText.add("\u00a77Currently producing:");
        pressureStatText.add("\u00a70" + te.lastEnergyProduction + " mL/tick.");
    }

    @Override
    protected void addProblems(List<String> textList){
        super.addProblems(textList);
        if(te.outputTimer <= 0) {
            textList.add(EnumChatFormatting.GRAY + "There is no EU input!");
            textList.add(EnumChatFormatting.BLACK + "Add a (bigger) EU supply to the network.");
        }
        if(te.getEfficiency() < 100) {
            textList.add(I18n.format("gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }

}
