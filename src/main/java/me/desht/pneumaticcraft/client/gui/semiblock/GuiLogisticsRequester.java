package me.desht.pneumaticcraft.client.gui.semiblock;

import appeng.api.AEApi;
import appeng.api.util.AEColor;
import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetLogisticsMinAmounts;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockRequester;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

public class GuiLogisticsRequester extends GuiLogisticsBase<SemiBlockRequester> {
    private GuiCheckBox aeIntegration;
    private WidgetTextFieldNumber minItems;
    private WidgetTextFieldNumber minFluid;
    private int packetSendDelay = 0;

    public GuiLogisticsRequester(InventoryPlayer invPlayer, SemiBlockRequester requester) {
        super(invPlayer, requester);
    }

    @Override
    public void initGui() {
        super.initGui();

        addAnimatedStat("gui.tab.info.ghostSlotInteraction.title", new ItemStack(Blocks.HOPPER), 0xFF00AAFF, true).setText("gui.tab.info.ghostSlotInteraction");

        if (Loader.isModLoaded(ModIds.AE2)) {
            if (logistics.isPlacedOnInterface()) {
                 Item item = AEApi.instance().definitions().parts().cableGlass().item(AEColor.TRANSPARENT);
                 if (item == null) {
                     Log.warning("AE2 cable couldn't be found!");
                     item = Itemss.LOGISTICS_FRAME_REQUESTER;
                 }
                 GuiAnimatedStat stat = addAnimatedStat("gui.tab.info.logisticsRequester.aeIntegration.title",
                         new ItemStack(item, 1, 16), 0xFF00AAFF, false);
                 stat.setText(ImmutableList.of("", "", "gui.tab.info.logisticsRequester.aeIntegration"));
                 stat.addWidget(aeIntegration = new GuiCheckBox(1, 16, 13, 0xFF000000, "gui.tab.info.logisticsRequester.aeIntegration.enable"));
             }
        }

        addMinOrderSizeTab();
    }

    private void addMinOrderSizeTab() {
        GuiAnimatedStat minAmountStat = addAnimatedStat("gui.logistic_frame.min_amount", new ItemStack(Blocks.CHEST), 0xFFC0C080, false);
        minAmountStat.addPadding(7, 21);
        WidgetLabel minItemsLabel = new WidgetLabel(5, 20, I18n.format("gui.logistic_frame.min_items"));
        minItemsLabel.setTooltipText("gui.logistic_frame.min_items.tooltip");
        minAmountStat.addWidget(minItemsLabel);
        minItems = new WidgetTextFieldNumber(fontRenderer, 5, 30, 30, 12);
        minItems.minValue = 1;
        minItems.maxValue = 64;
        minAmountStat.addWidget(minItems);
        WidgetLabel minFluidLabel = new WidgetLabel(5, 47, I18n.format("gui.logistic_frame.min_fluid"));
        minFluidLabel.setTooltipText("gui.logistic_frame.min_fluid.tooltip");
        minAmountStat.addWidget(minFluidLabel);
        minFluid = new WidgetTextFieldNumber(fontRenderer, 5, 57, 50, 12);
        minFluid.minValue = 1;
        minFluid.maxValue = 16000;
        minAmountStat.addWidget(minFluid);
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        super.actionPerformed(widget);
    }

    @Override
    public void updateScreen() {
        if (firstUpdate) {
            // do this before calling superclass method
            minItems.setValue(logistics.getMinItemOrderSize());
            minFluid.setValue(logistics.getMinFluidOrderSize());
        }

        super.updateScreen();

        if (aeIntegration != null) {
            aeIntegration.checked = logistics.isIntegrationEnabled();
        }

        if (packetSendDelay > 0 && --packetSendDelay == 0) {
            NetworkHandler.sendToServer(new PacketSetLogisticsMinAmounts(logistics, minItems.getValue(), minFluid.getValue()));
        }
    }

    @Override
    public void onKeyTyped(IGuiWidget widget) {
        if (widget == minFluid || widget == minItems) {
            // schedule a packet to be sent in 8 ticks; avoids network spam while typing
            packetSendDelay = 8;
        } else {
            super.onKeyTyped(widget);
        }
    }

    @Override
    public void onGuiClosed() {
        if (packetSendDelay > 0) {
            NetworkHandler.sendToServer(new PacketSetLogisticsMinAmounts(logistics, minItems.getValue(), minFluid.getValue()));
        }
        super.onGuiClosed();
    }
}
