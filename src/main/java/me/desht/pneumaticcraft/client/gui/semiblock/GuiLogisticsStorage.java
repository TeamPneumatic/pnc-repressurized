package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockStorage;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiLogisticsStorage extends GuiLogisticsBase<SemiBlockStorage> {
    public GuiLogisticsStorage(ContainerLogistics container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }
}
