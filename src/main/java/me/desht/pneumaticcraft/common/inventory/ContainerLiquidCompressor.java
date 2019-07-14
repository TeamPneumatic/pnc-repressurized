package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerLiquidCompressor extends ContainerPneumaticBase<TileEntityLiquidCompressor> {

    public ContainerLiquidCompressor(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerLiquidCompressor(int i, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainerTypes.LIQUID_COMPRESSOR, i, playerInventory, pos);

        addUpgradeSlots(11, 29);

        addSlot(new SlotFullFluidContainer(te.getPrimaryInventory(), 0, getFluidContainerOffset(), 22));
        addSlot(new SlotOutput(te.getPrimaryInventory(), 1, getFluidContainerOffset(), 55));

        addPlayerSlots(playerInventory, 84);
    }

    protected int getFluidContainerOffset() {
        return 62;
    }

}
