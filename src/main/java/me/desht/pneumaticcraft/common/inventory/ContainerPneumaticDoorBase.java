package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerPneumaticDoorBase extends ContainerPneumaticBase<TileEntityPneumaticDoorBase> {

    public ContainerPneumaticDoorBase(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerPneumaticDoorBase(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.PNEUMATIC_DOOR_BASE.get(), i, playerInventory, pos);

        addUpgradeSlots(23, 29);

        addPlayerSlots(playerInventory, 84);
    }
}
