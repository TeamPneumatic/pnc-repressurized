package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsRequester;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncSemiblock;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiLogisticsRequester extends GuiLogisticsBase<EntityLogisticsRequester> {
//    private WidgetCheckBox aeIntegration;
    private WidgetTextFieldNumber minItems;
    private WidgetTextFieldNumber minFluid;

    public GuiLogisticsRequester(ContainerLogistics container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.ghostSlotInteraction.title"), Textures.GUI_MOUSE_LOCATION, 0xFF00AAFF, true)
                .setText("pneumaticcraft.gui.tab.info.ghostSlotInteraction");

//        if (ModList.get().isLoaded(ModIds.AE2)) {
//            if (logistics.isPlacedOnInterface()) {
//                 Item item = AEApi.instance().definitions().parts().cableGlass().item(AEColor.TRANSPARENT);
//                 if (item == null) {
//                     Log.warning("AE2 cable couldn't be found!");
//                     item = ModItems.LOGISTICS_FRAME_REQUESTER;
//                 }
//                 GuiAnimatedStat stat = addAnimatedStat("pneumaticcraft.gui.tab.info.logisticsRequester.aeIntegration.title",
//                         new ItemStack(item, 1, 16), 0xFF00AAFF, false);
//                 stat.setText(ImmutableList.of("", "", "pneumaticcraft.gui.tab.info.logisticsRequester.aeIntegration"));
//                 stat.addSubWidget(aeIntegration = new GuiCheckBox(1, 16, 13, 0xFF000000, "pneumaticcraft.gui.tab.info.logisticsRequester.aeIntegration.enable"));
//             }
//        }

        addMinOrderSizeTab();
    }

    private void addMinOrderSizeTab() {
        WidgetAnimatedStat minAmountStat = addAnimatedStat(xlate("pneumaticcraft.gui.logistics_frame.min_amount"), new ItemStack(Blocks.CHEST), 0xFFC0C080, false);
        minAmountStat.addPadding(7, 21);

        WidgetLabel minItemsLabel = new WidgetLabel(5, 20, xlate("pneumaticcraft.gui.logistics_frame.min_items"));
        minItemsLabel.setTooltip(xlate("pneumaticcraft.gui.logistics_frame.min_items.tooltip"));
        minAmountStat.addSubWidget(minItemsLabel);
        minItems = new WidgetTextFieldNumber(font, 5, 30, 30, 12)
                .setRange(1, 64)
                .setValue(logistics.getMinItemOrderSize());
        minItems.setResponder(s -> sendDelayed(8));
        minAmountStat.addSubWidget(minItems);

        WidgetLabel minFluidLabel = new WidgetLabel(5, 47, xlate("pneumaticcraft.gui.logistics_frame.min_fluid"));
        minFluidLabel.setTooltip(xlate("pneumaticcraft.gui.logistics_frame.min_fluid.tooltip"));
        minAmountStat.addSubWidget(minFluidLabel);
        minFluid = new WidgetTextFieldNumber(font, 5, 57, 50, 12)
                .setRange(1, 16000)
                .setValue(logistics.getMinFluidOrderSize());
        minFluid.setResponder(s -> sendDelayed(8));
        minAmountStat.addSubWidget(minFluid);
    }

    @Override
    public void tick() {
        super.tick();
//        if (aeIntegration != null) {
//            aeIntegration.checked = logistics.isIntegrationEnabled();
//        }
    }

    @Override
    protected void doDelayedAction() {
        logistics.setMinItemOrderSize(minItems.getValue());
        logistics.setMinFluidOrderSize(minFluid.getValue());
        NetworkHandler.sendToServer(new PacketSyncSemiblock(logistics));
    }
}
