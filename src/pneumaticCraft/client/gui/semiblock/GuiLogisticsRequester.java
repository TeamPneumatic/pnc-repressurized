package pneumaticCraft.client.gui.semiblock;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.semiblock.SemiBlockRequester;

public class GuiLogisticsRequester extends GuiLogisticsBase{

    public GuiLogisticsRequester(InventoryPlayer invPlayer, SemiBlockRequester requester){
        super(invPlayer, requester);
    }

    @Override
    public void initGui(){
        super.initGui();
        addAnimatedStat("gui.tab.info.ghostSlotInteraction.title", new ItemStack(Blocks.hopper), 0xFF00AAFF, true).setText("gui.tab.info.ghostSlotInteraction");
    }
}
