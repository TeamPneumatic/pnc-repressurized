package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCreativeCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerCreativeCompressor extends ContainerPneumaticBase<TileEntityCreativeCompressor> {
    public ContainerCreativeCompressor(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerCreativeCompressor(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.CREATIVE_COMPRESSOR.get(), i, playerInventory, pos);
    }

    // phenomenal cosmic power... itty-bitty container implementation
}
