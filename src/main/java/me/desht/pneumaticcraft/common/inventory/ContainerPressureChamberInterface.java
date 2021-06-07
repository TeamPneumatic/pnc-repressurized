package me.desht.pneumaticcraft.common.inventory;

import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ContainerPressureChamberInterface extends ContainerPneumaticBase<TileEntityPressureChamberInterface> {

    public ContainerPressureChamberInterface(int i, PlayerInventory playerInventory, PacketBuffer buffer) {
        this(i, playerInventory, getTilePos(buffer));
    }

    public ContainerPressureChamberInterface(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModContainers.PRESSURE_CHAMBER_INTERFACE.get(), windowId, playerInventory, pos);

        // add the transfer slot
        addSlot(new SlotUntouchable(te.getPrimaryInventory(), 0, 66, 35));

        addUpgradeSlots(20, 26);

        addPlayerSlots(playerInventory, 84);
    }
}
