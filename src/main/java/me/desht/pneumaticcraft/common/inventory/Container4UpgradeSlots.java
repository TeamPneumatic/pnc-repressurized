package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;

public class Container4UpgradeSlots<T extends TileEntityBase> extends ContainerPneumaticBase<T> {

    public Container4UpgradeSlots(ContainerType type, int i, PlayerInventory playerInventory, BlockPos tilePos) {
        super(type, i, playerInventory, tilePos);

        addUpgradeSlots(48, 29);
        addPlayerSlots(playerInventory, 84);
    }
}
