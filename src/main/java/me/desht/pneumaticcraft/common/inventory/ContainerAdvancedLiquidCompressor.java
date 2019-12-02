package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerAdvancedLiquidCompressor extends ContainerLiquidCompressor {

    public ContainerAdvancedLiquidCompressor(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerAdvancedLiquidCompressor(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainerTypes.ADVANCED_LIQUID_COMPRESSOR, i, playerInventory, pos);
    }

    @Override
    protected int getFluidContainerOffset() {
        return 52;
    }

}
