package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDynamo;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerPneumaticDynamo extends ContainerEnergy<TileEntityPneumaticDynamo> {
    public ContainerPneumaticDynamo(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        super(ModContainers.PNEUMATIC_DYNAMO.get(), i, playerInventory, getTilePos(buffer));
    }

    public ContainerPneumaticDynamo(int i, PlayerInventory playerInventory, BlockPos tilePos) {
        super(ModContainers.PNEUMATIC_DYNAMO.get(), i, playerInventory, tilePos);
    }
}
