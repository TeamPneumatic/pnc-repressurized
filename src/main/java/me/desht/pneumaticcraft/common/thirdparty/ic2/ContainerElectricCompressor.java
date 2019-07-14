package me.desht.pneumaticcraft.common.thirdparty.ic2;

import me.desht.pneumaticcraft.common.inventory.Container4UpgradeSlots;
import net.minecraft.entity.player.PlayerInventory;

public class ContainerElectricCompressor extends Container4UpgradeSlots<TileEntityElectricCompressor> {
    public ContainerElectricCompressor(PlayerInventory inventory, TileEntityElectricCompressor te) {
        super(inventory, te);
    }
}
