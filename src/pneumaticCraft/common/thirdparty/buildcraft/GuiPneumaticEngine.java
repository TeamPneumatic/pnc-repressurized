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
public class GuiPneumaticEngine extends GuiPneumaticContainerBase<TileEntityPneumaticEngine>{
    private GuiAnimatedStat outputStat;

    public GuiPneumaticEngine(InventoryPlayer player, TileEntityPneumaticEngine te){
        super(new Container4UpgradeSlots(player, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui(){
        super.initGui();
        outputStat = addAnimatedStat("Output", Textures.GUI_BUILDCRAFT_ENERGY, 0xFFbd630a, false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        outputStat.setText(getOutputStat());
    }

    private List<String> getOutputStat(){
        List<String> textList = new ArrayList<String>();
        textList.add(EnumChatFormatting.GRAY + "Energy Producing:");
        textList.add(EnumChatFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(te.getCurrentMJProduction(), 1) + " MJ/tick");
        textList.add(EnumChatFormatting.GRAY + "Energy Stored:");
        textList.add(EnumChatFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(te.energy, 1) + " MJ");
        return textList;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText){
        super.addPressureStatInfo(pressureStatText);
        pressureStatText.add("\u00a77Currently Using:");
        pressureStatText.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(te.getCurrentAirUsage(), 1) + " mL/tick.");
    }
}
