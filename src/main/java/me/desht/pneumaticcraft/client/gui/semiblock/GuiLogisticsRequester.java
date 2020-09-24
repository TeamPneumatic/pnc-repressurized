package me.desht.pneumaticcraft.client.gui.semiblock;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsRequester;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.thirdparty.ae2.AE2Integration;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiLogisticsRequester extends GuiLogisticsBase<EntityLogisticsRequester> {
    private WidgetCheckBox aeIntegration;

    public GuiLogisticsRequester(ContainerLogistics container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.ghostSlotInteraction.title"), Textures.GUI_MOUSE_LOCATION, 0xFF00AAFF, true)
                .setText("pneumaticcraft.gui.tab.info.ghostSlotInteraction");

        if (AE2Integration.isAvailable() && logistics.getAE2integration().isPlacedOnInterface()) {
            addAE2Tab();
        }
    }


    private void addAE2Tab() {
        Item item = AE2Integration.glassCable();
        if (item == null) {
            Log.warning("AE2 cable couldn't be found!");
            item = ModItems.LOGISTICS_FRAME_REQUESTER.get();
        }
        WidgetAnimatedStat stat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.logisticsRequester.aeIntegration.title"),
                new ItemStack(item), 0xFF00AAFF, false);
        stat.setText(ImmutableList.of("", "", "pneumaticcraft.gui.tab.info.logisticsRequester.aeIntegration"));
        stat.addSubWidget(aeIntegration = new WidgetCheckBox(16, 13, 0xFF000000,
                xlate("pneumaticcraft.gui.tab.info.logisticsRequester.aeIntegration.enable"))
                .withTag("ae2")
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (AE2Integration.isAvailable() && aeIntegration != null) {
            aeIntegration.checked = logistics.isAE2enabled();
        }
    }

}
