package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsRequester;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiLogisticsRequester extends GuiLogisticsBase<EntityLogisticsRequester> {
    public GuiLogisticsRequester(ContainerLogistics container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.ghostSlotInteraction.title"), Textures.GUI_MOUSE_LOCATION, 0xFF00AAFF, true)
                .setText("pneumaticcraft.gui.tab.info.ghostSlotInteraction");

    }

}
