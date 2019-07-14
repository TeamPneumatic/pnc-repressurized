package me.desht.pneumaticcraft.common.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerAdvancedLiquidCompressor extends ContainerLiquidCompressor {

    public ContainerAdvancedLiquidCompressor(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(i, playerInventory, pos);
    }

    public ContainerAdvancedLiquidCompressor(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    @Override
    protected int getFluidContainerOffset() {
        return 52;
    }

}
