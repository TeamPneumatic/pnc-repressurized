package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElectrostaticCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerElectrostaticCompressor extends Container4UpgradeSlots<TileEntityElectrostaticCompressor> {
    public ContainerElectrostaticCompressor(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        super(ModContainers.ELECTROSTATIC_COMPRESSOR, i, playerInventory, getTilePos(buffer));
    }

    public ContainerElectrostaticCompressor(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.ELECTROSTATIC_COMPRESSOR, i, playerInventory, pos);
    }
}
