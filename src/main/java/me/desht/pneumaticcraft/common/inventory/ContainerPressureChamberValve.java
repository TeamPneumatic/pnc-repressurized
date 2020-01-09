package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerPressureChamberValve extends ContainerPneumaticBase<TileEntityPressureChamberValve> {

    public ContainerPressureChamberValve(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerPressureChamberValve(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.PRESSURE_CHAMBER_VALVE, i, playerInventory, pos);

        addUpgradeSlots(48, 29);

        addPlayerSlots(playerInventory, 84);
    }
}
