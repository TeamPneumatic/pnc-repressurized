package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluxCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerFluxCompressor extends ContainerEnergy<TileEntityFluxCompressor> {
    public ContainerFluxCompressor(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        super(ModContainers.FLUX_COMPRESSOR.get(), i, playerInventory, getTilePos(buffer));
    }

    public ContainerFluxCompressor(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.FLUX_COMPRESSOR.get(), i, playerInventory, pos);
    }
}
