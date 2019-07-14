package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElectrostaticCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerElectrostaticCompressor extends Container4UpgradeSlots<TileEntityElectrostaticCompressor> {
    public ContainerElectrostaticCompressor(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        super(ModContainerTypes.ELECTROSTATIC_COMPRESSOR, i, playerInventory, getTilePos(buffer));
    }

    public ContainerElectrostaticCompressor(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainerTypes.ELECTROSTATIC_COMPRESSOR, i, playerInventory, pos);
    }
}
