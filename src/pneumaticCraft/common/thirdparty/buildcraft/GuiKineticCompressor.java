package pneumaticCraft.common.thirdparty.buildcraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.client.gui.GuiPneumaticContainerBase;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiKineticCompressor extends GuiPneumaticContainerBase<TileEntityKineticCompressor>{
    private GuiAnimatedStat inputStat;

    public GuiKineticCompressor(InventoryPlayer player, TileEntityKineticCompressor te){
        super(new Container4UpgradeSlots(player, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui(){
        super.initGui();
        inputStat = addAnimatedStat("Input", Textures.GUI_BUILDCRAFT_ENERGY, 0xFFbd630a, false);
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
        textList.add(EnumChatFormatting.GRAY + "Energy used:");
        textList.add(EnumChatFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(te.energyUsed, 1) + " MJ/tick");
        textList.add(EnumChatFormatting.GRAY + "Energy stored:");
        textList.add(EnumChatFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(te.getPowerNetEnergy(), 1) + " MJ");
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
            textList.add(EnumChatFormatting.GRAY + "There is no MJ input!");
            textList.add(EnumChatFormatting.BLACK + "Add a (bigger) MJ supply to the network.");
        }
    }
}
