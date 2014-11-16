package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.gui.widget.WidgetTank;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerLiquidHopper;
import pneumaticCraft.common.tileentity.TileEntityLiquidHopper;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;

public class GuiLiquidHopper extends GuiPneumaticContainerBase<TileEntityLiquidHopper>{
    private GuiAnimatedStat statusStat;

    public GuiLiquidHopper(InventoryPlayer player, TileEntityLiquidHopper te){
        super(new ContainerLiquidHopper(player, te), te, Textures.GUI_LIQUID_HOPPER);
    }

    @Override
    public void initGui(){
        super.initGui();
        addWidget(new WidgetTank(0, guiLeft + 116, guiTop + 15, te.getTank()));
        statusStat = addAnimatedStat("gui.tab.hopperStatus", new ItemStack(Blockss.omnidirectionalHopper), 0xFFFFAA00, false);
    }

    @Override
    protected Point getInvNameOffset(){
        return new Point(0, -1);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        statusStat.setText(getStatus());
    }

    private List<String> getStatus(){
        List<String> textList = new ArrayList<String>();
        int itemsPer = te.getMaxItems();
        if(itemsPer > 1) {
            textList.add(I18n.format("gui.tab.hopperStatus.liquidTransferPerTick", itemsPer * 100));
        } else {
            int transferInterval = te.getItemTransferInterval();
            textList.add(I18n.format("gui.tab.hopperStatus.liquidTransferPerSecond", transferInterval == 0 ? "2000" : PneumaticCraftUtils.roundNumberTo(2000F / transferInterval, 1)));
        }
        return textList;
    }
}
