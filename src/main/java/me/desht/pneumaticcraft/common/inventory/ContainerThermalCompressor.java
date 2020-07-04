package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermalCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerThermalCompressor extends ContainerPneumaticBase<TileEntityThermalCompressor> {

    public ContainerThermalCompressor(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerThermalCompressor(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.THERMAL_COMPRESSOR.get(), i, playerInventory, pos);

        addUpgradeSlots(12, 29);
        addPlayerSlots(playerInventory, 84);
    }
}
