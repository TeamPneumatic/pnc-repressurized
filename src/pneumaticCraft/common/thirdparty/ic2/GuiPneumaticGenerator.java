package pneumaticCraft.common.thirdparty.ic2;

import ic2.api.item.IC2Items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.client.gui.GuiPneumaticContainerBase;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPneumaticGenerator extends GuiPneumaticContainerBase<TileEntityPneumaticGenerator>{
    private GuiAnimatedStat outputStat;

    public GuiPneumaticGenerator(InventoryPlayer player, TileEntityPneumaticGenerator te){
        super(new Container4UpgradeSlots(player, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui(){
        super.initGui();
        outputStat = addAnimatedStat("Output", IC2Items.getItem("glassFiberCableItem"), 0xFF555555, false);
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
        textList.add(EnumChatFormatting.GRAY + "Output configuration");
        textList.add(EnumChatFormatting.BLACK.toString() + te.getEnergyPacketSize() + " EU/tick");
        textList.add("\u00a77Currently producing:");
        textList.add("\u00a70" + te.curEnergyProduction + " EU/tick.");
        return textList;
    }

}
