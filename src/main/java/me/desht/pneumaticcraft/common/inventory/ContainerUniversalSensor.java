package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUniversalSensor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerUniversalSensor extends ContainerPneumaticBase<TileEntityUniversalSensor> {

    public ContainerUniversalSensor(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerUniversalSensor(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainerTypes.UNIVERSAL_SENSOR, i, playerInventory, pos);

        addUpgradeSlots(19, 108);

        addPlayerSlots(playerInventory, 157);
    }
}
