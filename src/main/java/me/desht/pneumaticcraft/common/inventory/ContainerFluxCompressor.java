package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluxCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerFluxCompressor extends ContainerEnergy<TileEntityFluxCompressor> {
    public ContainerFluxCompressor(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        super(ModContainerTypes.FLUX_COMPRESSOR, i, playerInventory, getTilePos(buffer));
    }

    public ContainerFluxCompressor(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainerTypes.FLUX_COMPRESSOR, i, playerInventory, pos);
    }
}
