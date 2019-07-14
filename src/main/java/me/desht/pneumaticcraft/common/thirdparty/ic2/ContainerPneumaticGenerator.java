package me.desht.pneumaticcraft.common.thirdparty.ic2;

import me.desht.pneumaticcraft.common.inventory.Container4UpgradeSlots;
import net.minecraft.entity.player.PlayerInventory;

public class ContainerPneumaticGenerator extends Container4UpgradeSlots<TileEntityPneumaticGenerator> {
    public ContainerPneumaticGenerator(PlayerInventory inventory, TileEntityPneumaticGenerator te) {
        super(inventory, te);
    }
}
