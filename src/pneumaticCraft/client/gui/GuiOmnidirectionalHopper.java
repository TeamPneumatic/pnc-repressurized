package pneumaticCraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerOmnidirectionalHopper;
import pneumaticCraft.common.tileentity.TileEntityOmnidirectionalHopper;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOmnidirectionalHopper extends GuiPneumaticContainerBase<TileEntityOmnidirectionalHopper>{
    private GuiAnimatedStat statusStat;

    public GuiOmnidirectionalHopper(InventoryPlayer player, TileEntityOmnidirectionalHopper te){

        super(new ContainerOmnidirectionalHopper(player, te), te, Textures.GUI_OMNIDIRECTIONAL_HOPPER);
    }

    @Override
    public void initGui(){
        super.initGui();
        statusStat = addAnimatedStat("gui.tab.hopperStatus", new ItemStack(Blockss.omnidirectionalHopper), 0xFFFFAA00, false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 28, 19, 4210752);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        statusStat.setText(getStatus());
    }

    @Override
    protected String getRedstoneButtonText(int mode){
        return super.getRedstoneButtonText(mode == 0 ? 0 : 3 - mode);
    }

    private List<String> getStatus(){
        List<String> textList = new ArrayList<String>();
        int itemsPer = te.getMaxItems();
        if(itemsPer > 1) {
            textList.add(I18n.format("gui.tab.hopperStatus.itemTransferPerTick", itemsPer));
        } else {
            int transferInterval = te.getItemTransferInterval();
            textList.add(I18n.format("gui.tab.hopperStatus.itemTransferPerSecond", transferInterval == 0 ? "20" : PneumaticCraftUtils.roundNumberTo(20F / transferInterval, 1)));
        }
        return textList;
    }

}
