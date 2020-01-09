package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityVacuumPump;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerVacuumPump extends ContainerPneumaticBase<TileEntityVacuumPump> {

    public ContainerVacuumPump(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerVacuumPump(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.VACUUM_PUMP, i, playerInventory, pos);

        addUpgradeSlots(71, 29);

        addPlayerSlots(playerInventory, 84);
    }
}
