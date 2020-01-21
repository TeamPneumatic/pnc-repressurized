package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsStorage;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiLogisticsStorage extends GuiLogisticsBase<EntityLogisticsStorage> {
    public GuiLogisticsStorage(ContainerLogistics container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }
}
