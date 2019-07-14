package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerAirCompressor extends ContainerPneumaticBase<TileEntityAirCompressor> {

    public ContainerAirCompressor(int windowId, PlayerInventory invPlayer, PacketBuffer extra) {
        this(ModContainerTypes.AIR_COMPRESSOR, windowId, invPlayer, getTilePos(extra));
    }

    public ContainerAirCompressor(int windowId, PlayerInventory invPlayer, BlockPos tePos) {
        this(ModContainerTypes.AIR_COMPRESSOR, windowId, invPlayer, tePos);
    }

    ContainerAirCompressor(ContainerType type, int windowId, PlayerInventory invPlayer, BlockPos tePos) {
        super(type, windowId, invPlayer, tePos);

        // Add the burn slot
        addSlot(new SlotItemHandler(te.getPrimaryInventory(), 0, getFuelSlotXOffset(), 54));

        addUpgradeSlots(23, 29);
        addPlayerSlots(invPlayer, 84);
    }

    protected int getFuelSlotXOffset() {
        return 80;
    }

}
