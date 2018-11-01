package me.desht.pneumaticcraft.client.gui.semiblock;

import appeng.api.AEApi;
import appeng.api.util.AEColor;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockRequester;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.List;

public class GuiLogisticsRequester extends GuiLogisticsBase<SemiBlockRequester> {
    private GuiCheckBox aeIntegration;

    public GuiLogisticsRequester(InventoryPlayer invPlayer, SemiBlockRequester requester) {
        super(invPlayer, requester);
    }

    @Override
    public void initGui() {
        super.initGui();
        addAnimatedStat("gui.tab.info.ghostSlotInteraction.title", new ItemStack(Blocks.HOPPER), 0xFF00AAFF, true).setText("gui.tab.info.ghostSlotInteraction");
        if (Loader.isModLoaded(ModIds.AE2)) {
            if(logistics.isPlacedOnInterface()) {
                 Item item = AEApi.instance().definitions().parts().cableGlass().item(AEColor.TRANSPARENT);
                 if(item == null) {
                     Log.warning("AE2 cable couldn't be found!");
                     item = Itemss.LOGISTICS_FRAME_REQUESTER;
                 }
                 GuiAnimatedStat stat = addAnimatedStat("gui.tab.info.logisticsRequester.aeIntegration.title", new ItemStack(item, 1, 16), 0xFF00AAFF, false);
                 List<String> text = new ArrayList<>();
                 for(int i = 0; i < 2; i++)
                     text.add("");
                 text.add("gui.tab.info.logisticsRequester.aeIntegration");
                 stat.setText(text);
                 stat.addWidget(aeIntegration = new GuiCheckBox(1, 16, 13, 0xFF000000, "gui.tab.info.logisticsRequester.aeIntegration.enable"));
             }
        }
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        super.actionPerformed(widget);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if(aeIntegration != null) aeIntegration.checked = logistics.isIntegrationEnabled();
    }
}
