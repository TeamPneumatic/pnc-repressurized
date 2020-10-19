package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressurizedSpawner;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerPressurizedSpawner extends ContainerPneumaticBase<TileEntityPressurizedSpawner> {
    public ContainerPressurizedSpawner(int windowId, PlayerInventory invPlayer, PacketBuffer buffer) {
        this(windowId, invPlayer, getTilePos(buffer));
    }

    public ContainerPressurizedSpawner(int windowId, PlayerInventory invPlayer, BlockPos pos) {
        super(ModContainers.PRESSURIZED_SPAWNER.get(), windowId, invPlayer, pos);

        addSlot(new ContainerVacuumTrap.SlotSpawnerCore(te.getPrimaryInventory(), 0, 79, 38));

        addUpgradeSlots(17, 29);

        addPlayerSlots(invPlayer, 84);
    }
}
