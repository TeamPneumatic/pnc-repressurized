package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerAerialInterface extends ContainerEnergy<TileEntityAerialInterface> {
    public ContainerAerialInterface(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        super(ModContainerTypes.AERIAL_INTERFACE, i, playerInventory, getTilePos(buffer));
    }

    public ContainerAerialInterface(int i, PlayerInventory playerInventory, BlockPos tilePos) {
        super(ModContainerTypes.AERIAL_INTERFACE, i, playerInventory, tilePos);
    }
}
