package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerAdvancedAirCompressor extends ContainerAirCompressor {

    public ContainerAdvancedAirCompressor(int windowId, PlayerInventory invPlayer, PacketBuffer extra) {
        this(windowId, invPlayer, getTilePos(extra));
    }

    public ContainerAdvancedAirCompressor(int windowId, PlayerInventory invPlayer, BlockPos tePos) {
        super(ModContainers.ADVANCED_AIR_COMPRESSOR.get(), windowId, invPlayer, tePos);
    }

    @Override
    protected int getFuelSlotXOffset() {
        return 69;  // dude
    }
}
