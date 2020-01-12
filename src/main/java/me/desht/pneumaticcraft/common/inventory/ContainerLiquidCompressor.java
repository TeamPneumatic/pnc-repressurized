package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerLiquidCompressor extends ContainerPneumaticBase<TileEntityLiquidCompressor> {

    public ContainerLiquidCompressor(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(ModContainers.LIQUID_COMPRESSOR.get(), i, playerInventory, getTilePos(buffer));
    }

    public ContainerLiquidCompressor(int i, PlayerInventory playerInventory, BlockPos tePos) {
        this(ModContainers.LIQUID_COMPRESSOR.get(), i, playerInventory, tePos);
    }

    ContainerLiquidCompressor(ContainerType type, int i, PlayerInventory playerInventory, BlockPos pos) {
        super(type, i, playerInventory, pos);

        addUpgradeSlots(11, 29);

        addSlot(new SlotFluidContainer(te.getPrimaryInventory(), 0, getFluidContainerOffset(), 22));
        addSlot(new SlotOutput(te.getPrimaryInventory(), 1, getFluidContainerOffset(), 55));

        addPlayerSlots(playerInventory, 84);
    }

    protected int getFluidContainerOffset() {
        return 62;
    }

}
