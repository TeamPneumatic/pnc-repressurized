package pneumaticCraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
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
        statusStat = addAnimatedStat("Hopper Status", new ItemStack(Blockss.omnidirectionalHopper), 0xFFFFAA00, false);
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
        textList.add(EnumChatFormatting.GRAY + "Item transfer speed:");
        int itemsPer = te.getMaxItems();
        if(itemsPer > 1) {
            textList.add(EnumChatFormatting.BLACK.toString() + itemsPer + " items/tick");
        } else {
            int transferInterval = te.getItemTransferInterval();
            textList.add(EnumChatFormatting.BLACK.toString() + (transferInterval == 0 ? "20" : PneumaticCraftUtils.roundNumberTo(20F / transferInterval, 1)) + " items/s");
        }
        return textList;
    }

}
