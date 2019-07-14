package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockDefaultStorage;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiLogisticsDefaultStorage extends GuiLogisticsBase<SemiBlockDefaultStorage> {
    public GuiLogisticsDefaultStorage(ContainerLogistics container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }
}
