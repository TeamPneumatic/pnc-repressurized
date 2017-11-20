package me.desht.pneumaticcraft.common.thirdparty.ic2;

import me.desht.pneumaticcraft.common.inventory.Container4UpgradeSlots;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPneumaticGenerator extends Container4UpgradeSlots<TileEntityPneumaticGenerator> {
    public ContainerPneumaticGenerator(InventoryPlayer inventory, TileEntityPneumaticGenerator te) {
        super(inventory, te);
    }
}
