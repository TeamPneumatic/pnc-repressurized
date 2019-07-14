package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerElevator extends ContainerPneumaticBase<TileEntityElevatorBase> {

    public ContainerElevator(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerElevator(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainerTypes.ELEVATOR, i, playerInventory, pos);

        addUpgradeSlots(23, 29);

        addPlayerSlots(playerInventory, 84);
    }
}
