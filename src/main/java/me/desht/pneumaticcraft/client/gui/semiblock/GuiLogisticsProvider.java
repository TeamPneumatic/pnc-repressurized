package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsPassiveProvider;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiLogisticsProvider extends GuiLogisticsBase<EntityLogisticsPassiveProvider> {
    public GuiLogisticsProvider(ContainerLogistics container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }
}
