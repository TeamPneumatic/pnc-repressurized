package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCreativeCompressedIronBlock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerCreativeCompressedIronBlock extends ContainerPneumaticBase<TileEntityCreativeCompressedIronBlock> {
    public ContainerCreativeCompressedIronBlock(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerCreativeCompressedIronBlock(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.CREATIVE_COMPRESSED_IRON_BLOCK.get(), i, playerInventory, pos);
    }
}
