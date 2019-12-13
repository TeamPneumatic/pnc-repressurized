package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetLogisticsMinAmounts;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockRequester;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiLogisticsRequester extends GuiLogisticsBase<SemiBlockRequester> {
    private WidgetCheckBox aeIntegration;
    private WidgetTextFieldNumber minItems;
    private WidgetTextFieldNumber minFluid;
    private int packetSendDelay = 0;

    public GuiLogisticsRequester(ContainerLogistics container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat("gui.tab.info.ghostSlotInteraction.title", new ItemStack(Blocks.HOPPER), 0xFF00AAFF, true)
                .setText("gui.tab.info.ghostSlotInteraction");

//        if (ModList.get().isLoaded(ModIds.AE2)) {
//            if (logistics.isPlacedOnInterface()) {
//                 Item item = AEApi.instance().definitions().parts().cableGlass().item(AEColor.TRANSPARENT);
//                 if (item == null) {
//                     Log.warning("AE2 cable couldn't be found!");
//                     item = ModItems.LOGISTICS_FRAME_REQUESTER;
//                 }
//                 GuiAnimatedStat stat = addAnimatedStat("gui.tab.info.logisticsRequester.aeIntegration.title",
//                         new ItemStack(item, 1, 16), 0xFF00AAFF, false);
//                 stat.setText(ImmutableList.of("", "", "gui.tab.info.logisticsRequester.aeIntegration"));
//                 stat.addSubWidget(aeIntegration = new GuiCheckBox(1, 16, 13, 0xFF000000, "gui.tab.info.logisticsRequester.aeIntegration.enable"));
//             }
//        }

        addMinOrderSizeTab();
    }

    private void addMinOrderSizeTab() {
        WidgetAnimatedStat minAmountStat = addAnimatedStat("gui.logistic_frame.min_amount", new ItemStack(Blocks.CHEST), 0xFFC0C080, false);
        minAmountStat.addPadding(7, 21);

        WidgetLabel minItemsLabel = new WidgetLabel(5, 20, I18n.format("gui.logistic_frame.min_items"));
//        minItemsLabel.setTooltipText("gui.logistic_frame.min_items.tooltip");
        minAmountStat.addSubWidget(minItemsLabel);
        minItems = new WidgetTextFieldNumber(font, 5, 30, 30, 12);
        minItems.minValue = 1;
        minItems.maxValue = 64;
        minItems.setResponder(s -> packetSendDelay = 8);
        minAmountStat.addSubWidget(minItems);

        WidgetLabel minFluidLabel = new WidgetLabel(5, 47, I18n.format("gui.logistic_frame.min_fluid"));
//        minFluidLabel.setTooltipText("gui.logistic_frame.min_fluid.tooltip");
        minAmountStat.addSubWidget(minFluidLabel);
        minFluid = new WidgetTextFieldNumber(font, 5, 57, 50, 12);
        minFluid.minValue = 1;
        minFluid.maxValue = 16000;
        minItems.setResponder(s -> packetSendDelay = 8);
        minAmountStat.addSubWidget(minFluid);
    }

    @Override
    public void tick() {
        if (firstUpdate) {
            // do this before calling superclass method
            minItems.setValue(logistics.getMinItemOrderSize());
            minFluid.setValue(logistics.getMinFluidOrderSize());
        }

        super.tick();

        if (aeIntegration != null) {
            aeIntegration.checked = logistics.isIntegrationEnabled();
        }

        if (packetSendDelay > 0 && --packetSendDelay == 0) {
            NetworkHandler.sendToServer(new PacketSetLogisticsMinAmounts(logistics, minItems.getValue(), minFluid.getValue()));
        }
    }

    @Override
    public void onClose() {
        if (packetSendDelay > 0) {
            NetworkHandler.sendToServer(new PacketSetLogisticsMinAmounts(logistics, minItems.getValue(), minFluid.getValue()));
        }
        super.onClose();
    }
}
