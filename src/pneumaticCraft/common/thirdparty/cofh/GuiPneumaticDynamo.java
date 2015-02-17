package pneumaticCraft.common.thirdparty.cofh;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.client.gui.GuiPneumaticContainerBase;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.gui.widget.WidgetTemperature;
import pneumaticCraft.lib.Textures;

public class GuiPneumaticDynamo extends GuiPneumaticContainerBase<TileEntityPneumaticDynamo>{
    private GuiAnimatedStat inputStat;

    public GuiPneumaticDynamo(InventoryPlayer inventoryPlayer, TileEntityPneumaticDynamo te){
        super(new ContainerRF(inventoryPlayer, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui(){
        super.initGui();
        inputStat = addAnimatedStat("Output", (ItemStack)null, 0xFF555555, false);

        addWidget(new WidgetEnergy(guiLeft + 20, guiTop + 20, te));
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
        textList.add(EnumChatFormatting.GRAY + "Maximum RF production:");
        textList.add(EnumChatFormatting.BLACK.toString() + te.getRFRate() + " RF/tick");
        textList.add(EnumChatFormatting.GRAY + "Maximum output rate:");
        textList.add(EnumChatFormatting.BLACK.toString() + te.getRFRate() * 2 + " RF/tick");
        textList.add(EnumChatFormatting.GRAY + "Current stored RF:");
        textList.add(EnumChatFormatting.BLACK.toString() + te.getInfoEnergyStored() + " RF");
        return textList;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText){
        super.addPressureStatInfo(pressureStatText);
        pressureStatText.add("\u00a77Max Usage:");
        pressureStatText.add("\u00a70" + te.getAirRate() + " mL/tick.");
    }

    @Override
    public void addProblems(List<String> curInfo){
        super.addProblems(curInfo);
        if(te.getEfficiency() < 100) {
            curInfo.add(I18n.format("gui.tab.problems.advancedAirCompressor.efficiency", te.getEfficiency() + "%%"));
        }
    }
}
